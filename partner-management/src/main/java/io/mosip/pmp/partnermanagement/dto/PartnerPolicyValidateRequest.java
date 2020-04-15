package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class PartnerPolicyValidateRequest {
	
	private String partnerId;
	
	private String partner_api_key;
	
	private String misp_license;

}
