package io.mosip.pms.partner.keycloak.service;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

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
			boolean accessTokenExpired = isExpired(accessTokenResponse.getAccess_token());
			boolean refreshTokenExpired = isExpired(accessTokenResponse.getRefresh_token());
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
	public boolean isExpired(String token) {
		DecodedJWT decodedJWT = JWT.decode(token);
		long expiryEpochTime = decodedJWT.getClaim("exp").asLong();
		long currentEpoch = DateUtils.getUTCCurrentDateTime().toEpochSecond(ZoneOffset.UTC);
		LOGGER.debug("invoked isExpired token " + expiryEpochTime + " currentEpoch " + currentEpoch);
		return currentEpoch > expiryEpochTime;
	}
}
