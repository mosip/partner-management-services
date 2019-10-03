package io.mosip.pmp.partner.util;

public class PartnerUtil {
		
	public static String createPartnerId(){
	    int id = (int)(Math.random()*1000000);
	    return id+"";
	}

}
