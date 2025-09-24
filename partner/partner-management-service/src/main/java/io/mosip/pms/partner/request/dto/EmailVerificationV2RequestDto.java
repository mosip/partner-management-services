package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailVerificationV2RequestDto {
    @NotBlank
    private String emailId;

    private String partnerId;
    
    private String partnerType;
}
