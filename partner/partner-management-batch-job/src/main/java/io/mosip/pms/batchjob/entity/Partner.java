package io.mosip.pms.batchjob.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name="partner")
@Data
public class Partner {

    @Id
    private String id;

    @Column(name="address")
    private String address;

    @Column(name="contact_no")
    private String contactNo;

    @Column(name="cr_by")
    private String createdBy;

    @Column(name="cr_dtimes")
    private Timestamp createdDatetime;

    @Column(name="del_dtimes")
    private Timestamp deletedDatetime;

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
    private String updatedBy;

    @Column(name="upd_dtimes")
    private Timestamp updatedDatetime;

    @Column(name="user_id")
    private String userId;

    @Column(name = "lang_code")
    private String langCode;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "addl_info")
    private String additionalInfo;
}
