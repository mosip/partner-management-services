package io.mosip.pmp.policy.errorMessages;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
 * @author Nagarjuna Kuchi
 *
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, 
    		HttpStatus status, WebRequest request) {		
		
		ExceptionUtils.logRootCause(ex);
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        FieldError fieldError = fieldErrors.get(0);		
        ServiceError serviceError = new ServiceError(ErrorMessagesEnumeration.MISSING_INPUT_PARAMETER.getErrorCode(), 
				ErrorMessagesEnumeration.MISSING_INPUT_PARAMETER.getErrorMessage() + fieldError.getField());
		ResponseWrapper<ServiceError> errorResponse = null;
		try {
			errorResponse = setErrors(request);
		} catch (IOException e) {
			//
		}
		errorResponse.getErrors().add(serviceError);
		return new ResponseEntity<>(errorResponse, status);        
    }
	
	@ExceptionHandler(PolicyManagementServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataServiceException(
			HttpServletRequest httpServletRequest, final PolicyManagementServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(DataViolationException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlDataNotFoundException(
			HttpServletRequest httpServletRequest, final DataViolationException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}

	@ExceptionHandler(RequestException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(HttpServletRequest httpServletRequest,
			final RequestException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}
	

	@ExceptionHandler(FilePathNotFoundException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlPathNotFoundException(HttpServletRequest httpServletRequest,
			final FilePathNotFoundException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(httpServletRequest, e, HttpStatus.OK);
	}
	
	private ResponseEntity<ResponseWrapper<ServiceError>> getErrorResponseEntity(HttpServletRequest httpServletRequest,
			BaseUncheckedException e, HttpStatus httpStatus) throws IOException {
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, httpStatus);
	}
	
	
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(HttpServletRequest httpServletRequest,
			Exception exception) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(ErrorMessagesEnumeration.INTERNAL_SERVER_ERROR.getErrorCode(),
				ErrorMessagesEnumeration.INTERNAL_SERVER_ERROR.getErrorMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(exception);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * @param httpServletRequest
	 * @return
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