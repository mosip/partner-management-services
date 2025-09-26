package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartnerExistsRequestDto {
    @NotBlank
    private String emailId;

    @NotBlank
    private String partnerId;

    @NotBlank
    private String partnerType;
}
