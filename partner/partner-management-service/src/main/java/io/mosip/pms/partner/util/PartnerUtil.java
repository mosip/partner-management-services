package io.mosip.pms.partner.util;

import java.security.SecureRandom;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerUtil {
	
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
}
