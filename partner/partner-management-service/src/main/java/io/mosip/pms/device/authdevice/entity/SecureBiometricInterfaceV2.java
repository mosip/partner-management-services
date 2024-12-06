package io.mosip.pms.device.authdevice.entity;

import io.mosip.pms.common.entity.PartnerV3;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="secure_biometric_interface")
@Data
public class SecureBiometricInterfaceV2 {
    @Id
    @Column(name="id",length=36,nullable=false)
    private String id;

    @Column(name="sw_binary_hash",nullable=false)
    private byte[] swBinaryHash;

    @Column(name = "sw_version", nullable = false, length = 64)
    private String swVersion;

    @Column(name = "sw_cr_dtimes")
    private LocalDateTime swCreateDateTime;

    @Column(name = "sw_expiry_dtimes")
    private LocalDateTime swExpiryDateTime;

    @Column(name="approval_status",length=36,nullable=false)
    private String approvalStatus;

    @Column(name="is_active",nullable=false)
    private Boolean isActive;

    @Column(name="is_deleted")
    private Boolean isDeleted;

    @Column(name="cr_by",length=256,nullable=false)
    private String crBy;

    @Column(name="cr_dtimes",nullable=false)
    private LocalDateTime crDtimes;

    @Column(name="del_dtimes")
    private LocalDateTime delDtimes;

    @Column(name="upd_by",length=256)
    private String updBy;

    @Column(name="upd_dtimes")
    private LocalDateTime updDtimes;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name="partner_org_name")
    private String partnerOrgName;

    @ManyToOne
    @JoinColumn(name="provider_id", insertable = false, updatable = false)
    private PartnerV3 partner;
}
