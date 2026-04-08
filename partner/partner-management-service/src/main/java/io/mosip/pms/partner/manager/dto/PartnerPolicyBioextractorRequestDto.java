package io.mosip.pms.partner.manager.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PartnerPolicyBioextractorRequestDto {
	private String partnerPolicyRequestId;
	private String partId;
	private String policyId;
	private String attributeName;
	private String extractorProvider;
	private String extractorProviderVersion;
	private String biometricModality;
	private String biometricSubTypes;
	private String statusCode;
	private Date crDtimes;
	private Date updDtimes;
}

