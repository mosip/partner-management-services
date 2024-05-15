package io.mosip.pms.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig  {

	@Bean
	public HttpFirewall defaultHttpFirewall() {
		return new DefaultHttpFirewall();
	}

	@Bean
	protected SecurityFilterChain configureSecurityFilterChain(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(http -> http.disable());
		return httpSecurity.build();
	}

	private String[] allowedEndPoints() {
		return new String[]{"*", "/swagger-ui.html"};
	}

	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		List<UserDetails> users = new ArrayList<>();
		users.add(User.withDefaultPasswordEncoder().username("misp-user").password("misp")
				.authorities(new SimpleGrantedAuthority("ROLE_MISP")).build());
		users.add(User.withDefaultPasswordEncoder().username("policy").password("policy")
				.authorities(new SimpleGrantedAuthority("POLICYMANAGER")).build());
		users.add(User.withDefaultPasswordEncoder().username("partner").password("partner")
				.authorities(new SimpleGrantedAuthority("PARTNER")).build());
		users.add(User.withDefaultPasswordEncoder().username("zonal-admin").password("admin")
				.authorities(new SimpleGrantedAuthority("ZONAL_ADMIN")).build());
		users.add(User.withDefaultPasswordEncoder().username("partner-admin").password("admin")
				.authorities(new SimpleGrantedAuthority("ROLE_PARTNER_ADMIN")).build());
		return new InMemoryUserDetailsManager(users);
	}
}
