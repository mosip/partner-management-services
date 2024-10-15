package io.mosip.pms.common.entity;

import java.sql.Timestamp;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import org.hibernate.annotations.NamedNativeQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
                        @ColumnResult(name = "policyGroupName", type = String.class),
                        @ColumnResult(name = "emailAddress", type = String.class),
                        @ColumnResult(name = "certificateUploadStatus", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "isActive", type = Boolean.class),
                        @ColumnResult(name = "createdDateTime", type = Timestamp.class)
                })
        }
)
@NamedNativeQuery(
        name = "PartnerSummaryEntity.getSummaryOfAllPartners",
        resultClass = PartnerSummaryEntity.class,
        query = "SELECT p.id AS partnerId, "
                + "p.partner_type_code AS partnerType, "
                + "p.name AS orgName, "
                + "pg.name AS policyGroupName, "
                + "p.email_id AS emailAddress, "
                + "CASE WHEN p.certificate_alias IS NULL THEN 'not_uploaded' ELSE 'uploaded' END AS certificateUploadStatus, "
                + "CASE WHEN p.is_active = TRUE THEN 'activated' ELSE 'deactivated' END AS status, "
                + "p.is_active AS isActive, "
                + "p.cr_dtimes AS createdDateTime "
                + "FROM pms.partner p "
                + "LEFT JOIN pms.policy_group pg ON p.policy_group_id = pg.id"
)
public class PartnerSummaryEntity {

    public PartnerSummaryEntity(
            String partnerId, String partnerType, String orgName,
            String policyGroupName, String emailAddress,
            String certificateUploadStatus, String status, Boolean isActive,
            Timestamp createdDateTime) {
        super();
        this.partnerId = partnerId;
        this.partnerType = partnerType;
        this.orgName = orgName;
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

    private String policyGroupName;

    private String emailAddress;

    private String certificateUploadStatus;

    private String status;

    private Boolean isActive;

    private Timestamp createdDateTime;
}
