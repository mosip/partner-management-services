package io.mosip.pms.common.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name="partner_policy")
public class PartnerPolicyV2 {

    @Id
    @Column(name="policy_api_key")
    private String apiKeyId;

    @Column(name="part_id")
    private String partnerId;

    @Column(name="cr_by")
    private String createdBy;

    @Column(name="cr_dtimes")
    private Timestamp createdDateTime;

    @Column(name="del_dtimes")
    private Timestamp deletedDateTime;

    @Column(name="is_active")
    private Boolean isActive;

    @Column(name="is_deleted")
    private Boolean isDeleted;

    @Column(name="policy_id")
    private String policyId;

    @Column(name="upd_by")
    private String updatedBy;

    @Column(name="upd_dtimes")
    private Timestamp updatedDateTime;

    @Column(name="valid_from_datetime")
    private Timestamp validFromDatetime;

    @Column(name="valid_to_datetime")
    private Timestamp validToDatetime;

    @Column(name = "label")
    private String label;

    @ManyToOne
    @JoinColumn(name="part_id", insertable = false, updatable = false)
    private PartnerV3 partner;

    @ManyToOne
    @JoinColumn(name = "policy_id", insertable = false, updatable = false)
    private AuthPolicy policy;
}