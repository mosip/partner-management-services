package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.NotificationsSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationsSummaryRepository extends BaseRepository<NotificationsSummaryEntity, String> {

    @Query(value = "SELECT new NotificationsSummaryEntity(" +
            "n.id, n.partnerId, n.notificationType, n.notificationStatus, n.createdDatetime as crDtimes, n.notificationDetailsJson) " +
            "FROM NotificationEntity n " +
            "WHERE (:filterBy IS NULL OR lower(n.notificationDetailsJson) LIKE %:filterBy%) " +
            "AND (:notificationStatus IS NULL OR lower(n.notificationStatus) LIKE %:notificationStatus%) " +
            "AND (:notificationType IS NULL OR lower(n.notificationType) LIKE %:notificationType%) " +
            "AND (n.partnerId IN :partnerIdList) " +
            "ORDER BY crDtimes DESC"
    )
    Page<NotificationsSummaryEntity> getSummaryOfAllNotifications(
            @Param("filterBy") String filterBy,
            @Param("notificationStatus") String notificationStatus,
            @Param("notificationType") String notificationType,
            @Param("partnerIdList") List<String> partnerIdList,
            Pageable pageable
    );
}
