package io.mosip.pms.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import jakarta.servlet.http.Cookie;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.test.util.ReflectionTestUtils;

public class CsrfRequestHandlerConfigTest {

	private static final String COOKIE_NAME = "XSRF-TOKEN";
	private static final String HEADER_NAME = "X-XSRF-TOKEN";
	private static final String ORIGIN = "http://localhost:3000";

	private BeanPostProcessor postProcessor;
	private BeanPostProcessor sessionPostProcessor;
	private CsrfFilter csrfFilter;

	@Before
	public void setUp() {
		postProcessor = CsrfRequestHandlerConfig.createCsrfPostProcessor(true);
		sessionPostProcessor = CsrfRequestHandlerConfig.statelessSessionStrategyPostProcessor();
		csrfFilter = new CsrfFilter(CookieCsrfTokenRepository.withHttpOnlyFalse());
		csrfFilter = (CsrfFilter) postProcessor.postProcessBeforeInitialization(csrfFilter, "csrfFilter");
	}

	@Test
	public void otherBeansAreReturnedUnchanged() {
		Object bean = new Object();
		assertSame(bean, postProcessor.postProcessBeforeInitialization(bean, "anyBean"));
		assertSame(bean, sessionPostProcessor.postProcessBeforeInitialization(bean, "anyBean"));
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
		post.addHeader("Origin", ORIGIN);
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
		post.addHeader("Origin", ORIGIN);
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
		post.addHeader("Origin", ORIGIN);
		post.addHeader(HEADER_NAME, "not-the-token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		csrfFilter.doFilter(post, response, chain);

		assertEquals(403, response.getStatus());
		assertNull(chain.getRequest());
	}

	@Test
	public void postWithoutOriginOrRefererSkipsCsrfCheck() throws Exception {
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		csrfFilter.doFilter(post, response, chain);

		assertEquals(200, response.getStatus());
		assertNotNull("direct API caller must reach the rest of the chain", chain.getRequest());
	}

	@Test
	public void postWithRefererOnlyIsStillChecked() throws Exception {
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		post.addHeader("Referer", ORIGIN + "/page");
		MockHttpServletResponse response = new MockHttpServletResponse();

		csrfFilter.doFilter(post, response, new MockFilterChain());

		assertEquals(403, response.getStatus());
	}

	@Test
	public void postWithOnlyCrossSiteFetchMetadataIsStillChecked() throws Exception {
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		post.addHeader("Sec-Fetch-Site", "cross-site");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		csrfFilter.doFilter(post, response, chain);

		assertEquals(403, response.getStatus());
		assertNull(chain.getRequest());
	}

	@Test
	public void postWithSameOriginFetchMetadataStillNeedsToken() throws Exception {
		MockHttpServletRequest noToken = new MockHttpServletRequest("POST", "/partners");
		noToken.addHeader("Sec-Fetch-Site", "same-origin");
		MockHttpServletResponse rejected = new MockHttpServletResponse();
		csrfFilter.doFilter(noToken, rejected, new MockFilterChain());
		assertEquals(403, rejected.getStatus());

		String token = fetchTokenWithGet();
		MockHttpServletRequest withToken = new MockHttpServletRequest("POST", "/partners");
		withToken.addHeader("Sec-Fetch-Site", "same-origin");
		withToken.setCookies(new Cookie(COOKIE_NAME, token));
		withToken.addHeader(HEADER_NAME, token);
		MockHttpServletResponse accepted = new MockHttpServletResponse();
		csrfFilter.doFilter(withToken, accepted, new MockFilterChain());
		assertEquals(200, accepted.getStatus());
	}

	@Test
	public void postWithNullOriginIsStillChecked() throws Exception {
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		post.addHeader("Origin", "null");
		MockHttpServletResponse response = new MockHttpServletResponse();

		csrfFilter.doFilter(post, response, new MockFilterChain());

		assertEquals(403, response.getStatus());
	}

	@Test
	public void adapterIgnoreListIsKept() throws Exception {
		CsrfFilter filter = new CsrfFilter(CookieCsrfTokenRepository.withHttpOnlyFalse());
		filter.setRequireCsrfProtectionMatcher(new AndRequestMatcher(CsrfFilter.DEFAULT_CSRF_MATCHER,
				new NegatedRequestMatcher(new AntPathRequestMatcher("/ignored/**"))));
		filter = (CsrfFilter) postProcessor.postProcessBeforeInitialization(filter, "csrfFilter");

		MockHttpServletRequest ignored = new MockHttpServletRequest("POST", "/ignored/x");
		ignored.setServletPath("/ignored/x");
		ignored.addHeader("Origin", ORIGIN);
		MockHttpServletResponse ignoredResponse = new MockHttpServletResponse();
		filter.doFilter(ignored, ignoredResponse, new MockFilterChain());
		assertEquals("ignored URL must still skip the check", 200, ignoredResponse.getStatus());

		MockHttpServletRequest checked = new MockHttpServletRequest("POST", "/partners");
		checked.setServletPath("/partners");
		checked.addHeader("Origin", ORIGIN);
		MockHttpServletResponse checkedResponse = new MockHttpServletResponse();
		filter.doFilter(checked, checkedResponse, new MockFilterChain());
		assertEquals(403, checkedResponse.getStatus());
	}

	@Test
	public void handlerReturnsHeaderValueAndHandlesMissingCookies() {
		CsrfRequestHandlerConfig.XorCsrfTokenRequestHandler handler = new CsrfRequestHandlerConfig.XorCsrfTokenRequestHandler(true);
		CsrfToken csrfToken = CookieCsrfTokenRepository.withHttpOnlyFalse()
				.generateToken(new MockHttpServletRequest());

		assertNull(handler.resolveCsrfTokenValue(new MockHttpServletRequest("POST", "/x"), csrfToken));

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

		sessionPostProcessor.postProcessBeforeInitialization(filter, "sessionManagementFilter");

		SessionAuthenticationStrategy replaced = (SessionAuthenticationStrategy) ReflectionTestUtils
				.getField(filter, "sessionAuthenticationStrategy");
		assertNotNull(replaced);
		replaced.onAuthentication(null, new MockHttpServletRequest(), new MockHttpServletResponse());
		assertTrue("original strategy must be replaced", replaced != original);
		assertEquals("replacement must do nothing", false, called[0]);
	}

	@Test
	public void csrfPostProcessorLeavesSessionStrategyAlone() {
		SessionAuthenticationStrategy original = (authentication, request, response) -> {
		};
		SessionManagementFilter filter = new SessionManagementFilter(new RequestAttributeSecurityContextRepository(),
				original);

		postProcessor.postProcessBeforeInitialization(filter, "sessionManagementFilter");

		assertSame(original, ReflectionTestUtils.getField(filter, "sessionAuthenticationStrategy"));
	}

	@Test
	public void sessionPostProcessorLeavesCsrfFilterAlone() throws Exception {
		CsrfFilter untouched = new CsrfFilter(CookieCsrfTokenRepository.withHttpOnlyFalse());
		Object requestHandlerBefore = ReflectionTestUtils.getField(untouched, "requestHandler");

		sessionPostProcessor.postProcessBeforeInitialization(untouched, "csrfFilter");

		assertSame(requestHandlerBefore, ReflectionTestUtils.getField(untouched, "requestHandler"));
	}

	@Test
	public void configIsActiveOnlyWhenCsrfIsEnabled() {
		ApplicationContextRunner runner = new ApplicationContextRunner()
				.withUserConfiguration(CsrfRequestHandlerConfig.class);

		runner.withPropertyValues("mosip.security.csrf-enable=true").run(context -> {
			assertTrue(context.containsBean("csrfXorTokenPostProcessor"));
			assertTrue(context.containsBean("statelessSessionStrategyPostProcessor"));
		});
		runner.withPropertyValues("mosip.security.csrf-enable=false").run(context -> {
			assertFalse(context.containsBean("csrfXorTokenPostProcessor"));
			assertFalse(context.containsBean("statelessSessionStrategyPostProcessor"));
		});
		runner.run(context -> {
			assertFalse(context.containsBean("csrfXorTokenPostProcessor"));
			assertFalse(context.containsBean("statelessSessionStrategyPostProcessor"));
		});
	}

	@Test
	public void browserResponseCarriesNewMaskedTokenEachTime() throws Exception {
		String raw = fetchTokenWithGet();

		String first = getWithOrigin(csrfFilter, raw).getHeader(HEADER_NAME);
		String second = getWithOrigin(csrfFilter, raw).getHeader(HEADER_NAME);

		assertNotNull(first);
		assertNotNull(second);
		assertNotEquals("masked token must differ from the raw cookie value", raw, first);
		assertNotEquals("every response must carry a new masked token", first, second);
	}

	@Test
	public void nonBrowserResponseCarriesNoMaskedToken() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();

		csrfFilter.doFilter(new MockHttpServletRequest("GET", "/partners"), response, new MockFilterChain());

		assertNull(response.getHeader(HEADER_NAME));
	}

