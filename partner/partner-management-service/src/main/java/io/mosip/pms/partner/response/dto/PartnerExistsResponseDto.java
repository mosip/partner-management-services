package io.mosip.pms.partner.response.dto;

import lombok.Data;

import java.util.List;

@Data
public class PartnerExistsResponseDto {

    private boolean partnerExists;

    private boolean duplicateExists;

    private String message;

    private List<String> policyRequiredPartnerTypes;
}
