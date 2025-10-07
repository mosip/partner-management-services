package io.mosip.pms.partner.misp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MISPLicenseResponseDtoV2 {

    private String partnerId;

    private String policyId;

    private String licenseKey;

    private String licenseKeyName;

    private String licenseKeyStatus;

    private LocalDateTime expiryDateTime;
}
