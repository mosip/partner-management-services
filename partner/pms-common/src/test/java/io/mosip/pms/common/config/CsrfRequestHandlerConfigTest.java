package io.mosip.pms.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import jakarta.servlet.http.Cookie;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.test.util.ReflectionTestUtils;

public class CsrfRequestHandlerConfigTest {

	private static final String COOKIE_NAME = "XSRF-TOKEN";
	private static final String HEADER_NAME = "X-XSRF-TOKEN";

	private BeanPostProcessor postProcessor;
	private CsrfFilter csrfFilter;

	@Before
	public void setUp() {
		postProcessor = CsrfRequestHandlerConfig.csrfRequestHandlerPostProcessor();
		// same setup as the kernel auth adapter: cookie repository, no request handler
		csrfFilter = new CsrfFilter(CookieCsrfTokenRepository.withHttpOnlyFalse());
		csrfFilter = (CsrfFilter) postProcessor.postProcessBeforeInitialization(csrfFilter, "csrfFilter");
	}

	@Test
	public void otherBeansAreReturnedUnchanged() {
		Object bean = new Object();
		assertSame(bean, postProcessor.postProcessBeforeInitialization(bean, "anyBean"));
	}

	@Test
	public void getRequestSetsXsrfCookieEagerly() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		csrfFilter.doFilter(new MockHttpServletRequest("GET", "/partners"), response, new MockFilterChain());

		Cookie cookie = response.getCookie(COOKIE_NAME);
		assertNotNull("XSRF-TOKEN cookie must be set on the first GET", cookie);
		assertTrue(cookie.getValue().length() > 0);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void postWithRawCookieValueInHeaderIsAccepted() throws Exception {
		String token = fetchTokenWithGet();
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		post.setCookies(new Cookie(COOKIE_NAME, token));
		post.addHeader(HEADER_NAME, token);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		csrfFilter.doFilter(post, response, chain);

		assertEquals(200, response.getStatus());
		assertNotNull("request must reach the rest of the chain", chain.getRequest());
	}

	@Test
	public void postWithoutHeaderIsRejected() throws Exception {
		String token = fetchTokenWithGet();
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		post.setCookies(new Cookie(COOKIE_NAME, token));
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		csrfFilter.doFilter(post, response, chain);

		assertEquals(403, response.getStatus());
		assertNull(chain.getRequest());
	}

	@Test
	public void postWithWrongHeaderIsRejected() throws Exception {
		String token = fetchTokenWithGet();
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		post.setCookies(new Cookie(COOKIE_NAME, token));
		post.addHeader(HEADER_NAME, "not-the-token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		csrfFilter.doFilter(post, response, chain);

		assertEquals(403, response.getStatus());
		assertNull(chain.getRequest());
	}

	@Test
	public void loggingHandlerReturnsHeaderValueAndHandlesMissingCookies() {
		CsrfRequestHandlerConfig.LoggingCsrfTokenRequestHandler handler = new CsrfRequestHandlerConfig.LoggingCsrfTokenRequestHandler();
		CsrfToken csrfToken = CookieCsrfTokenRepository.withHttpOnlyFalse()
				.generateToken(new MockHttpServletRequest());

		// no cookies at all, no header
		assertNull(handler.resolveCsrfTokenValue(new MockHttpServletRequest("POST", "/x"), csrfToken));

		// header present, other cookie only
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/x");
		request.setCookies(new Cookie("other", "v"));
		request.addHeader(HEADER_NAME, csrfToken.getToken());
		assertEquals(csrfToken.getToken(), handler.resolveCsrfTokenValue(request, csrfToken));
	}

	@Test
	public void sessionManagementFilterGetsNoOpStrategy() {
		boolean[] called = { false };
		SessionAuthenticationStrategy original = (authentication, request, response) -> called[0] = true;
		SessionManagementFilter filter = new SessionManagementFilter(new RequestAttributeSecurityContextRepository(),
				original);

		postProcessor.postProcessBeforeInitialization(filter, "sessionManagementFilter");

		SessionAuthenticationStrategy replaced = (SessionAuthenticationStrategy) ReflectionTestUtils
				.getField(filter, "sessionAuthenticationStrategy");
		assertNotNull(replaced);
		replaced.onAuthentication(null, new MockHttpServletRequest(), new MockHttpServletResponse());
		assertTrue("original strategy must be replaced", replaced != original);
		assertEquals("replacement must do nothing", false, called[0]);
	}

	/** Does a GET through the filter and returns the cookie value the browser would store. */
	private String fetchTokenWithGet() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		csrfFilter.doFilter(new MockHttpServletRequest("GET", "/partners"), response, new MockFilterChain());
		return response.getCookie(COOKIE_NAME).getValue();
	}
}
