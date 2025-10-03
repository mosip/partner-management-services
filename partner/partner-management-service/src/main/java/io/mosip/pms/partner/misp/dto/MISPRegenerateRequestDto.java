package io.mosip.pms.partner.misp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MISPRegenerateRequestDto {

    private String policyId;
    private String licenseKeyName;
    private LocalDate expiryDate;
}
