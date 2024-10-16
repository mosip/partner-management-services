package io.mosip.pms.partner.util;

import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerUtil {

	private static final Logger LOGGER = PMSLogger.getLogger(PartnerUtil.class);
	private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
	private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
	
	/**
	 * @return partnerId.
	 */
	
	public static String createPartnerId(){
		return getSecureRandomId(1000000);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String createPartnerApiKey() {
		return getSecureRandomId(1000000);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String generateId(){
		return getSecureRandomId(1000000);
	}
	
	/**
	 * @return AuthPolicyId.
	 */
	
	public static String createAuthPolicyId(){
		return getSecureRandomId(1000000);
	}
	
	/**
	 * @return PartnerPolicyRequestId.
	 */
	
	public static String createPartnerPolicyRequestId(){	    
	    return getSecureRandomId(1000000);
	}
	
	/**
	 * Will generate secure random integer
	 * @param length
	 * @return
	 */
	private static String getSecureRandomId(int length) {
		SecureRandom random = new SecureRandom();
		return random.nextInt(length) + "";
	}
	/**
	 * Will generate random uuid
	 */
	public static String generateUUID(String prefix, String replaceHypen, int length)
	{
		String uniqueId = prefix + UUID.randomUUID().toString().replace("-", replaceHypen);
		if (uniqueId.length() <= length)
			return uniqueId;
		return uniqueId.substring(0, length);
	}

	public static X509Certificate decodeCertificateData(String certificateData) {
		certificateData = certificateData.replaceAll(BEGIN_CERTIFICATE, "")
				.replaceAll(END_CERTIFICATE, "")
				.replaceAll("\n", "");
		X509Certificate cert = null;
		try {
			byte[] decodedCertificate = Base64.getDecoder().decode(certificateData);

			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decodedCertificate));
		} catch (Exception ex) {
			LOGGER.error("Could not decode the certificate data :" + ex.getMessage());
			throw new PartnerServiceException(ErrorCode.UNABLE_TO_DECODE_CERTIFICATE.getErrorCode(),
					ErrorCode.UNABLE_TO_DECODE_CERTIFICATE.getErrorMessage());
		}
		return cert;
	}
}
