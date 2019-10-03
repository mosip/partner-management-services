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

}
