package io.mosip.pms.common.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.util.ReflectionUtils;

/**
 * The kernel auth adapter enables CSRF with a cookie repository but sets no
 * request handler, so Spring Security 6 uses the XOR handler with a lazy token.
 * That leaves the XSRF-TOKEN cookie unset until the first POST/PUT (403) and
 * rejects the raw token the portal sends in X-XSRF-TOKEN.
 * <p>
 * This switches to the plain handler and loads the token on every request.
 * The services are stateless, so the session strategy that would delete the
 * cookie on each request is replaced with a no-op.
 */
@Configuration
public class CsrfRequestHandlerConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CsrfRequestHandlerConfig.class);

	@Bean
	public static BeanPostProcessor csrfRequestHandlerPostProcessor() {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) {
				if (bean instanceof CsrfFilter csrfFilter) {
					CsrfTokenRequestAttributeHandler handler = new LoggingCsrfTokenRequestHandler();
					// null name = load the token on every request, so the cookie is always set
					handler.setCsrfRequestAttributeName(null);
					csrfFilter.setRequestHandler(handler);
					LOGGER.info("CSRF request handler replaced with plain CsrfTokenRequestAttributeHandler");
				}
				if (bean instanceof SessionManagementFilter) {
					SessionAuthenticationStrategy noOp = (authentication, request, response) -> {
					};
					java.lang.reflect.Field field = ReflectionUtils.findField(SessionManagementFilter.class,
							"sessionAuthenticationStrategy");
					if (field != null) {
						ReflectionUtils.makeAccessible(field);
						ReflectionUtils.setField(field, bean, noOp);
						LOGGER.info("Session authentication strategy (CSRF token replace) disabled for stateless service");
					}
				}
				return bean;
			}
		};
	}

	/** Same as the plain handler, but logs (yes/no only) what the browser sent. */
	static class LoggingCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
		@Override
		public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
			String actual = super.resolveCsrfTokenValue(request, csrfToken);
			boolean cookiePresent = false;
			if (request.getCookies() != null) {
				for (Cookie c : request.getCookies()) {
					if ("XSRF-TOKEN".equals(c.getName())) {
						cookiePresent = true;
					}
				}
			}
			LOGGER.debug("CSRF check {} {}: cookiePresent={}, headerPresent={}, match={}", request.getMethod(),
					request.getRequestURI(), cookiePresent, actual != null,
					actual != null && actual.equals(csrfToken.getToken()));
			return actual;
		}
	}
}
