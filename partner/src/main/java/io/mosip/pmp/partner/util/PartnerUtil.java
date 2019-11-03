package io.mosip.pmp.partner.util;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerUtil {
		
	/**
	 * @return partnerId.
	 */
	
	public static String createPartnerId(){
	    int id = (int)(Math.random()*1000000);
	    return id+"";
	}
	
	/**
	 * @return AuthPolicyId.
	 */
	
	public static String createAuthPolicyId(){
	    int id = (int)(Math.random()*1000000);
	    return id+"";
	}
	
	/**
	 * @return PartnerPolicyRequestId.
	 */
	
	public static String createPartnerPolicyRequestId(){
	    int id = (int)(Math.random()*1000000);
	    return id+"";
	}

}
