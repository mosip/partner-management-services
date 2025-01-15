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
        name = "Mapping.ApiKeyRequestsSummaryEntity",
        classes = {
                @ConstructorResult(
                        targetClass = ApiKeyRequestsSummaryEntity.class,
                        columns = {
                                @ColumnResult(name = "apiKeyId", type = String.class),
                                @ColumnResult(name = "partnerId", type = String.class),
                                @ColumnResult(name = "apiKeyLabel", type = String.class),
                                @ColumnResult(name = "orgName", type = String.class),
                                @ColumnResult(name = "policyId", type = String.class),
                                @ColumnResult(name = "policyName", type = String.class),
                                @ColumnResult(name = "policyDescription", type = String.class),
                                @ColumnResult(name = "policyGroupId", type = String.class),
                                @ColumnResult(name = "policyGroupName", type = String.class),
                                @ColumnResult(name = "policyGroupDescription", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "createdDateTime", type = Date.class)
                        }
                )
        }
)
public class ApiKeyRequestsSummaryEntity {

    public ApiKeyRequestsSummaryEntity(
            String apiKeyId, String partnerId, String apiKeyLabel, String orgName, String policyId,
            String policyName, String policyDescription, String policyGroupId, String policyGroupName, String policyGroupDescription,
            String status, Date createdDateTime) {
        this.apiKeyId = apiKeyId;
        this.partnerId = partnerId;
        this.apiKeyLabel = apiKeyLabel;
        this.orgName = orgName;
        this.policyId = policyId;
        this.policyName = policyName;
        this.policyDescription = policyDescription;
        this.policyGroupId = policyGroupId;
        this.policyGroupDescription = policyGroupDescription;
        this.policyGroupName = policyGroupName;
        this.status = status;
        this.createdDateTime = createdDateTime;
    }

    // No-argument constructor
    public ApiKeyRequestsSummaryEntity() {
        super();
    }

    @Id
    private String apiKeyId;

    private String partnerId;

    private String apiKeyLabel;

    private String orgName;

    private String policyId;

    private String policyName;

    private String policyDescription;

    private String policyGroupId;

    private String policyGroupName;

    private String policyGroupDescription;

    private String status;

    private Date createdDateTime;
}
