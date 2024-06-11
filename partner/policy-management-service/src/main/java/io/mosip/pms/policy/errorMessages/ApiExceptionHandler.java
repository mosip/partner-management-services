package io.mosip.pms.policy.errorMessages;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.mosip.pms.common.request.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.policy.validator.exception.PolicyObjectValidationFailedException;


/**
 * <p> This class handles all the exceptions of the policy management service.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * Exception to be thrown when validation on an argument annotated with {@code @Valid} fails.
	 *
	 */
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
    		HttpStatus status, WebRequest request) {
		ExceptionUtils.logRootCause(ex);
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        FieldError fieldError = fieldErrors.get(0);
        ServiceError serviceError = new ServiceError(ErrorMessages.MISSING_INPUT_PARAMETER.getErrorCode(),
        		"Invalid request parameter - " + fieldError.getDefaultMessage() + " :" + fieldError.getField());
		ResponseWrapper<ServiceError> errorResponse = null;
		try {
			errorResponse = setErrors(request);
			errorResponse.getErrors().add(serviceError);
		} catch (IOException e) {
			//
		}
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
    }

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("id", null);
		body.put("version", null);
		body.put("metadata", null);
		body.put("response", null);
		body.put("responsetime", LocalDateTime.now(ZoneId.of("UTC")));

		List<FieldError> fieldErrors = ex.getFieldErrors();
		FieldError fieldError = fieldErrors.get(0);

		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(ErrorMessages.MISSING_INPUT_PARAMETER.getErrorCode());
		errorResponse.setMessage("Invalid request parameter - " + fieldError.getDefaultMessage() + " :" + fieldError.getField());
		List<ErrorResponse> errors = new ArrayList<>();
		errors.add(errorResponse);
		body.put("errors", errors);
		return ResponseEntity.badRequest().body(body);
	}
	
	/**
	 * Exception to be thrown when misp application validations failed.
	 * 
	 * @param httpServletRequest
	 * @param e
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(PolicyManagementServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataServiceException(
			HttpServletRequest httpServletRequest, final PolicyManagementServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param httpServletRequest
	 * @param e
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(PolicyObjectValidationFailedException.class)
	public ResponseEntity<ResponseWrapper<List<ServiceError>>> controlDataServiceException(
			HttpServletRequest httpServletRequest, final PolicyObjectValidationFailedException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseObject(httpServletRequest, e, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param httpServletRequest
	 * @param e
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(RequestException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataServiceException(
			HttpServletRequest httpServletRequest, final RequestException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(e);
	}
		
	/**
	 * This method extract the response from HttpServletRequest request.
	 * 
	 * @param httpServletRequest
	 * @param e
	 * @param httpStatus
	 * @return
	 * @throws IOException
	 */
	private ResponseEntity<ResponseWrapper<ServiceError>> getErrorResponseEntity(HttpServletRequest httpServletRequest,
			BaseUncheckedException e, HttpStatus httpStatus) throws IOException {
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, httpStatus);
	}
	
	/**
	 * 
	 * @param exception
	 * @return
	 */
	private ResponseEntity<ResponseWrapper<ServiceError>> getErrorResponseEntity(RequestException exception){
		List<ServiceError> errors = new ArrayList<>();		
		ResponseWrapper<ServiceError> responseError = new ResponseWrapper<>();
		for (ServiceError serviceError : exception.getErrors()) {
			ServiceError errorResponse = new ServiceError();
			errorResponse.setErrorCode(serviceError.getErrorCode());
			errorResponse.setMessage(serviceError.getMessage());
			errors.add(errorResponse);
		}
		responseError.setErrors(errors);		
		return new ResponseEntity<>(responseError, HttpStatus.OK);	
	}
	/**
	 * This method extract the response from HttpServletRequest request.
	 * 
	 * @param httpServletRequest
	 * @param httpStatus
	 * @return
	 * @throws IOException
	 */
	private ResponseEntity<ResponseWrapper<List<ServiceError>>> getErrorResponseObject(HttpServletRequest httpServletRequest,
			PolicyObjectValidationFailedException ex, HttpStatus httpStatus) throws IOException {
		List<ServiceError> errors = new ArrayList<>();		
		for(io.mosip.pms.policy.validator.exception.ServiceError error: ex.getServiceErrors()) {
			errors.add(new ServiceError(error.getErrorCode(),error.getMessage()));
		}
		ResponseWrapper<List<ServiceError>> errorResponse = setServiceErrors(httpServletRequest);
		errorResponse.getErrors().addAll(errors);
		return new ResponseEntity<>(errorResponse, httpStatus);
	}	
	
	/**
	 * This method handles all runtime exceptions
	 * 
	 * @param httpServletRequest
	 * @param exception
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(HttpServletRequest httpServletRequest,
			Exception exception) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
				ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		errorResponse.getErrors().add(error);
		if(exception != null) {
			ExceptionUtils.logRootCause(exception);
		}
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	
	/**
	 *  This method maps the HttpServletRequest parameters to the response. 
	 * 
	 * @param httpServletRequest
	 * @return response
	 * @throws IOException
	 */
	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {			
			return responseWrapper;
		}		
		
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}
	
	/**
	 *  This method maps the HttpServletRequest parameters to the response. 
	 * 
	 * @param httpServletRequest
	 * @return response
	 * @throws IOException
	 */
	private ResponseWrapper<List<ServiceError>> setServiceErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<List<ServiceError>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {			
			return responseWrapper;
		}		
		
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}

	/**
	 * This method maps the WebRequest parameters to the response.
	 * 
	 * @param webRequest
	 * @return
	 * @throws IOException
	 */
	private ResponseWrapper<ServiceError> setErrors(WebRequest webRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		ServletWebRequest httpServletRequest = (ServletWebRequest) webRequest;
		if (httpServletRequest.getRequest() instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest.getRequest()).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {			
			return responseWrapper;
		}		
		
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}
}	