package io.mosip.pms.partner.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerUtil {

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
}
