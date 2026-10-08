package io.mosip.pms.common.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

import java.util.function.Supplier;

/**
 * Configures CSRF handling for the kernel auth adapter under Spring Security 6.
 * Active only when mosip.security.csrf-enable=true. The token is not rotated after login.
 */
@Configuration
@ConditionalOnProperty(name = "mosip.security.csrf-enable", havingValue = "true")
public class CsrfRequestHandlerConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(CsrfRequestHandlerConfig.class);

	/** Cookie name used by the adapter's CookieCsrfTokenRepository. */
	static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";

	/** Accept the raw cookie value besides the XOR token (default true). */
	static final String ACCEPT_RAW_TOKEN_PROPERTY = "mosip.pms.csrf.accept-raw-token";

	/**
	 * Sends a new XOR token with every browser response and limits the token check to browser requests.
	 */
	@Bean
	public static BeanPostProcessor csrfXorTokenPostProcessor(Environment environment) {
		return createCsrfPostProcessor(environment.getProperty(ACCEPT_RAW_TOKEN_PROPERTY, Boolean.class, true));
	}

	static BeanPostProcessor createCsrfPostProcessor(boolean acceptRawToken) {
		return new BeanPostProcessor() {
			@Override
			public Object postProcessBeforeInitialization(Object bean, String beanName) {
				if (bean instanceof CsrfFilter csrfFilter) {
					XorCsrfTokenRequestHandler handler = new XorCsrfTokenRequestHandler(acceptRawToken);
					handler.setCsrfRequestAttributeName(null);
					csrfFilter.setRequestHandler(handler);
					LOGGER.info("CSRF request handler replaced with XOR token handler (raw token accepted: {})",
							acceptRawToken);

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

	/**
	 * Stops the session strategy from deleting the XSRF-TOKEN cookie on every request.
	 */
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
	 * True if the request has Sec-Fetch-Site, Origin or Referer, which browsers send and scripts usually do not.
	 */
	static boolean isBrowserRequest(HttpServletRequest request) {
		return request.getHeader("Sec-Fetch-Site") != null || request.getHeader("Origin") != null
				|| request.getHeader("Referer") != null;
	}

	/**
	 * Sends a new XOR token in the X-XSRF-TOKEN response header and accepts it, or the raw cookie value if allowed.
	 */
	static class XorCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

		private final XorCsrfTokenRequestAttributeHandler xorHandler = new XorCsrfTokenRequestAttributeHandler();
		private final CsrfTokenRequestAttributeHandler rawHandler = new CsrfTokenRequestAttributeHandler();
		private final boolean acceptRawToken;

		XorCsrfTokenRequestHandler(boolean acceptRawToken) {
			this.acceptRawToken = acceptRawToken;
		}

		void setCsrfRequestAttributeName(String name) {
			xorHandler.setCsrfRequestAttributeName(name);
		}

		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response,
				Supplier<CsrfToken> deferredCsrfToken) {
			xorHandler.handle(request, response, deferredCsrfToken);
			if (isBrowserRequest(request)) {
				CsrfToken xorToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
				response.setHeader(xorToken.getHeaderName(), xorToken.getToken());
			}
		}

		@Override
		public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
			String actual = xorHandler.resolveCsrfTokenValue(request, csrfToken);
			boolean xorValid = actual != null && actual.equals(csrfToken.getToken());
			if (!xorValid && acceptRawToken) {
				actual = rawHandler.resolveCsrfTokenValue(request, csrfToken);
			}
			if (LOGGER.isDebugEnabled()) {
				boolean cookiePresent = false;
				if (request.getCookies() != null) {
					for (Cookie c : request.getCookies()) {
						if (XSRF_COOKIE_NAME.equals(c.getName())) {
							cookiePresent = true;
						}
					}
				}
				LOGGER.debug("CSRF check {}: cookiePresent={}, headerPresent={}, xor={}, match={}",
						request.getMethod(), cookiePresent, actual != null, xorValid,
						actual != null && actual.equals(csrfToken.getToken()));
			}
			return actual;
		}
	}
}
