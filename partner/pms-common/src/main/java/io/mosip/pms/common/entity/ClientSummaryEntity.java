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
        name = "Mapping.ClientSummaryEntity",
        classes = { @ConstructorResult(
                targetClass = ClientSummaryEntity.class,
                columns = {
                        @ColumnResult(name = "partnerId", type = String.class),
                        @ColumnResult(name = "orgName", type = String.class),
                        @ColumnResult(name = "policyGroupName", type = String.class),
                        @ColumnResult(name = "policyGroupDescription", type = String.class),
                        @ColumnResult(name = "policyName", type = String.class),
                        @ColumnResult(name = "policyDescription", type = String.class),
                        @ColumnResult(name = "clientId", type = String.class),
                        @ColumnResult(name = "clientName", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "createdDateTime", type = Date.class),
                        @ColumnResult(name = "policyGroupId", type = String.class),
                        @ColumnResult(name = "updatedDateTime", type = Date.class)
                })
        }
)
public class ClientSummaryEntity {

    public ClientSummaryEntity(String partnerId, String orgName, String policyGroupName, String policyGroupDescription,
                               String policyName, String policyDescription, String clientId,
                               String clientName, String status, Date createdDateTime, String policyGroupId,
                               Date updatedDateTime) {

        this.partnerId = partnerId;
        this.orgName = orgName;
        this.policyGroupName = policyGroupName;
        this.policyGroupDescription = policyGroupDescription;
        this.policyName = policyName;
        this.policyDescription = policyDescription;
        this.clientId = clientId;
        this.clientName = clientName;
        this.status = status;
        this.createdDateTime = createdDateTime;
        this.policyGroupId = policyGroupId;
        this.updatedDateTime = updatedDateTime;
    }

    // No-argument constructor
    public ClientSummaryEntity() {
        super();
    }

    private String partnerId;

    private String orgName;

    private String policyGroupName;

    private String policyGroupDescription;

    private String policyName;

    private String policyDescription;

    @Id
    private String clientId;

    private String clientName;

    private String status;

    private Date createdDateTime;

    private String policyGroupId;

    private Date updatedDateTime;
}
