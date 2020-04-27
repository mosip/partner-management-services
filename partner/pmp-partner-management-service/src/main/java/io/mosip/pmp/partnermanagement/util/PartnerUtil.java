package io.mosip.pmp.partnermanagement.util;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerUtil {

	public PartnerUtil() {
	}

	/**
	 * @return partnerId.
	 */

	public static String createPartnerId() {
		int id = (int) (Math.random() * 1000000);
		return id + "";
	}

	public static String createPartnerApiKey() {
		int id = (int) (Math.random() * 100000000);
		return id + "";
	}
}
