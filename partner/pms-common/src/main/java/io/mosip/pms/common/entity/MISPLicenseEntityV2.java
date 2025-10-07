package io.mosip.pms.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "misp_license")
public class MISPLicenseEntityV2 implements Serializable {

    private static final long serialVersionUID = -8541947597557590399L;

    @EmbeddedId
    private MISPLicenseEntityPK id;

    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "license_key_name")
    private String licenseKeyName;

    @Column(name = "valid_from_date")
    private LocalDateTime validFromDate;

    @Column(name = "valid_to_date")
    private LocalDateTime validToDate;

    @Column(name = "is_active")
    public Boolean isActive;

    @Column(name = "cr_by", nullable = false, length = 256)
    public String createdBy;

    @Column(name = "cr_dtimes", nullable = false)
    public LocalDateTime createdDateTime;

    @Column(name = "upd_by", length = 256)
    public String updatedBy;

    @Column(name = "upd_dtimes")
    public LocalDateTime updatedDateTime;

    @Column(name = "is_deleted")
    public Boolean isDeleted;

    @Column(name = "del_dtimes")
    public LocalDateTime deletedDateTime;

    @ManyToOne
    @JoinColumn(name="misp_id", insertable = false, updatable = false)
    private PartnerV3 partner;

    @ManyToOne
    @JoinColumn(name = "policy_id", insertable = false, updatable = false)
    private AuthPolicy policy;
}
