package io.mosip.pms.policy.util;

import java.security.SecureRandom;

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
}
