package io.mosip.pmp.partner.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.mosip.pmp.partner.constant.PartnerInputExceptionConstant;
import io.mosip.pmp.partner.core.ResponseWrapper;

/**
 * @author sanjeev.shrivastava
 *
 */
@RestControllerAdvice
public class PartnerControllerAdvice extends ResponseEntityExceptionHandler {

	/**
	 * @param httpServletRequest
	 * @param exception
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(PartnerAlreadyRegisteredException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassage(
			final HttpServletRequest httpServletRequest, final PartnerAlreadyRegisteredException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<ErrorResponse>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		 Map<String, Object> body = new LinkedHashMap<>(); 
		 body.put("timestamp", new Date()); 
		 body.put("status", status.value());
		 ErrorResponse errorResponse =new ErrorResponse();
		 errorResponse.setErrorCode(PartnerInputExceptionConstant.MISSING_PARTNER_INPUT_PARAMETER.getErrorCode());
		 errorResponse.setMessage(PartnerInputExceptionConstant.MISSING_PARTNER_INPUT_PARAMETER.getErrorMessage());
		 List<ErrorResponse> errors = new ArrayList<ErrorResponse>();
		 errors.add(errorResponse);
		 
		body.put("errors", errors);
		return new ResponseEntity<>(body, headers, status);
	}
	
	
	/**
	 * @param httpServletRequest
	 * @param exception
	 * @return
	 * @throws IOException
	 */
	
	@ExceptionHandler(PartnerDoesNotExistException.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getExcepionMassages(
			final HttpServletRequest httpServletRequest, final PartnerDoesNotExistException exception)
			throws IOException {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<ErrorResponse>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getErrorText());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
	

	/**
	 * @param httpServletRequest
	 * @param exception
	 * @return
	 * @throws IOException
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseWrapper<ErrorResponse>> getAllExcepionMassage(
			final HttpServletRequest httpServletRequest, final Exception exception) throws IOException {
		ResponseWrapper<ErrorResponse> responseError = new ResponseWrapper<ErrorResponse>();
		ErrorResponse errorResponse = new ErrorResponse();
		//errorResponse.setErrorCode(exception.getErrorCode());
		errorResponse.setMessage(exception.getMessage());
		responseError.setErrors(errorResponse);
		return new ResponseEntity<>(responseError, HttpStatus.OK);
	}
}
