package io.mosip.pmp.partner.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import io.mosip.pmp.authdevice.exception.AuthDeviceServiceException;
import io.mosip.pmp.authdevice.exception.DeviceValidationException;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.exception.ValidationException;
import io.mosip.pmp.misp.exception.MISPServiceException;
import io.mosip.pmp.partner.constant.PartnerInputExceptionConstant;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.core.ValidateResponseWrapper;

/**
 * @author sanjeev.shrivastava
 *
 */

@RestControllerAdvice
public class PartnerControllerAdvice extends ResponseEntityExceptionHandler {

	@Autowired
	private ObjectMapper objectMapper;

	String msg = "mosip.partnermanagement";
	String version = "1.0";

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains Partner already registered exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */
	@ExceptionHandler(PartnerAlreadyRegisteredException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassage(
			final HttpServletRequest httpServletRequest, final PartnerAlreadyRegisteredException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> responseError = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getAuthenticationFailedExceptionMassage(
			final HttpServletRequest httpServletRequest, final AuthenticationFailedException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> responseError = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	@ExceptionHandler(PartnerAlreadyRegisteredWithSamePolicyGroupException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerAlreadyRegisteredWithSamePolicyGroupExceptionMassage(
			final HttpServletRequest httpServletRequest,
			final PartnerAlreadyRegisteredWithSamePolicyGroupException exception) throws IOException {
		ResponseWrapper<ErrorResponse> responseError = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", new Date());
		body.put("status", status.value());
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(PartnerInputExceptionConstant.MISSING_PARTNER_INPUT_PARAMETER.getErrorCode());
		errorResponse.setMessage(PartnerInputExceptionConstant.MISSING_PARTNER_INPUT_PARAMETER.getErrorMessage());
		List<ErrorResponse> errors = new ArrayList<>();
		errors.add(errorResponse);

		body.put("errors", errors);
		return new ResponseEntity<>(body, headers, status);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains Partner Does Not Exist Exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */

	@ExceptionHandler(PartnerDoesNotExistException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final PartnerDoesNotExistException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> responseError = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains Policy Group Does Not Exist Exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */
	@ExceptionHandler(PolicyGroupDoesNotExistException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final PolicyGroupDoesNotExistException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> responseError = setErrors(httpServletRequest);
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains Partner API Key Is Not Created Exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */
	@ExceptionHandler(PartnerAPIKeyIsNotCreatedException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final PartnerAPIKeyIsNotCreatedException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		responseError.setId(msg);
		responseError.setVersion(version);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains APIKeyReqId Status In Progress Exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */
	@ExceptionHandler(APIKeyReqIdStatusInProgressException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final APIKeyReqIdStatusInProgressException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		responseError.setId(msg);
		responseError.setVersion(version);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(MISPServiceException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final MISPServiceException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		responseError.setId(msg);
		responseError.setVersion(version);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @param exception
	 *            this class contains Partner APIKeyReqID Does Not Exist Exception
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
	 */
	@ExceptionHandler(PartnerAPIKeyReqIDDoesNotExistException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerAPIKEYExcepionMassages(
			final HttpServletRequest httpServletRequest, final PartnerAPIKeyReqIDDoesNotExistException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
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
	@ExceptionHandler(PartnerDoesNotExistsException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionsMassages(
			final HttpServletRequest httpServletRequest, final PartnerDoesNotExistsException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(PartnerServiceException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final PartnerServiceException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final ValidationException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrors().get(0).getErrorCode());
		errorResponse.setMessage(exception.getErrors().get(0).getMessage());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.setErrors(errorResponse);
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
	
	@ExceptionHandler(AuthDeviceServiceException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final AuthDeviceServiceException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	
	@ExceptionHandler(RequestException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getPartnerServiceExceptionMassages(
			final HttpServletRequest httpServletRequest, final RequestException exception) {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setId(msg);
		responseError.setVersion(version);
		responseError.setErrors(errorResponse);
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
		responseError.setErrors(errorResponse);
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
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest
	 *            this class contains servlet request
	 * @return this class contains errorCode and message
	 * @throws IOException
	 *             this class contains Checked Exception
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
}
