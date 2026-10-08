package io.mosip.pms.common.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * Temporary workaround for the kernel auth adapter, which enables CSRF with a cookie
 * repository but sets no request handler. With Spring Security 6 that leaves the
 * XSRF-TOKEN cookie unset until the first POST/PUT (403), rejects the raw token the
 * portal sends in X-XSRF-TOKEN, and, as the services are stateless, deletes the
 * cookie on every request. Remove once the adapter sets the handler itself.
 * <p>
 * Only active when mosip.security.csrf-enable=true. The token check is limited to
 * browser requests (Sec-Fetch-Site, Origin or Referer present), so direct API
 * callers are not blocked. The token is not rotated after login (no session), so a
 * token planted in advance, for example from a sibling subdomain, stays valid.
 */
@Configuration
@ConditionalOnProperty(name = "mosip.security.csrf-enable", havingValue = "true")
public class CsrfRequestHandlerConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CsrfRequestHandlerConfig.class);

	/** Must match the cookie name of the adapter's CookieCsrfTokenRepository (the default). */
	static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";

	/** Plain token handler, token loaded on every request, check limited to browser requests. */
	@Bean
	public static BeanPostProcessor csrfPlainRequestHandlerPostProcessor() {
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
					LOGGER.info("CSRF check limited to browser requests (Sec-Fetch-Site, Origin or Referer header present)");
				}
				return bean;
			}
		};
	}

	/** Replaces the session strategy that would delete the XSRF-TOKEN cookie on every stateless request. */
	@Bean
	public static BeanPostProcessor statelessSessionStrategyPostProcessor() {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) {
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

	/**
	 * Browsers send Sec-Fetch-Site, and Origin (even "null") or Referer on POST/PUT; scripts and other
	 * services usually send none of them.
	 */
	static boolean isBrowserRequest(HttpServletRequest request) {
		return request.getHeader("Sec-Fetch-Site") != null || request.getHeader("Origin") != null
				|| request.getHeader("Referer") != null;
	}

	/** Same as the plain handler, but logs (yes/no only) what the browser sent. */
	static class LoggingCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
		@Override
		public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
			String actual = super.resolveCsrfTokenValue(request, csrfToken);
			if (LOGGER.isDebugEnabled()) {
				boolean cookiePresent = false;
				if (request.getCookies() != null) {
					for (Cookie c : request.getCookies()) {
						if (XSRF_COOKIE_NAME.equals(c.getName())) {
							cookiePresent = true;
						}
					}
				}
				LOGGER.debug("CSRF check {}: cookiePresent={}, headerPresent={}, match={}", request.getMethod(),
						cookiePresent, actual != null, actual != null && actual.equals(csrfToken.getToken()));
			}
			return actual;
		}
	}
}
