package io.mosip.pmp.partner.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.mosip.pmp.partner.core.RequestWrapper;

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
	
	public static String createAuthPolicyId(){
	    int id = (int)(Math.random()*1000000);
	    return id+"";
	}
	
	
	/**
	 * Creates the request.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @return the request wrapper
	 */
	public static <T> RequestWrapper<T> createRequest(T t){
		
		LocalDateTime localDateTime = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
		
    	RequestWrapper<T> request = new RequestWrapper<>();
    	request.setRequest(t);
    	request.setId("ida");
    	request.setRequesttime(localDateTime);
    	return request;
    }

}
