package io.mosip.pmp.partner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.pmp.partner.util.RestUtil;

@Configuration
public class PartnerServiceConfig {

	@Bean
	public RestUtil getRestUtil() {
		return new RestUtil();
	}
}
