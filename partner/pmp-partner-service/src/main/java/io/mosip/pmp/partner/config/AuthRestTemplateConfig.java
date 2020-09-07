package io.mosip.pmp.partner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import io.mosip.pmp.keycloak.impl.AccessTokenResponse;
import io.mosip.pmp.keycloak.impl.MemoryCache;

@Configuration
public class AuthRestTemplateConfig {

	@Primary
	@Bean(name = "authRestTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	public MemoryCache<String, AccessTokenResponse> memoryCache() {
		return new MemoryCache<>(1);
	}
}

