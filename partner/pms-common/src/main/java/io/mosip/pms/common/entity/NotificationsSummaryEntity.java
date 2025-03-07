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
        name = "Mapping.NotificationsSummaryEntity",
        classes = { @ConstructorResult(
                targetClass = NotificationsSummaryEntity.class,
                columns = {
                        @ColumnResult(name = "notificationId", type = String.class),
                        @ColumnResult(name = "notificationPartnerId", type = String.class),
                        @ColumnResult(name = "notificationType", type = String.class),
                        @ColumnResult(name = "notificationStatus", type = String.class),
                        @ColumnResult(name = "createdDateTime", type = LocalDateTime.class),
                        @ColumnResult(name = "notificationDetails", type = String.class)
                })
        }
)
public class NotificationsSummaryEntity {

    public NotificationsSummaryEntity(String notificationId, String notificationPartnerId, String notificationType,
                                      String notificationStatus, LocalDateTime createdDateTime, String notificationDetails) {
        this.notificationId = notificationId;
        this.notificationPartnerId = notificationPartnerId;
        this.notificationType = notificationType;
        this.notificationStatus = notificationStatus;
        this.createdDateTime = createdDateTime;
        this.notificationDetails = notificationDetails;
    }

    public NotificationsSummaryEntity() {
        super();
    }

    @Id
    private String notificationId;

    private String notificationPartnerId;

    private String notificationType;

    private String notificationStatus;

    private LocalDateTime createdDateTime;

    private String notificationDetails;
}
