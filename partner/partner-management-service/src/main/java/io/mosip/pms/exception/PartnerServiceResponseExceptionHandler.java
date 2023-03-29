package io.mosip.pms.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.exception.ValidationException;
import io.mosip.pms.common.request.dto.ErrorResponse;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.common.response.dto.ValidateResponseWrapper;
import io.mosip.pms.device.exception.DeviceServiceException;
import io.mosip.pms.device.exception.DeviceValidationException;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.manager.exception.PartnerManagerServiceException;
import io.mosip.pms.partner.misp.exception.MISPServiceException;

@RestControllerAdvice
public class PartnerServiceResponseExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private ObjectMapper objectMapper;

	String msg = "mosip.partnermanagement";
	String version = "1.0";

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("id", null);
		body.put("version", null);
		body.put("metadata", null);
		body.put("response", null);
		body.put("responsetime", LocalDateTime.now(ZoneId.of("UTC")));
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        FieldError fieldError = fieldErrors.get(0);
        
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(ErrorCode.MISSING_PARTNER_INPUT_PARAMETER.getErrorCode());
		errorResponse.setMessage("Invalid request parameter - " + fieldError.getDefaultMessage() + " :" + fieldError.getField());
		List<ErrorResponse> errors = new ArrayList<>();
		errors.add(errorResponse);
		body.put("errors", errors);
		return new ResponseEntity<>(body, headers, HttpStatus.OK);
	}
	
	@ExceptionHandler(MISPServiceException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final MISPServiceException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.getErrors().add(errorResponse);
		responseError.setId(msg);
		responseError.setVersion(version);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(PartnerServiceException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final PartnerServiceException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.getErrors().add(errorResponse);
		responseError.setId(msg);
		responseError.setVersion(version);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains Partner Does Not Exists Exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */
	@ExceptionHandler(PartnerManagerServiceException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionsMassages(
			final HttpServletRequest httpServletRequest, final PartnerManagerServiceException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.getErrors().add(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final ValidationException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		List<ErrorResponse> errors = new ArrayList<>();		
		for (ServiceError serviceError : exception.getErrors()) {
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.setErrorCode(serviceError.getErrorCode());
			errorResponse.setMessage(serviceError.getMessage());
			errors.add(errorResponse);
		}
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.setErrors(errors);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(DeviceValidationException.class)
	public ResponseEntity<ValidateResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final DeviceValidationException exception) {
		ValidateResponseWrapper<ErrorResponse> responseError = new ValidateResponseWrapper<>();
		List<ErrorResponse> errors = new ArrayList<>();		
		for (ServiceError serviceError : exception.getErrors()) {
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.setErrorCode(serviceError.getErrorCode());
			errorResponse.setMessage(serviceError.getMessage());
			errors.add(errorResponse);
		}
		responseError.setId("io.mosip.devicemanagement");
		responseError.setVersion(version);
		responseError.setErrors(errors);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(DeviceServiceException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final DeviceServiceException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.getErrors().add(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(ApiAccessibleException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final ApiAccessibleException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.getErrors().add(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(AuthZException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final AuthZException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getList().get(0).getErrorCode());
		errorResponse.setMessage(exception.getList().get(0).getMessage());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.getErrors().add(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(io.mosip.pms.common.exception.RequestException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final io.mosip.pms.common.exception.RequestException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		List<ErrorResponse> errors = new ArrayList<>();		
		for (ServiceError serviceError : exception.getErrors()) {
			ErrorResponse errorResponse = new ErrorResponse();
			errorResponse.setErrorCode(serviceError.getErrorCode());
			errorResponse.setMessage(serviceError.getMessage());
			errors.add(errorResponse);
		}
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.setErrors(errors);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains Checked Exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getAllExcepionMassage(
			final HttpServletRequest httpServletRequest, final Exception exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setMessage(exception.getMessage());
		responseError.getErrors().add(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */

	@SuppressWarnings("unused")
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

}
