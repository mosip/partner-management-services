package io.mosip.pms.batchjob.repository;

import io.mosip.pms.batchjob.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationServiceRepository extends JpaRepository<Notification, String> {

}
