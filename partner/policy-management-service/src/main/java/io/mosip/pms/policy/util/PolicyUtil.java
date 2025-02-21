package io.mosip.pms.policy.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.request.dto.ErrorResponse;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.policy.errorMessages.ErrorMessages;
import io.mosip.pms.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pms.policy.errorMessages.ServiceError;
import org.springframework.data.domain.Sort;

import java.security.Policy;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Nagarjuna
 *
 */
public class PolicyUtil {

	private static final Logger LOGGER = PMSLogger.getLogger(PolicyUtil.class);
	public static final Map<String, String> aliasToColumnMap = new HashMap<>();
	static {
		aliasToColumnMap.put("policyId", "id");
		aliasToColumnMap.put("policyName", "name");
		aliasToColumnMap.put("policyDescription", "descr");
		aliasToColumnMap.put("policyGroupId", "pg.id");
		aliasToColumnMap.put("policyGroupName", "pg.name");
		aliasToColumnMap.put("status", "isActive");
		aliasToColumnMap.put("createdDateTime", "crDtimes");
	}
	
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

	public static Sort getSortingRequest (String fieldName, String sortType) {
		Sort sortingRequest = null;
		if (sortType.equalsIgnoreCase(CommonConstant.ASC)) {
			sortingRequest = Sort.by(fieldName).ascending();
		}
		if (sortType.equalsIgnoreCase(CommonConstant.DESC)) {
			sortingRequest = Sort.by(fieldName).descending();
		}
		return sortingRequest;
	}

	public static void validateGetAllPoliciesRequestParameters(String sortFieldName, String sortType, int pageNo, int pageSize) {
		// Validate sortFieldName
		if (sortFieldName != null && !aliasToColumnMap.containsKey(sortFieldName)) {
			LOGGER.error("Invalid sort field name: " + sortFieldName);
			throw new PolicyManagementServiceException(ErrorMessages.INVALID_SORT_FIELD.getErrorCode(),
					String.format(ErrorMessages.INVALID_SORT_FIELD.getErrorMessage(), sortFieldName));
		}

		// Validate sortType
		if (sortType != null &&
				!sortType.equalsIgnoreCase(CommonConstant.ASC) &&
				!sortType.equalsIgnoreCase(CommonConstant.DESC)) {
			LOGGER.error("Invalid sort type: " + sortType);
			throw new PolicyManagementServiceException(ErrorMessages.INVALID_SORT_TYPE.getErrorCode(),
					String.format(ErrorMessages.INVALID_SORT_TYPE.getErrorMessage(), sortType));
		}

		// Validate pageNo
		if (pageNo < 0) {
			LOGGER.error("Invalid page no: " + pageNo);
			throw new PolicyManagementServiceException(ErrorMessages.INVALID_PAGE_NO.getErrorCode(),
					ErrorMessages.INVALID_PAGE_NO.getErrorMessage());
		}

		// Validate pageSize
		if (pageSize <= 0) {
			LOGGER.error("Invalid page size: " + pageSize);
			throw new PolicyManagementServiceException(ErrorMessages.INVALID_PAGE_SIZE.getErrorCode(),
					ErrorMessages.INVALID_PAGE_SIZE.getErrorMessage());
		}
	}
}
