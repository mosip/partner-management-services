package io.mosip.pms.partner.misp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MISPLicenseRequestDtoV2 {

    private String partnerId;
    private String policyId;
    private String licenseKeyName;
    private LocalDate expiryDate;

}
