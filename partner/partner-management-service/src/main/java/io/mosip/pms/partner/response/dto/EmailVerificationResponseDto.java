package io.mosip.pms.partner.response.dto;

import java.util.List;

import lombok.Data;

@Data
public class EmailVerificationResponseDto {
	
	private Boolean emailExists;
	
	private List<String> policyRequiredPartnerTypes;
}
