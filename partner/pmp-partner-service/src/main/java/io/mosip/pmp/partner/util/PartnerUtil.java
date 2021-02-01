package io.mosip.pmp.partner.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerUtil.class);
		
	/**
	 * @return partnerId.
	 */
	
	public static String createPartnerId(){
	    int id = (int)(Math.random()*1000000);
	    return id+"";
	}
	
	/**
	 * 
	 * @return
	 */
	public static String createPartnerApiKey() {
		int id = (int) (Math.random() * 100000000);
		return id + "";
	}
	
	/**
	 * 
	 * @return
	 */
	public static String generateId(){
	    int id = (int)(Math.random()*1000000);
	    return id +"";
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
	    	LOGGER.error(e.getMessage());
	    } catch (IOException e) {
	    	LOGGER.error(e.getMessage());
	    }
	    return prop;
	}
}
