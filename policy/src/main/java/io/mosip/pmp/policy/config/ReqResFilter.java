package io.mosip.pmp.policy.config;

import java.io.IOException;
import java.time.Instant;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * @author Nagarjuna
 *
 */
public class ReqResFilter implements Filter {
	
	private static final Logger logger = LoggerFactory.getLogger(ReqResFilter.class);
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// init method overriding
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		ContentCachingRequestWrapper requestWrapper = null;
		ContentCachingResponseWrapper responseWrapper = null;	

			if (httpServletRequest.getRequestURI().endsWith(".stream")) {
				chain.doFilter(request, response);
				return;
			}
			requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
			responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);
			
			logRequest(requestWrapper);
			chain.doFilter(requestWrapper, responseWrapper);
			

			responseWrapper.copyBodyToResponse();
	}

	@Override
	public void destroy() {
		// Auto-generated method stub
	}
	
	private void logRequest(ContentCachingRequestWrapper request)
	{
		@SuppressWarnings("unused")
		String requestBody;
		if (request instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) request).getContentAsByteArray());
		}
		long startTime = Instant.now().toEpochMilli();
		  logger.info("Request URL::" + request.getRequestURL().toString() +
		   ":: Start Time=" + Instant.now());
		  request.setAttribute("startTime", startTime);
	}
}
