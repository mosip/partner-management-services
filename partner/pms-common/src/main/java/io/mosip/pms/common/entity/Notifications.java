package io.mosip.pms.common.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "notifications")
@Data
public class Notifications {

    @Id
    private String id;

    @Column(name = "partner_id")
    private String partnerId;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "notification_status")
    private String notificationStatus;

    @Column(name = "notification_details_json")
    private String notificationDetailsJson;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "email_lang_code")
    private String emailLangCode;

    @Column(name = "email_sent")
    private Boolean emailSent;

    @Column(name = "email_sent_dtimes")
    private LocalDateTime emailSentDatetime;

    @Column(name = "cr_by")
    private String createdBy;

    @Column(name = "cr_dtimes")
    private LocalDateTime createdDatetime;

    @Column(name = "upd_by")
    private String updatedBy;

    @Column(name = "upd_dtimes")
    private LocalDateTime updatedDatetime;

    @ManyToOne
    @JoinColumn(name="partner_id", insertable = false, updatable = false)
    private PartnerV3 partner;

}
