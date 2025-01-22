package io.mosip.pms.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name="partner")
@Data
public class PartnerV3 {

    @Id
    private String id;

    @Column(name="address")
    private String address;

    @Column(name="contact_no")
    private String contactNo;

    @Column(name="cr_by")
    private String crBy;

    @Column(name="cr_dtimes")
    private Timestamp crDtimes;

    @Column(name="del_dtimes")
    private Timestamp delDtimes;

    @Column(name="email_id")
    private String emailId;

    @Column(name="is_active")
    private Boolean isActive;

    @Column(name="is_deleted")
    private Boolean isDeleted;

    @Column(name="name")
    private String name;

    @Column(name="policy_group_id")
    private String policyGroupId;

    @Column(name="certificate_alias")
    private String certificateAlias;

    @Column(name = "partner_type_code")
    private String partnerTypeCode;

    @Column(name="approval_status")
    private String approvalStatus;

    @Column(name="upd_by")
    private String updBy;

    @Column(name="upd_dtimes")
    private Timestamp updDtimes;

    @Column(name="user_id")
    private String userId;

    @Column(name = "lang_code")
    private String langCode;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "addl_info")
    private String additionalInfo;

    @ManyToOne
    @JoinColumn(name = "policy_group_id", insertable = false, updatable = false)
    private PolicyGroup policyGroup;
}
