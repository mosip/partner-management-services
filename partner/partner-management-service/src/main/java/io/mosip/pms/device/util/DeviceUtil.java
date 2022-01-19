package io.mosip.pms.device.util;

import java.security.SecureRandom;

public class DeviceUtil {

	/**
	 * 
	 * @return
	 */
	public static String generateId(){
		SecureRandom random = new SecureRandom();
		return random.nextInt((100000)) + "";
	}
}
