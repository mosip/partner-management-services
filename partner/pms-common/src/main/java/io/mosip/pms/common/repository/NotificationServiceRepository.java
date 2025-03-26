package io.mosip.pms.common.repository;

import io.mosip.pms.common.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NotificationServiceRepository extends JpaRepository<NotificationEntity, String> {

    @Query("SELECT n FROM NotificationEntity n WHERE DATE(n.createdDatetime) < DATE(:notificationExpiryDate)")
    List<NotificationEntity> getPastNotifications(@Param("notificationExpiryDate") LocalDate notificationExpiryDate);

}
