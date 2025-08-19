package io.mosip.pms.partner.keycloak.service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.dto.AccessTokenResponse;

/**
 * RestInterceptor for getting admin token
 * 
 * @author Nagarjuna 
 * @since 1.2.0
 *
 */
@Component
public class RestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER= PMSLogger.getLogger(RestInterceptor.class);
	
	@Autowired
	private MemoryCache<String, AccessTokenResponse> memoryCache;
	
	@Qualifier("authRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	ObjectMapper objectMapper;

	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	@Value("${mosip.iam.master.realm-id}")
	private String realmId;

	@Value("${mosip.keycloak.admin.client.id}")
	private String adminClientID;

	@Value("${mosip.keycloak.admin.user.id}")
	private String adminUserName;

	@Value("${mosip.keycloak.admin.secret.key}")
	private String adminSecret;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		AccessTokenResponse accessTokenResponse = null;
		if ((accessTokenResponse = memoryCache.get("adminToken")) != null) {
			boolean accessTokenExpired = isAccessTokenExpired(accessTokenResponse.getAccess_token());
			boolean refreshTokenExpired = isRefreshTokenExpired(accessTokenResponse.getRefresh_token());
			LOGGER.info("access token expired: " + accessTokenExpired + " ,refresh token expired: " + refreshTokenExpired);
			if (refreshTokenExpired){
				accessTokenResponse = getAdminToken(false, null);				
			} else if (accessTokenExpired) {
				accessTokenResponse = getAdminToken(true, accessTokenResponse.getRefresh_token());
			}
		}else {
			accessTokenResponse = getAdminToken(false, null);
		}
		memoryCache.put("adminToken", accessTokenResponse);
		request.getHeaders().add("Authorization",
				"Bearer " + (accessTokenResponse != null ? accessTokenResponse.getAccess_token() : null));
		return execution.execute(request, body);
	}

	private AccessTokenResponse getAdminToken(boolean isGetRefreshToken, String refreshToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("realmId", realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		LOGGER.info("location " + uriComponentsBuilder.toUriString() + " refresh token expired: " + isGetRefreshToken);
		if (isGetRefreshToken) {
			tokenRequestBody = getAdminValueMap(refreshToken);
		} else {
			tokenRequestBody = getAdminValueMap();
		}

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response=null;
		try {
		 response = restTemplate.postForEntity(
				uriComponentsBuilder.buildAndExpand(pathParams).toUriString(), request, AccessTokenResponse.class);
		 return response.getBody();
		}catch(HttpServerErrorException | HttpClientErrorException ex) {
			LOGGER.error(ex.getMessage());
		}
		return null;
	}

	private MultiValueMap<String, String> getAdminValueMap() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "password");
		map.add("username", adminUserName);
		map.add("password", adminSecret);
		map.add("client_id", adminClientID);
		return map;
	}

	private MultiValueMap<String, String> getAdminValueMap(String refreshToken) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "refresh_token");
		map.add("refresh_token", refreshToken);
		map.add("client_id", adminClientID);
		return map;
	}

	/**
	 * Returns true if token if expired else false
	 *
	 * @param token the token
	 * @return true if token if expired else false
	 */
	public boolean isRefreshTokenExpired(String token) {
		DecodedJWT decodedJWT = JWT.decode(token);
		long expiryEpochTime = decodedJWT.getClaim("exp").asLong();
		long currentEpoch = DateUtils.getUTCCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
		LOGGER.debug("invoked isExpired token " + expiryEpochTime + " currentEpoch " + currentEpoch);
		return currentEpoch > expiryEpochTime;
	}

	/**
	 * Returns true if token is expired else false
	 *
	 * @param token the token
	 * @return true if token is expired else false
	 * @throws IllegalArgumentException if the token is invalid or cannot be verified
	 */
	public boolean isAccessTokenExpired(String token) {
		// Verify the token
		DecodedJWT verifiedJWT = verifyToken(token);

		// Check expiration
		if (verifiedJWT.getExpiresAt() == null) {
			throw new IllegalArgumentException("Token missing exp claim");
		}

		long expiryEpochTime = verifiedJWT.getExpiresAt().toInstant().getEpochSecond();
		long currentEpoch = DateUtils.getUTCCurrentDateTime().toEpochSecond(ZoneOffset.UTC);

		LOGGER.debug("invoked isExpired token {} currentEpoch {}", expiryEpochTime, currentEpoch);
		return currentEpoch > expiryEpochTime;
	}

	/**
	 * Verifies the token signature and returns a decoded JWT.
	 *
	 * @param token the JWT token
	 * @return a verified DecodedJWT
	 * @throws IllegalArgumentException if verification fails
	 */
	private DecodedJWT verifyToken(String token) {
		try {
			DecodedJWT decodedJWT = JWT.decode(token);
			String kid = decodedJWT.getKeyId();

			String jwksUrl = UriComponentsBuilder
					.fromUriString(keycloakOpenIdUrl + "/certs")
					.buildAndExpand(realmId)
					.toUriString();

			ResponseEntity<String> response = restTemplate.getForEntity(jwksUrl, String.class);
			JsonNode jwks = objectMapper.readTree(response.getBody());

			RSAPublicKey publicKey = null;
			for (JsonNode key : jwks.get("keys")) {
				if (kid.equals(key.get("kid").asText())) {
					BigInteger modulus = new BigInteger(1,
							Base64.getUrlDecoder().decode(key.get("n").asText()));
					BigInteger exponent = new BigInteger(1,
							Base64.getUrlDecoder().decode(key.get("e").asText()));
					RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
					publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
					break;
				}
			}

			if (publicKey == null) {
				throw new IllegalArgumentException("No matching JWK found for kid: " + kid);
			}

			Algorithm algorithm = Algorithm.RSA256(publicKey, null);
			JWTVerifier verifier = JWT.require(algorithm).build();
			return verifier.verify(token);

		} catch (JWTVerificationException e) {
			throw new IllegalArgumentException("Token verification failed: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unexpected error verifying token: " + e.getMessage(), e);
		}
	}

}
