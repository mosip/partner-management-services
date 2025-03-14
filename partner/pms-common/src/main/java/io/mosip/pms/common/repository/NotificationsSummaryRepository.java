package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsSummaryRepository extends BaseRepository<NotificationEntity, String> {

    @Query(value = "SELECT * " +
            "FROM notifications n " +
            "CROSS JOIN LATERAL jsonb_each(CAST(n.notification_details_json AS jsonb)) AS obj_keys " +
            "WHERE jsonb_typeof(obj_keys.value) = 'array' " +
            "AND EXISTS ( " +
            "    SELECT 1 " +
            "    FROM jsonb_array_elements(obj_keys.value) AS nested_obj " +
            "    CROSS JOIN LATERAL jsonb_each_text(nested_obj) AS fields " +
            "    WHERE LOWER(fields.value) LIKE LOWER(CONCAT('%', :filterBy, '%')) " +
            ") " +
            "AND (:notificationStatus IS NULL OR LOWER(n.notification_status) LIKE LOWER(CONCAT('%', :notificationStatus, '%'))) " +
            "AND (:notificationType IS NULL OR n.notification_type LIKE CONCAT('%', :notificationType, '%')) " +
            "AND (COALESCE(:partnerIdList) IS NULL OR n.partner_id IN (:partnerIdList)) " +
            "ORDER BY n.cr_dtimes DESC",
            countQuery = "SELECT COUNT(*) " +
                    "FROM notifications n " +
                    "CROSS JOIN LATERAL jsonb_each(CAST(n.notification_details_json AS jsonb)) AS obj_keys " +
                    "WHERE jsonb_typeof(obj_keys.value) = 'array' " +
                    "AND EXISTS ( " +
                    "    SELECT 1 " +
                    "    FROM jsonb_array_elements(obj_keys.value) AS nested_obj " +
                    "    CROSS JOIN LATERAL jsonb_each_text(nested_obj) AS fields " +
                    "    WHERE LOWER(fields.value) LIKE LOWER(CONCAT('%', :filterBy, '%')) " +
                    ") " +
                    "AND (:notificationStatus IS NULL OR LOWER(n.notification_status) LIKE LOWER(CONCAT('%', :notificationStatus, '%'))) " +
                    "AND (:notificationType IS NULL OR n.notification_type LIKE CONCAT('%', :notificationType, '%')) " +
                    "AND (COALESCE(:partnerIdList) IS NULL OR n.partner_id IN (:partnerIdList))",
            nativeQuery = true)
    Page<NotificationEntity> getSummaryOfAllNotifications(
            @Param("filterBy") String filterBy,
            @Param("notificationStatus") String notificationStatus,
            @Param("notificationType") String notificationType,
            @Param("partnerIdList") List<String> partnerIdList,
            Pageable pageable);

}
