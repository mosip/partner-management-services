package io.mosip.pmp.partnermanagement.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.partnermanagement.core.ResponseWrapper;

/**
 * @author sanjeev.shrivastava
 *
 */

@RestControllerAdvice
public class PartnerMnagementControllerAdvice extends ResponseEntityExceptionHandler{
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * @param httpServletRequest
	 * @param exception
	 * @return
	 * @throws IOException
	 */
	
	@ExceptionHandler(PartnerAPIKeyDoesNotExistException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerAPIKeyDoesNotExistExceptionMassage(
			final HttpServletRequest httpServletRequest, final PartnerAPIKeyDoesNotExistException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> response = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		
		List<ErrorResponse> errorlist = new ArrayList<ErrorResponse>();
		errorlist.add(errorResponse);
		
		response.setErrors(errorlist);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * @param httpServletRequest
	 * @param exception
	 * @return
	 * @throws IOException
	 */
	
	@ExceptionHandler(PolicyNotExistException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPolicyNotExistExceptionMassage(
			final HttpServletRequest httpServletRequest, final PolicyNotExistException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> response = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		
		List<ErrorResponse> errorlist = new ArrayList<ErrorResponse>();
		errorlist.add(errorResponse);
		
		response.setErrors(errorlist);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	/**
	 * @param httpServletRequest
	 * @return
	 * @throws IOException
	 */
	
	private ResponseWrapper<ErrorResponse> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ErrorResponse> responseWrapper = new ResponseWrapper<>();
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}
	
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExceptionMassage(
			final HttpServletRequest httpServletRequest, final Exception exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> response = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getMessage());
		
		List<ErrorResponse> errorlist = new ArrayList<ErrorResponse>();
		errorlist.add(errorResponse);
		
		response.setErrors(errorlist);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
