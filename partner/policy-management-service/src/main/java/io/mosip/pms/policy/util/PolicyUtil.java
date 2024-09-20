package io.mosip.pms.policy.util;

import io.mosip.pms.common.request.dto.ErrorResponse;

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

	public static List<ErrorResponse> setErrorResponse(String errorCode, String errorMessage) {
		List<ErrorResponse> errorResponseList = new ArrayList<>();
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode(errorCode);
		errorResponse.setMessage(errorMessage);
		errorResponseList.add(errorResponse);
		return errorResponseList;
	}
}
