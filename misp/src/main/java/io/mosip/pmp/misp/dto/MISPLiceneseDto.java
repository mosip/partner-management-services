package io.mosip.pmp.misp.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MISPLiceneseDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 671785832679079663L;
	
	private String licenseKey;
	
	private String licenseKeyExpiry;
	
	private String licenseKeyStatus;

}
