package io.mosip.pms.partner.response.dto;

import java.util.List;

import lombok.Data;

@Data
public class PartnerPolicyCredentialTypeResponseDto {

	private String partnerId;

	private String policyId;

	private List<String> credentialTypes;
}

