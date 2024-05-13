package io.mosip.pms.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class AntPathMatcherConfig {
	@Bean
	public AntPathMatcher antPathMatcher() {
		return new AntPathMatcher();
	}
}