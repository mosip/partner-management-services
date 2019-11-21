package io.mosip.pmp.partner.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
	
	public Properties getProperties(String path) {
	    Properties prop = new Properties();
	    try {
	    	InputStream inputStream = getClass()
	    				.getClassLoader().getResourceAsStream(path);
	        prop.load(inputStream);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return prop;
	}

}
