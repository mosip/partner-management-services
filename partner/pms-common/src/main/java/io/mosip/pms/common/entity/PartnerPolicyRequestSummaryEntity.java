package io.mosip.pms.common.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@SqlResultSetMapping(
        name = "Mapping.PartnerPolicyRequestSummaryEntity",
        classes = { @ConstructorResult(
                targetClass = PartnerPolicyRequestSummaryEntity.class,
                columns = {
                        @ColumnResult(name = "id", type = String.class),
                        @ColumnResult(name = "partnerId", type = String.class),
                        @ColumnResult(name = "partnerStatus", type = String.class),
                        @ColumnResult(name = "partnerType", type = String.class),
                        @ColumnResult(name = "policyGroupId", type = String.class),
                        @ColumnResult(name = "policyGroupName", type = String.class),
                        @ColumnResult(name = "orgName", type = String.class),
                        @ColumnResult(name = "policyName", type = String.class),
                        @ColumnResult(name = "policyId", type = String.class),
                        @ColumnResult(name = "partnerComment", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "createdDateTime", type = Date.class),
                        @ColumnResult(name = "updatedDateTime", type = Date.class),
                        @ColumnResult(name = "policyDescription", type = String.class),
                        @ColumnResult(name = "policyGroupDescription", type = String.class)
                })
        }
)
public class PartnerPolicyRequestSummaryEntity {

    public PartnerPolicyRequestSummaryEntity(
            String id, String partnerId, String partnerStatus, String orgName, String partnerType,
            String policyGroupId, String policyGroupName, String policyId, String policyName, String status,
            Date createdDateTime, String partnerComment, Date updatedDateTime,
            String policyDescription, String policyGroupDescription) {
        this.id = id;
        this.partnerId = partnerId;
        this.partnerStatus = partnerStatus;
        this.partnerType = partnerType;
        this.orgName = orgName;
        this.policyId = policyId;
        this.policyGroupId = policyGroupId;
        this.policyGroupName = policyGroupName;
        this.policyName = policyName;
        this.partnerComment = partnerComment;
        this.status = status;
        this.createdDateTime = createdDateTime;
        this.updatedDateTime = updatedDateTime;
        this.policyDescription = policyDescription;
        this.policyGroupDescription = policyGroupDescription;
    }

    // No-argument constructor
    public PartnerPolicyRequestSummaryEntity() {
        super();
    }

    @Id
    private String id;

    private String partnerId;

    private String partnerStatus;

    private String partnerType;

    private String orgName;

    private String policyId;

    private String policyGroupId;

    private String policyGroupName;

    private String policyName;

    private String partnerComment;

    private String status;

    private Date createdDateTime;

    private Date updatedDateTime;

    private String policyDescription;

    private String policyGroupDescription;
}
