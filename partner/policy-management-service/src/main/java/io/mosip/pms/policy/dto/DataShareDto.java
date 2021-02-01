package io.mosip.pms.policy.dto;

import lombok.Data;

/**
 * For data share policies
 * @author Nagarjuna
 *
 */
@Data
public class DataShareDto {

	private String validForInMinutes;
	
	private String transactionsAllowed;
	
	private String encryptionType;
	
	private String shareDomain;
	
	private String typeOfShare;
	
	//private String source;
}
