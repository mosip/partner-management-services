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
        name = "Mapping.PartnerSummaryEntity",
        classes = { @ConstructorResult(
                targetClass = PartnerSummaryEntity.class,
                columns = {
                        @ColumnResult(name = "partnerId", type = String.class),
                        @ColumnResult(name = "partnerType", type = String.class),
                        @ColumnResult(name = "orgName", type = String.class),
                        @ColumnResult(name = "policyGroupId", type = String.class),
                        @ColumnResult(name = "policyGroupName", type = String.class),
                        @ColumnResult(name = "emailAddress", type = String.class),
                        @ColumnResult(name = "certificateUploadStatus", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "isActive", type = Boolean.class),
                        @ColumnResult(name = "createdDateTime", type = Date.class)
                })
        }
)
public class PartnerSummaryEntity {

    public PartnerSummaryEntity(
            String partnerId, String partnerType, String orgName, String policyGroupId,
            String policyGroupName, String emailAddress,
            String certificateUploadStatus, String status, Boolean isActive,
            Date createdDateTime) {
        this.partnerId = partnerId;
        this.partnerType = partnerType;
        this.orgName = orgName;
        this.policyGroupId = policyGroupId;
        this.policyGroupName = policyGroupName;
        this.emailAddress = emailAddress;
        this.certificateUploadStatus = certificateUploadStatus;
        this.status = status;
        this.isActive = isActive;
        this.createdDateTime = createdDateTime;
    }

    // No-argument constructor
    public PartnerSummaryEntity() {
        super();
    }

    @Id
    private String partnerId;

    private String partnerType;

    private String orgName;

    private String policyGroupId;

    private String policyGroupName;

    private String emailAddress;

    private String certificateUploadStatus;

    private String status;

    private Boolean isActive;

    private Date createdDateTime;
}
