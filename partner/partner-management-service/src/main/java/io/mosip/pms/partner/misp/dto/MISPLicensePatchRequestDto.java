package io.mosip.pms.partner.misp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MISPLicensePatchRequestDto {
    private LocalDate expiryDate;
    private String status;
}
