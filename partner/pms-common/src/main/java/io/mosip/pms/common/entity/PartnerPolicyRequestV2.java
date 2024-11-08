package io.mosip.pms.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name="partner_policy_request")
@Data
public class PartnerPolicyRequestV2 {

    @Id
    private String id;

    @Column(name="policy_id")
    private String policyId;

    @Column(name="part_id")
    private String partnerId;

    @Column(name="request_datetimes")
    private Timestamp requestDateTime;

    @Column(name="request_detail")
    private String requestDetail;

    @Column(name="cr_by")
    private String createdBy;

    @Column(name="cr_dtimes")
    private Timestamp createdDateTime;

    @Column(name="del_dtimes")
    private Timestamp deletedDateTime;

    @Column(name="is_deleted")
    private Boolean isDeleted;

    @Column(name="status_code")
    private String statusCode;

    @Column(name="upd_by")
    private String updatedBy;

    @Column(name="upd_dtimes")
    private Timestamp updatedDateTime;

    @ManyToOne
    @JoinColumn(name="part_id", insertable = false, updatable = false)
    private PartnerV3 partner;

    @ManyToOne
    @JoinColumn(name = "policy_id", insertable = false, updatable = false)
    private AuthPolicy policy;
}
