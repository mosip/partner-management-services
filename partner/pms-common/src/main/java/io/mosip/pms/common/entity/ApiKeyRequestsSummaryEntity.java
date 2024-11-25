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
                                @ColumnResult(name = "apiKeyName", type = String.class),
                                @ColumnResult(name = "orgName", type = String.class),
                                @ColumnResult(name = "policyName", type = String.class),
                                @ColumnResult(name = "policyGroupName", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "createdDateTime", type = Date.class)
                        }
                )
        }
)
public class ApiKeyRequestsSummaryEntity {

    public ApiKeyRequestsSummaryEntity(
            String apiKeyId, String partnerId, String apiKeyName, String orgName,
            String policyName, String policyGroupName, String status, Date createdDateTime) {
        this.apiKeyId = apiKeyId;
        this.partnerId = partnerId;
        this.apiKeyName = apiKeyName;
        this.orgName = orgName;
        this.policyName = policyName;
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

    private String apiKeyName;

    private String orgName;

    private String policyName;

    private String policyGroupName;

    private String status;

    private Date createdDateTime;
}
