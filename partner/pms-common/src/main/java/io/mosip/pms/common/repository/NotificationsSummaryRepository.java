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
            "WHERE (:notificationStatus IS NULL OR LOWER(n.notification_status) = LOWER(:notificationStatus)) " +
            "AND (n.partner_id IN (:partnerIdList)) " +
            "AND (:certificateId IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'certificateId') ILIKE CONCAT('%', :certificateId, '%')) " +
            "AND (:issuedBy IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedBy') ILIKE CONCAT('%', :issuedBy, '%')) " +
            "AND (:issuedTo IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedTo') ILIKE CONCAT('%', :issuedTo, '%')) " +
            "AND (:partnerDomain IS NULL OR LOWER(CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'partnerDomain') = LOWER(:partnerDomain)) " +
            "AND (:expiryDateTime IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'expiryDateTime') ILIKE CONCAT('%', :expiryDateTime, '%')) " +
            "ORDER BY n.cr_dtimes DESC",

            countQuery = "SELECT COUNT(*) " +
                    "FROM notifications n " +
                    "WHERE (:notificationStatus IS NULL OR LOWER(n.notification_status) = LOWER(:notificationStatus)) " +
                    "AND (n.partner_id IN (:partnerIdList)) " +
                    "AND (:certificateId IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'certificateId') ILIKE CONCAT('%', :certificateId, '%')) " +
                    "AND (:issuedBy IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedBy') ILIKE CONCAT('%', :issuedBy, '%')) " +
                    "AND (:issuedTo IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedTo') ILIKE CONCAT('%', :issuedTo, '%')) " +
                    "AND (:partnerDomain IS NULL OR LOWER(CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'partnerDomain') = LOWER(:partnerDomain)) " +
                    "AND (:expiryDateTime IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'expiryDateTime') ILIKE CONCAT('%', :expiryDateTime, '%')) ",
            nativeQuery = true)
    Page<NotificationEntity> getSummaryOfAllNotifications(
            @Param("certificateId") String certificateId,
            @Param("issuedBy") String issuedBy,
            @Param("issuedTo") String issuedTo,
            @Param("partnerDomain") String partnerDomain,
            @Param("expiryDateTime") String expiryDateTime,
            @Param("notificationStatus") String notificationStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM notifications n " +
            "WHERE (:notificationStatus IS NULL OR LOWER(n.notification_status) = LOWER(:notificationStatus)) " +
            "AND (:notificationType IS NULL OR LOWER(n.notification_type) = LOWER(:notificationType)) " +
            "AND (n.partner_id IN (:partnerIdList)) " +
            "AND (:certificateId IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'certificateId') ILIKE CONCAT('%', :certificateId, '%')) " +
            "AND (:issuedBy IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedBy') ILIKE CONCAT('%', :issuedBy, '%')) " +
            "AND (:issuedTo IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedTo') ILIKE CONCAT('%', :issuedTo, '%')) " +
            "AND (:partnerDomain IS NULL OR LOWER(CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'partnerDomain') = LOWER(:partnerDomain)) " +
            "AND (:expiryDateTime IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'expiryDateTime') ILIKE CONCAT('%', :expiryDateTime, '%')) " +
            "ORDER BY n.cr_dtimes DESC",

            countQuery = "SELECT COUNT(*) " +
                    "FROM notifications n " +
                    "WHERE (:notificationStatus IS NULL OR LOWER(n.notification_status) = LOWER(:notificationStatus)) " +
                    "AND (:notificationType IS NULL OR LOWER(n.notification_type) = LOWER(:notificationType)) " +
                    "AND (n.partner_id IN (:partnerIdList)) " +
                    "AND (:certificateId IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'certificateId') ILIKE CONCAT('%', :certificateId, '%')) " +
                    "AND (:issuedBy IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedBy') ILIKE CONCAT('%', :issuedBy, '%')) " +
                    "AND (:issuedTo IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'issuedTo') ILIKE CONCAT('%', :issuedTo, '%')) " +
                    "AND (:partnerDomain IS NULL OR LOWER(CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'partnerDomain') = LOWER(:partnerDomain)) " +
                    "AND (:expiryDateTime IS NULL OR (CAST(n.notification_details_json AS JSONB)->'certificateDetails'->0->>'expiryDateTime') ILIKE CONCAT('%', :expiryDateTime, '%')) ",
                    nativeQuery = true)
    Page<NotificationEntity> getSummaryOfAllRootAndIntermediateNotifications(
            @Param("certificateId") String certificateId,
            @Param("issuedBy") String issuedBy,
            @Param("issuedTo") String issuedTo,
            @Param("partnerDomain") String partnerDomain,
            @Param("expiryDateTime") String expiryDateTime,
            @Param("notificationStatus") String notificationStatus,
            @Param("notificationType") String notificationType,
            @Param("partnerIdList") List<String> partnerIdList,
            Pageable pageable);

}
