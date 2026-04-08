package io.mosip.pms.partner.manager.dto;

import lombok.Data;

import java.util.List;

@Data
public class PartnerPolicyBioextractorRequestResponseDto {
	private String requestId;
	private List<PartnerPolicyBioextractorRequestDto> bioExtractors;
}

