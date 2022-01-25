package io.mosip.pms.policy.validator.exception;

import java.util.ArrayList;
import java.util.List;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.pms.policy.validator.constants.PolicyValidatorErrorConstant;

public class PolicyObjectValidationFailedException extends BaseCheckedException {

	/**
	 * Generated serialization ID.
	 */
	private static final long serialVersionUID = -3849227719514230853L;

	List<ServiceError> serviceErrors = new ArrayList<>();
	/**
	 * Constructor for JsonValidationProcessingException class.
	 * 
	 * @param errorCode    the error code of the exception.
	 * @param errorMessage the error message associated with the exception.
	 */
	public PolicyObjectValidationFailedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Instantiates a new id object validation processing exception.
	 *
	 * @param errorConstant the error constant
	 * @param errors        the errors
	 */
	public PolicyObjectValidationFailedException(PolicyValidatorErrorConstant errorConstant, List<ServiceError> errors) {
		super(errors.get(0).getErrorCode(),errors.get(0).getMessage());
		errors.stream().forEach(error -> super.addInfo(error.getErrorCode(), error.getMessage()));
		errors.stream().forEach(error -> serviceErrors.add(new ServiceError(error.getErrorCode(), error.getMessage())));
	}

	public List<ServiceError> getServiceErrors(){
		return serviceErrors;
	}
}
