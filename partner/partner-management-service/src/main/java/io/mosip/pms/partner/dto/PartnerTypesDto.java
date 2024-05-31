package io.mosip.pms.partner.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PartnerTypesDto {
    private String partnerId;
    private String partnerType;
    private String policyGroupId;
    private String policyGroupName;
    private String policyGroupDescription;
}
