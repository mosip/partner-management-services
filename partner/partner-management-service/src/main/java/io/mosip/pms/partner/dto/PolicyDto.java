package io.mosip.pms.partner.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class PolicyDto {
    private String partnerId;
    private String partnerType;
    private String policyGroup;
    private String policyName;
    private Date createDate;
    private String status;
}
