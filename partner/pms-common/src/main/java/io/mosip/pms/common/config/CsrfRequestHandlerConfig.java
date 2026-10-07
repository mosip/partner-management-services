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
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

/**
 * The kernel auth adapter enables CSRF with a cookie repository but sets no
 * request handler, so Spring Security 6 uses the XOR handler with a lazy token.
 * That leaves the XSRF-TOKEN cookie unset until the first POST/PUT (403) and
 * rejects the raw token the portal sends in X-XSRF-TOKEN.
 * <p>
 * This switches to the plain handler and loads the token on every request.
 * The services are stateless, so the session strategy that would delete the
 * cookie on each request is replaced with a no-op. The check is limited to
 * browser requests (Origin or Referer present), so direct API callers are not
 * blocked.
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

					// keep the adapter's own rules (safe methods, csrf_ignore.url) and add the browser check
					java.lang.reflect.Field matcherField = ReflectionUtils.findField(CsrfFilter.class,
							"requireCsrfProtectionMatcher");
					if (matcherField == null) {
						throw new IllegalStateException(
								"CsrfFilter.requireCsrfProtectionMatcher not found; cannot limit CSRF checks to browser requests");
					}
					ReflectionUtils.makeAccessible(matcherField);
					RequestMatcher existing = (RequestMatcher) ReflectionUtils.getField(matcherField, csrfFilter);
					csrfFilter.setRequireCsrfProtectionMatcher(
							request -> existing.matches(request) && isBrowserRequest(request));
					LOGGER.info("CSRF check limited to browser requests (Origin or Referer header present)");
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
					} else {
						throw new IllegalStateException(
								"SessionManagementFilter.sessionAuthenticationStrategy not found; CSRF cookie would be cleared on every request");
					}
				}
				return bean;
			}
		};
	}

	/** Browsers always send Origin (even "null") or Referer on POST/PUT; scripts and other services usually do not. */
	static boolean isBrowserRequest(HttpServletRequest request) {
		return request.getHeader("Origin") != null || request.getHeader("Referer") != null;
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
			LOGGER.debug("CSRF check {}: cookiePresent={}, headerPresent={}, match={}", request.getMethod(),
					cookiePresent, actual != null,
					actual != null && actual.equals(csrfToken.getToken()));
			return actual;
		}
	}
}
