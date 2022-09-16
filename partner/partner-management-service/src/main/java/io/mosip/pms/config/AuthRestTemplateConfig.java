package io.mosip.pms.config;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import io.mosip.pms.partner.dto.AccessTokenResponse;
import io.mosip.pms.partner.keycloak.service.MemoryCache;

@Configuration
@EnableScheduling
@EnableAsync
public class AuthRestTemplateConfig {
	
	@Value("${pms.auth.httpclient.connections.max.per.host:20}")
	private int maxConnectionPerRoute;

	@Value("${pms.auth.httpclient.connections.max:100}")
	private int totalMaxConnection;
	
	@Primary
	@Bean(name = "authRestTemplate")
	public RestTemplate restTemplate() {
		HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setMaxConnPerRoute(maxConnectionPerRoute)
				.setMaxConnTotal(totalMaxConnection).disableCookieManagement();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClientBuilder.build());
		return new RestTemplate(requestFactory);
	}
	
	@Bean
	public MemoryCache<String, AccessTokenResponse> memoryCache() {
		return new MemoryCache<>(1);
	}

}
