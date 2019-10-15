package io.mosip.pmp.misp.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0 
 * @since 2019- Oct
 *
 * Defines the object to hold the misp license details.
 */
@Data
public class MISPLiceneseDto implements Serializable{

	public static final long serialVersionUID = 671785832679079663L;
	
	public String licenseKey;
	
	public String licenseKeyExpiry;
	
	public String licenseKeyStatus;

}
