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
                        @ColumnResult(name = "oidcClientId", type = String.class),
                        @ColumnResult(name = "oidcClientName", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "createdDateTime", type = Date.class),
                })
        }
)
public class ClientSummaryEntity {

    public ClientSummaryEntity(String partnerId, String orgName, String policyGroupName, String policyGroupDescription,
                               String policyName, String policyDescription, String oidcClientId,
                               String oidcClientName, String status, Date createdDateTime) {

        this.partnerId = partnerId;
        this.orgName = orgName;
        this.policyGroupName = policyGroupName;
        this.policyGroupDescription= policyGroupDescription;
        this.policyName = policyName;
        this.policyDescription = policyDescription;
        this.oidcClientId = oidcClientId;
        this.oidcClientName = oidcClientName;
        this.status = status;
        this.createdDateTime = createdDateTime;
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
    private String oidcClientId;

    private String oidcClientName;

    private String status;

    private Date createdDateTime;
}
