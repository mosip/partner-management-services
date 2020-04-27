package io.mosip.pmp.misp.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the misp status update request details.
 *
 */

@Data
@ApiModel(value = "MISPStatusUpdateResponse" , description = "MISP Status update request representation")
public class MISPStatusUpdateResponse {

	/**
	 *  Carries the misp status(Active OR De-Active) 
	 */
	private String mispStatus;
	
	/**
	 *  Carries the misp license key.
	 */
	private String mispLicenseKey;
	
	/**
	 * Carries the misp license key expiry date 
	 */
	private String mispLicenseKeyExpiry;
	
	/**
	 * Carries the misp license key status(Atcive OR De-Active)
	 */
	private String mispLicenseKeyStatus;
	
	/**
	 * Carries the misp id
	 */
	private String mispID;

	/**
	 * Carries the misp Status
	 */
	private String mispStatusCode;
	/**
	 * Carries the status message
	 */	
	private String message;

}
