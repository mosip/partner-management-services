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
    private String policyName;
    private Date createDate;
    private String status;
    private String policyGroupId;
    private String policyGroupDescription;
    private String policyGroupName;
    private String policyId;
    private String policyDescription;
    private String partnerComments;
    private Date updDtimes;
}