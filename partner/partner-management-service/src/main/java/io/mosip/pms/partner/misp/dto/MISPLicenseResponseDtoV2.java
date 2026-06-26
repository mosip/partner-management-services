package io.mosip.pms.partner.misp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MISPLicenseResponseDtoV2 {

    private String mispLicenseId;

    private String partnerId;

    private String policyId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String licenseKey;

    private String licenseKeyName;

    private String status;

    private LocalDateTime expiryDateTime;
}
