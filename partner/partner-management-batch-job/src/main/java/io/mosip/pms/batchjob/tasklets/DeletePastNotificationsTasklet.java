package io.mosip.pms.batchjob.tasklets;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This Batch Job deletes past notifications based on a configured time threshold.
 */
@Component
public class DeletePastNotificationsTasklet implements org.springframework.batch.core.step.tasklet.Tasklet {

    private final Logger log = LoggerConfiguration.logConfig(DeletePastNotificationsTasklet.class);

    @Autowired
    private NotificationServiceRepository notificationServiceRepository;

    @Value("${mosip.pms.batch.job.past.notifications.deletion.period}")
    private Integer pastNotificationDeletionPeriod;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("DeletePastNotificationsTasklet: START");
        deletePastNotifications();
        log.info("DeletePastNotificationsTasklet: DONE");
        return RepeatStatus.FINISHED;
    }

    private void deletePastNotifications() {
        try {
            LocalDate pastNotificationsDate = LocalDate.now().minusDays(pastNotificationDeletionPeriod);
            log.info("Fetching notifications older than {} days (before {}).", pastNotificationDeletionPeriod, pastNotificationsDate);

            // Fetch past notifications to delete
            List<NotificationEntity> notificationsToDelete = notificationServiceRepository.getPastNotifications(pastNotificationsDate);

            if (notificationsToDelete.isEmpty()) {
                log.info("No old notifications found. Skipping deletion.");
                return;
            }
            log.info("Found {} old notifications. Proceeding with deletion.", notificationsToDelete.size());
            List<NotificationEntity> deletedNotifications = new ArrayList<>();
            notificationsToDelete.forEach(notification -> {
                try {
                    notificationServiceRepository.deleteById(notification.getId());
                    deletedNotifications.add(notification);
                    log.info("Successfully deleted Notification with Id: {}", notification.getId());
                } catch (Exception e) {
                    log.error("Failed to delete notification for notification ID {}: {}", notification.getId(), e.getMessage());
                }
            });

            log.info("Deleted {} notifications.", deletedNotifications.size());
            deletedNotifications.forEach(notification -> log.info("Deleted Notification ID: {}", notification.getId()));

        } catch (Exception e) {
            log.error("Error occurred while deleting past notifications: {}", e.getMessage());
        }
    }
}
