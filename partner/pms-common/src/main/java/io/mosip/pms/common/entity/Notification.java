package io.mosip.pms.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "notifications")
@Data
public class Notification {

    @Id
    private String id;

    @Column(name = "partner_id")
    private String partnerId;

    @Column(name = "notification_type")
    private String notificationType;

    @Column(name = "notification_status")
    private String notificationStatus;

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

    @Column(name = "notification_details_json")
    private String notificationDetailsJson;

}
