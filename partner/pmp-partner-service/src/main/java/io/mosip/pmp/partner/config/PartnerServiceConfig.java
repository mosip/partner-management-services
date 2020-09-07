package io.mosip.pmp.partner.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import io.mosip.pmp.keycloak.impl.AccessTokenResponse;
import io.mosip.pmp.keycloak.impl.MemoryCache;
import io.mosip.pmp.keycloak.impl.RestInterceptor;
import io.mosip.pmp.partner.util.RestUtil;

@Configuration
public class PartnerServiceConfig {

	@Autowired
	private RestInterceptor restInterceptor;
	
	@Bean
	public RestUtil getRestUtil() {
		return new RestUtil();
	}
	
	@Primary
	@Bean(name = "authRestTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean(name = "keycloakRestTemplate")
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Collections.singletonList(restInterceptor));
		return restTemplate;
	}
	
	@Bean
	public MemoryCache<String, AccessTokenResponse> memoryCache() {
		return new MemoryCache<>(1);
	}
}
