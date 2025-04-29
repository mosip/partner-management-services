package io.mosip.pms.partner.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerUtil {

	private static final Logger LOGGER = PMSLogger.getLogger(PartnerUtil.class);

	public static final String BLANK_STRING = "";

	/**
	 * @return partnerId.
	 */
	
	public static String createPartnerId(){
		return getSecureRandomId(1000000);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String createPartnerApiKey() {
		return getSecureRandomId(1000000);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String generateId(){
		return getSecureRandomId(1000000);
	}
	
	/**
	 * @return AuthPolicyId.
	 */
	
	public static String createAuthPolicyId(){
		return getSecureRandomId(1000000);
	}
	
	/**
	 * @return PartnerPolicyRequestId.
	 */
	
	public static String createPartnerPolicyRequestId(){	    
	    return getSecureRandomId(1000000);
	}
	
	/**
	 * Will generate secure random integer
	 * @param length
	 * @return
	 */
	private static String getSecureRandomId(int length) {
		SecureRandom random = new SecureRandom();
		return random.nextInt(length) + "";
	}
	/**
	 * Will generate random uuid
	 */
	public static String generateUUID(String prefix, String replaceHypen, int length)
	{
		String uniqueId = prefix + UUID.randomUUID().toString().replace("-", replaceHypen);
		if (uniqueId.length() <= length)
			return uniqueId;
		return uniqueId.substring(0, length);
	}

	public static String trimAndReplace(String str) {
		if (str == null) {
			return null;
		}
		return str.trim().replaceAll("\\s+", " ");
	}

	public static String getCertificateName(String subjectDN) {
		String[] parts = subjectDN.split(",");
		for (String part : parts) {
			if (part.trim().startsWith("CN=")) {
				return part.trim().substring(3);
			}
		}
		return BLANK_STRING;
	}

	public static void validateApiResponse(Map<String, Object> response, String apiUrl) {
		if (response == null) {
			LOGGER.debug("Received null response from API: {}", apiUrl);
			throw new BatchJobServiceException(ErrorCode.API_NULL_RESPONSE.getErrorCode(),
					ErrorCode.API_NULL_RESPONSE.getErrorMessage());
		}
		if (response.containsKey(PartnerConstants.ERRORS)) {
			List<Map<String, Object>> errorList = (List<Map<String, Object>>) response.get(PartnerConstants.ERRORS);
			if (errorList != null && !errorList.isEmpty()) {
				LOGGER.debug("Error occurred while fetching data: {}", errorList);
				throw new BatchJobServiceException(String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORCODE)),
						String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORMESSAGE)));
			}
		}
		if (!response.containsKey(PartnerConstants.RESPONSE) || response.get(PartnerConstants.RESPONSE) == null) {
			LOGGER.debug("Missing response data in API call: {}", apiUrl);
			throw new BatchJobServiceException(ErrorCode.API_NULL_RESPONSE.getErrorCode(),
					ErrorCode.API_NULL_RESPONSE.getErrorMessage());
		}
	}
}
