package io.mosip.pms.partner.misp.dto;

import lombok.Data;

@Data
public class MISPDeactivateResponseDto {
    private String partnerId;
    private String policyId;
    private String licenseKeyName;
    private String licenseKeyStatus;
}
