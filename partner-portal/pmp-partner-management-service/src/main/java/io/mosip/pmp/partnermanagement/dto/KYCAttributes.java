package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class KYCAttributes {
	
	/** The attribute name. */
	private String attributeName;
	
	/** required is for the attribute name */
	private boolean required;
	
	/**  masked is for the attribute  name*/
	private boolean masked;

}
