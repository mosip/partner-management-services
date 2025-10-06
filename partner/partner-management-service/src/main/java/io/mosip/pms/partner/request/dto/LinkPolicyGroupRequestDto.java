package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkPolicyGroupRequestDto {

    @NotBlank
    private String policyGroupId;
}
