package io.mosip.pmp.misp.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import io.mosip.pmp.misp.utils.MispLogger;

/**
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 */

public class ReqResFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 *  The doFilter method of the Filter is called by the container each time a request/response pair is passed through the 
	 *  chain due to a client request for a resource at the end of the chain. The FilterChain passed in to this method allows the 
	 *  Filter to pass on the request and response to the next entity in the chain.
	 *    
	 */
	@SuppressWarnings("unused")
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		RequestWrapper myRequestWrapper = new RequestWrapper((HttpServletRequest) request);
		ResponseWrapper myResponseWrapper = new ResponseWrapper((HttpServletResponse)response);
		
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
		
		MispLogger.info("ClientIP : " + myRequestWrapper.getRemoteHost() + " clientPort : " + request.getRemotePort() + "Uri :" +myRequestWrapper.getRequestURI());
		MispLogger.info("RequestBody: " + myRequestWrapper.getBody());
		
		chain.doFilter(myRequestWrapper, responseWrapper);
//		byte[] bytes = myResponseWrapper.getByteArray();
//
//		myResponseWrapper.getOutputStream().write(bytes);
		
		responseWrapper.copyBodyToResponse();
	}

	@Override
	public void destroy() {
	}	
}
