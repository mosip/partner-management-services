package io.mosip.pms.test.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {


	@Bean
	public HttpFirewall defaultHttpFirewall() {
		return new DefaultHttpFirewall();
	}

	@Bean
	protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(csrf -> csrf.disable());
		httpSecurity.authorizeHttpRequests(cfg -> cfg.anyRequest().permitAll());
		return httpSecurity.build();
	}

	private String[] allowedEndPoints() {
		return new String[] { "*","/swagger-ui.html" };
	}

	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		List<UserDetails> users = new ArrayList<>();
		users.add(new User("misp-user", "misp",
				Arrays.asList(new SimpleGrantedAuthority("ROLE_MISP"))));
		users.add(new User("policy", "policy",
				Arrays.asList(new SimpleGrantedAuthority("POLICYMANAGER"))));
		users.add(new User("partner", "partner",
				Arrays.asList(new SimpleGrantedAuthority("PARTNER"))));
		users.add(new User("zonal-admin", "admin",
				Arrays.asList(new SimpleGrantedAuthority("ZONAL_ADMIN"))));
		users.add(new User("partner-admin", "admin",
				Arrays.asList(new SimpleGrantedAuthority("ROLE_PARTNER_ADMIN"))));
		return new InMemoryUserDetailsManager(users);
	}
}