package io.mosip.pms.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@SqlResultSetMapping(
        name = "Mapping.MISPLicenseSummaryEntity",
        classes = { @ConstructorResult(
                targetClass = MISPLicenseSummaryEntity.class,
                columns = {
                        @ColumnResult(name = "partnerId", type = String.class),
                        @ColumnResult(name = "orgName", type = String.class),
                        @ColumnResult(name = "policyGroupId", type = String.class),
                        @ColumnResult(name = "policyGroupName", type = String.class),
                        @ColumnResult(name = "policyGroupDescription", type = String.class),
                        @ColumnResult(name = "policyId", type = String.class),
                        @ColumnResult(name = "policyName", type = String.class),
                        @ColumnResult(name = "policyDescription", type = String.class),
                        @ColumnResult(name = "mispLicenseKeyName", type = String.class),
                        @ColumnResult(name = "mispLicenseKey", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "createdDateTime", type = LocalDateTime.class),
                        @ColumnResult(name = "expiryDateTime", type = LocalDateTime.class),
                })
        }
)
public class MISPLicenseSummaryEntity {
    public MISPLicenseSummaryEntity(String partnerId, String orgName, String policyGroupId, String policyGroupName,
                                   String policyGroupDescription, String policyId, String policyName,
                                   String policyDescription, String mispLicenseKeyName, String mispLicenseKey, String status,
                                    LocalDateTime createdDateTime, LocalDateTime expiryDateTime) {
        this.partnerId = partnerId;
        this.orgName = orgName;
        this.policyGroupId = policyGroupId;
        this.policyGroupName = policyGroupName;
        this.policyGroupDescription = policyGroupDescription;
        this.policyId = policyId;
        this.policyName = policyName;
        this.policyDescription = policyDescription;
        this.mispLicenseKeyName = mispLicenseKeyName;
        this.mispLicenseKey = mispLicenseKey;
        this.status = status;
        this.createdDateTime = createdDateTime;
        this.expiryDateTime = expiryDateTime;
    }

    // No-argument constructor
    public MISPLicenseSummaryEntity() {
        super();
    }

    @Id
    private String partnerId;

    private String orgName;

    private String policyGroupId;

    private String policyGroupName;

    private String policyGroupDescription;

    private String policyId;

    private String policyName;

    private String policyDescription;

    private String mispLicenseKeyName;

    private String mispLicenseKey;

    private String status;

    private LocalDateTime createdDateTime;

    private LocalDateTime expiryDateTime;
}
