package io.mosip.pms.device.util;

public class DeviceUtil {

	/**
	 * 
	 * @return
	 */
	public static String generateId(){
	    int id = (int)(Math.random()*1000000);
	    return id +"";
	}
}
