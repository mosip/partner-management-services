package io.mosip.pms.policy.util;

import io.mosip.pms.common.request.dto.ErrorResponse;
import io.mosip.pms.policy.errorMessages.ServiceError;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Nagarjuna
 *
 */
public class PolicyUtil {
	
	public static String generateId() {
		SecureRandom random = new SecureRandom();
		return random.nextInt(100000) + "";
	}

	public static List<ServiceError> setErrorResponse(String errorCode, String errorMessage) {
		List<ServiceError> serviceErrorList = new ArrayList<>();
		ServiceError serviceError = new ServiceError();
		serviceError.setErrorCode(errorCode);
		serviceError.setMessage(errorMessage);
		serviceErrorList.add(serviceError);
		return serviceErrorList;
	}
}
