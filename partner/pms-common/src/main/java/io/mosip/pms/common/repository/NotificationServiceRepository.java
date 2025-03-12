package io.mosip.pms.common.repository;

import io.mosip.pms.common.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationServiceRepository extends JpaRepository<NotificationEntity, String> {

}
