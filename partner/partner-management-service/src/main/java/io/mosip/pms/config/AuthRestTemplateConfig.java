package io.mosip.pms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import io.mosip.pms.partner.dto.AccessTokenResponse;
import io.mosip.pms.partner.keycloak.service.MemoryCache;

@Configuration
@EnableScheduling
@EnableAsync
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
