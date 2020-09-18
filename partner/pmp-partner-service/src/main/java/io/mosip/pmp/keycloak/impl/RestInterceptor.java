package io.mosip.pmp.keycloak.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * RestInterceptor for getting admin token
 * 
 * @author Nagarjuna 
 * @since 1.2.0
 *
 */
@Component
public class RestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER= LoggerFactory.getLogger(RestInterceptor.class);
	
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
		} else {
			accessTokenResponse = getAdminToken(false, null);
		}
		memoryCache.put("adminToken", accessTokenResponse);
		request.getHeaders().add("Authorization", "Bearer " + accessTokenResponse.getAccess_token());
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
		}catch(HttpServerErrorException | HttpClientErrorException ex) {
			LOGGER.error(ex.getMessage());
		}
		
		return response.getBody();
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
}
