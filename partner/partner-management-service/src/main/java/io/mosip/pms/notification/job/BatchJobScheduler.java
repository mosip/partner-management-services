/* 
 * Copyright
 * 
 */
package io.mosip.pms.notification.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;

/**
 * This class is a job scheduler of batch in which jobs are getting executed
 * based on cron expressions
 */
@RefreshScope
@Component
@EnableScheduling
public class BatchJobScheduler {

	private Logger log = PMSLogger.getLogger(BatchJobScheduler.class);

	private static final String LOGDISPLAY = "{} - {} - {}";

	private static final String JOB_STATUS = "Job's status";

	@Autowired
	private JobLauncher jobLauncher;

	@Qualifier("rootAndIntermediateCertificateExpiryJob")
	@Autowired
	private Job rootAndIntermediateCertificateExpiryJob;

	@Qualifier("partnerCertificateExpiryJob")
	@Autowired
	private Job partnerCertificateExpiryJob;

	@Qualifier("weeklyNotificationsJob")
	@Autowired
	private Job weeklyNotificationsJob;

	@Qualifier("deletePastNotificationsJob")
	@Autowired
	private Job deletePastNotificationsJob;

	@Qualifier("ftmChipExpiryNotificationsJob")
	@Autowired
	private Job ftmChipExpiryNotificationsJob;

	@Qualifier("sbiExpiryNotificationsJob")
	@Autowired
	private Job sbiExpiryNotificationsJob;

	@Qualifier("apiKeyExpiryNotificationsJob")
	@Autowired
	private Job apiKeyExpiryNotificationsJob;

	@Qualifier("sbiExpiryAutoDeactivationJob")
	@Autowired
	private Job sbiExpiryAutoDeactivationJob;

	@Qualifier("apiKeyExpiryAutoDeactivationJob")
	@Autowired
	private Job apiKeyExpiryAutoDeactivationJob;

	@Scheduled(cron = "${mosip.pms.batch.job.root.intermediate.cert.expiry.cron.schedule}")
	public void rootCertificateExpiryScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(rootAndIntermediateCertificateExpiryJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "RootAndIntermediateCertificateExpiryJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.partner.cert.expiry.cron.schedule}")
	public void partnerCertificateExpiryScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(partnerCertificateExpiryJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "PartnerCertificateExpiryJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.delete.past.notifications.cron.schedule}")
	public void deletePastNotificationScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(deletePastNotificationsJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "deletePastNotificationsJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.weekly.notifications.cron.schedule}")
	public void weeklyNotificationScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(weeklyNotificationsJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "weeklyNotificationsJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.ftm.chip.expiry.notifications.cron.schedule}")
	public void ftmChipExpiryNotificationsScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(ftmChipExpiryNotificationsJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "ftmChipExpiryNotificationsJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.sbi.expiry.notifications.cron.schedule}")
	public void sbiExpiryNotificationsScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(sbiExpiryNotificationsJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "sbiExpiryNotificationsJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.api.key.expiry.notifications.cron.schedule}")
	public void apiKeyExpiryNotificationsScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(apiKeyExpiryNotificationsJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "apiKeyExpiryNotificationsJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.sbi.expiry.auto.deactivation.cron.schedule}")
	public void sbiExpiryAutoDeactivationScheduler() {

		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(sbiExpiryAutoDeactivationJob, jobParam);

			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());

		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				 | JobParametersInvalidException e) {

			log.error(LOGDISPLAY, "sbiExpiryAutoDeactivationJob failed", e.getMessage(), null);
		}
	}

	@Scheduled(cron = "${mosip.pms.batch.job.api.key.expiry.auto.deactivation.cron.schedule}")
	public void apiKeyExpiryAutoDeactivationScheduler() {
		JobParameters jobParam = new JobParametersBuilder().addLong("updateStatusTime", System.currentTimeMillis())
				.toJobParameters();
		try {
			JobExecution jobExecution = jobLauncher.run(apiKeyExpiryAutoDeactivationJob, jobParam);
			log.info(LOGDISPLAY, JOB_STATUS, jobExecution.getId().toString(), jobExecution.getStatus().toString());
		} catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
				| JobParametersInvalidException e) {
			log.error(LOGDISPLAY, "ApiKeyExpiryAutoDeactivationJob failed", e.getMessage(), null);
		}
	}

}
