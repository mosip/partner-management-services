package io.mosip.pms.policy.util;

/**
 * 
 * @author Nagarjuna
 *
 */
public class PolicyUtil {
	
	public static String generateId() {
		int id = (int) (Math.random() * 1000000);
		return id + "";
	}
}
