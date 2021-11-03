package io.mosip.pms.common.util;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.core.util.TokenHandlerUtil;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.dto.Metadata;
import io.mosip.pms.common.dto.SecretKeyRequest;
import io.mosip.pms.common.dto.TokenRequestDTO;
import io.mosip.pms.common.exception.ApiAccessibleException;

@Component
public class RestUtil {

	@Autowired
	private Environment environment;

	private static final String AUTHORIZATION = "Authorization=";

	@SuppressWarnings("unchecked")
	public <T> T postApi(String apiUrl, List<String> pathsegments, String queryParamName, String queryParamValue,
			MediaType mediaType, Object requestType, Class<?> responseClass) {
		T result = null;
		UriComponentsBuilder builder = null;
		if (apiUrl != null)
			builder = UriComponentsBuilder.fromUriString(apiUrl);
		if (builder != null) {

			if (!((pathsegments == null) || (pathsegments.isEmpty()))) {
				for (String segment : pathsegments) {
					if (!((segment == null) || (("").equals(segment)))) {
						builder.pathSegment(segment);
					}
				}

			}
			if (!((queryParamName == null) || (("").equals(queryParamName)))) {
				String[] queryParamNameArr = queryParamName.split(",");
				String[] queryParamValueArr = queryParamValue.split(",");

				for (int i = 0; i < queryParamNameArr.length; i++) {
					builder.queryParam(queryParamNameArr[i], queryParamValueArr[i]);
				}
			}

			RestTemplate restTemplate;

			try {
				restTemplate = getRestTemplate();
				result = (T) restTemplate.postForObject(builder.toUriString(), setRequestHeader(requestType, mediaType),
						responseClass);

			} catch (Exception e) {
				throw new ApiAccessibleException(
						ApiAccessibleExceptionConstant.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
						ApiAccessibleExceptionConstant.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage() + apiUrl);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getApi(String apiName, List<String> pathsegments, String queryParamName, String queryParamValue,
			Class<?> responseType) {
		String apiHostIpPort = environment.getProperty(apiName);
		T result = null;
		UriComponentsBuilder builder = null;
		UriComponents uriComponents = null;
		if (apiHostIpPort != null) {

			builder = UriComponentsBuilder.fromUriString(apiHostIpPort);
			if (!((pathsegments == null) || (pathsegments.isEmpty()))) {
				for (String segment : pathsegments) {
					if (!((segment == null) || (("").equals(segment)))) {
						builder.pathSegment(segment);
					}
				}
			}

			if (!((queryParamName == null) || (("").equals(queryParamName)))) {

				String[] queryParamNameArr = queryParamName.split(",");
				String[] queryParamValueArr = queryParamValue.split(",");
				for (int i = 0; i < queryParamNameArr.length; i++) {
					builder.queryParam(queryParamNameArr[i], queryParamValueArr[i]);
				}

			}
			uriComponents = builder.build(false).encode();
			RestTemplate restTemplate;

			try {
				restTemplate = getRestTemplate();
				result = (T) restTemplate
						.exchange(uriComponents.toUri(), HttpMethod.GET, setRequestHeader(null, null), responseType)
						.getBody();
			} catch (Exception e) {
				throw new ApiAccessibleException(
						ApiAccessibleExceptionConstant.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
						ApiAccessibleExceptionConstant.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage() + apiName);
			}

		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getApi(String apiUrl, Map<String, String> pathsegments, Class<?> responseType) {
		T result = null;
		UriComponentsBuilder builder = null;
		if (apiUrl != null) {

			builder = UriComponentsBuilder.fromUriString(apiUrl);

			URI urlWithPath = builder.build(pathsegments);

			RestTemplate restTemplate;

			try {
				restTemplate = getRestTemplate();
				result = (T) restTemplate
						.exchange(urlWithPath, HttpMethod.GET, setRequestHeader(null, null), responseType).getBody();
			} catch (Exception e) {
				throw new ApiAccessibleException(
						ApiAccessibleExceptionConstant.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
						ApiAccessibleExceptionConstant.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage() + apiUrl);
			}

		}
		return result;
	}

	public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		return new RestTemplate(requestFactory);

	}

	@SuppressWarnings("unchecked")
	private HttpEntity<Object> setRequestHeader(Object requestType, MediaType mediaType) throws IOException {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Cookie", getToken());
		if (mediaType != null) {
			headers.add("Content-Type", mediaType.toString());
		}
		if (requestType != null) {
			try {
				HttpEntity<Object> httpEntity = (HttpEntity<Object>) requestType;
				HttpHeaders httpHeader = httpEntity.getHeaders();
				Iterator<String> iterator = httpHeader.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if (!(headers.containsKey("Content-Type") && key == "Content-Type"))
						headers.add(key, httpHeader.get(key).get(0));
				}
				return new HttpEntity<Object>(httpEntity.getBody(), headers);
			} catch (ClassCastException e) {
				return new HttpEntity<Object>(requestType, headers);
			}
		} else
			return new HttpEntity<Object>(headers);
	}

	public String getToken() throws IOException {
		String token = System.getProperty("token");
		boolean isValid = false;

		if (StringUtils.isNotEmpty(token)) {
			isValid = TokenHandlerUtil.isValidBearerToken(token,
					environment.getProperty("pms.cert.service.token.request.issuerUrl"),
					environment.getProperty("pms.cert.service.token.request.clientId"));
		}
		if (!isValid) {
			TokenRequestDTO<SecretKeyRequest> tokenRequestDTO = new TokenRequestDTO<SecretKeyRequest>();
			tokenRequestDTO.setMetadata(new Metadata());

			tokenRequestDTO.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
			// tokenRequestDTO.setRequest(setPasswordRequestDTO());
			tokenRequestDTO.setRequest(setSecretKeyRequestDTO());

			Gson gson = new Gson();
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(environment.getProperty("pms.cert.service.token.request.issuerUrl"));
			try {
				StringEntity postingString = new StringEntity(gson.toJson(tokenRequestDTO));
				post.setEntity(postingString);
				post.setHeader("Content-type", "application/json");
				HttpResponse response = httpClient.execute(post);
				Header[] cookie = response.getHeaders("Set-Cookie");
				if (cookie.length == 0)
					throw new IOException("cookie is empty. Could not generate new token.");
				token = response.getHeaders("Set-Cookie")[0].getValue();
				System.setProperty("token", token.substring(14, token.indexOf(';')));
				return token.substring(0, token.indexOf(';'));
			} catch (IOException e) {
				throw e;
			}
		}
		return AUTHORIZATION + token;
	}

	private SecretKeyRequest setSecretKeyRequestDTO() {
		SecretKeyRequest request = new SecretKeyRequest();
		request.setAppId(environment.getProperty("mosip.pmp.auth.appId"));
		request.setClientId(environment.getProperty("mosip.pmp.auth.clientId"));
		request.setSecretKey(environment.getProperty("mosip.pmp.auth.secretKey"));
		return request;
	}
}
