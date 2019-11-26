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
	public String mispStatus;
	
	/**
	 *  Carries the misp license key.
	 */
	public String mispLicenseKey;
	
	/**
	 * Carries the misp license key expiry date 
	 */
	public String mispLicenseKeyExpiry;
	
	/**
	 * Carries the misp license key status(Atcive OR De-Active)
	 */
	public String mispLicenseKeyStatus;
	
	/**
	 * Carries the misp id
	 */
	public String mispID;

	/**
	 * Carries the misp Status
	 */
	public String mispStatusCode;
	/**
	 * Carries the status message
	 */	
	public String message;

}
