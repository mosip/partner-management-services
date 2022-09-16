package io.mosip.pms.config;

import java.util.Collections;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import io.mosip.pms.partner.keycloak.service.RestInterceptor;

@Configuration
public class KeycloakRestTemplateConfig {
	
	@Value("${pms.keycloak.httpclient.connections.max.per.host:20}")
	private int maxConnectionPerRoute;

	@Value("${pms.keycloak.httpclient.connections.max:100}")
	private int totalMaxConnection;

	@Autowired
	private RestInterceptor restInterceptor;
	
	@Bean(name = "keycloakRestTemplate")
	public RestTemplate getRestTemplate() {
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setMaxConnPerRoute(maxConnectionPerRoute)
				.setMaxConnTotal(totalMaxConnection).disableCookieManagement();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		restTemplate.setInterceptors(Collections.singletonList(restInterceptor));
		return restTemplate;
	}
}
