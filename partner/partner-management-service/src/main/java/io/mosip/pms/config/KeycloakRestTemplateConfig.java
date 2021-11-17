package io.mosip.pms.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import io.mosip.pms.partner.keycloak.service.RestInterceptor;

@Configuration
public class KeycloakRestTemplateConfig {

	@Autowired
	private RestInterceptor restInterceptor;
	
	@Bean(name = "keycloakRestTemplate")
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Collections.singletonList(restInterceptor));
		return restTemplate;
	}
}