	@Test
	public void postWithMaskedTokenFromResponseHeaderIsAccepted() throws Exception {
		String raw = fetchTokenWithGet();
		String first = getWithOrigin(csrfFilter, raw).getHeader(HEADER_NAME);
		String second = getWithOrigin(csrfFilter, raw).getHeader(HEADER_NAME);

		assertEquals(200, postWithToken(csrfFilter, raw, first).getStatus());
		assertEquals(200, postWithToken(csrfFilter, raw, second).getStatus());
	}

	@Test
	public void maskedTokenForADifferentCookieIsRejected() throws Exception {
		String otherRaw = fetchTokenWithGet();
		String masked = getWithOrigin(csrfFilter, otherRaw).getHeader(HEADER_NAME);
		String raw = fetchTokenWithGet();

		assertEquals(403, postWithToken(csrfFilter, raw, masked).getStatus());
	}

	@Test
	public void rawCookieValueIsRejectedWhenRawTokenIsNotAccepted() throws Exception {
		CsrfFilter strict = (CsrfFilter) CsrfRequestHandlerConfig.createCsrfPostProcessor(false)
				.postProcessBeforeInitialization(new CsrfFilter(CookieCsrfTokenRepository.withHttpOnlyFalse()),
						"csrfFilter");
		MockHttpServletResponse first = getWithOrigin(strict, null);
		String raw = first.getCookie(COOKIE_NAME).getValue();
		String masked = first.getHeader(HEADER_NAME);

		assertEquals(403, postWithToken(strict, raw, raw).getStatus());
		assertEquals(200, postWithToken(strict, raw, masked).getStatus());
	}

	private MockHttpServletResponse getWithOrigin(CsrfFilter filter, String rawCookie) throws Exception {
		MockHttpServletRequest get = new MockHttpServletRequest("GET", "/partners");
		get.addHeader("Origin", ORIGIN);
		if (rawCookie != null) {
			get.setCookies(new Cookie(COOKIE_NAME, rawCookie));
		}
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(get, response, new MockFilterChain());
		return response;
	}

	private MockHttpServletResponse postWithToken(CsrfFilter filter, String rawCookie, String headerValue)
			throws Exception {
		MockHttpServletRequest post = new MockHttpServletRequest("POST", "/partners");
		post.addHeader("Origin", ORIGIN);
		post.setCookies(new Cookie(COOKIE_NAME, rawCookie));
		post.addHeader(HEADER_NAME, headerValue);
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(post, response, new MockFilterChain());
		return response;
	}

	/** Does a GET through the filter and returns the cookie value the browser would store. */
	private String fetchTokenWithGet() throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		csrfFilter.doFilter(new MockHttpServletRequest("GET", "/partners"), response, new MockFilterChain());
		return response.getCookie(COOKIE_NAME).getValue();
	}
}
