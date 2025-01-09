package io.mosip.pms.common.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@SqlResultSetMapping(
        name = "Mapping.PolicySummaryEntity",
        classes = { @ConstructorResult(
                targetClass = PolicySummaryEntity.class,
                columns = {
                        @ColumnResult(name = "policyId", type = String.class),
                        @ColumnResult(name = "policyName", type = String.class),
                        @ColumnResult(name = "policyDescription", type = String.class),
                        @ColumnResult(name = "policyGroupId", type = String.class),
                        @ColumnResult(name = "policyGroupName", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "createdDateTime", type = Date.class)
                })
        }
)
public class PolicySummaryEntity {

    public PolicySummaryEntity(String policyId, String policyName, String policyDescription,
                               String policyGroupId, String policyGroupName, String status, Date createdDateTime) {
        this.policyId = policyId;
        this.policyName = policyName;
        this.policyDescription = policyDescription;
        this.policyGroupId = policyGroupId;
        this.policyGroupName = policyGroupName;
        this.status = status;
        this.createdDateTime = createdDateTime;
    }

    public PolicySummaryEntity() {
        super();
    }

    @Id
    private String policyId;

    private String policyName;

    private String policyDescription;

    private String policyGroupId;

    private String policyGroupName;

    private String status;

    private Date createdDateTime;
}
