package io.mosip.pms.partner.misp.dto;

import lombok.Data;

@Data
public class MISPDeactivateRequestDto {

    private String policyId;
    private String licenseKeyName;
    private String status;
}
