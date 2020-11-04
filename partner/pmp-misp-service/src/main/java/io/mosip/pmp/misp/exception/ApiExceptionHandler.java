package io.mosip.pmp.misp.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

/**
 * <p> This class handles all the exceptions of the mosip infra service.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * Exception to be thrown when validation on an argument annotated with {@code @Valid} fails.
	 * 
	 */
	@Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, 
    		HttpStatus status, WebRequest request) {		
		
		ExceptionUtils.logRootCause(ex);
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        FieldError fieldError = fieldErrors.get(0);		
        ServiceError serviceError = new ServiceError(ErrorMessages.MISSING_INPUT_PARAMETER.getErrorCode(), 
        		ErrorMessages.MISSING_INPUT_PARAMETER.getErrorMessage() + fieldError.getField());
        logger.error(serviceError.getErrorCode(), serviceError.getMessage());
		ResponseWrapper<ServiceError> errorResponse = null;
		try {
			errorResponse = setErrors(request);
			errorResponse.getErrors().add(serviceError);
		} catch (IOException e) {
			//
		}
		return new ResponseEntity<>(errorResponse, status);        
    }
	
	/**
	 * Exception to be thrown when misp application validations failed.
	 * 
	 * @param httpServletRequest
	 * @param e
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(MISPException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataServiceException(
			HttpServletRequest httpServletRequest, final MISPException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
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
		ServiceError serviceError = new ServiceError(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
				ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		logger.error(serviceError.getErrorCode(), serviceError.getMessage());
		errorResponse.getErrors().add(serviceError);
		ExceptionUtils.logRootCause(exception);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
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
		logger.info("Response " + responseWrapper.getResponse());
		
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