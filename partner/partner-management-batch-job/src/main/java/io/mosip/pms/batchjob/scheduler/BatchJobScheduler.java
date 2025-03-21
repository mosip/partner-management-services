/* 
 * Copyright
 * 
 */
package io.mosip.pms.batchjob.scheduler;

import org.slf4j.Logger;
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

import io.mosip.pms.batchjob.config.LoggerConfiguration;

/**
 * This class is a job scheduler of batch in which jobs are getting executed
 * based on cron expressions
 */
@RefreshScope
@Component
@EnableScheduling
public class BatchJobScheduler {

	private Logger log = LoggerConfiguration.logConfig(BatchJobScheduler.class);

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

	@Scheduled(cron = "${mosip.pms.batch.job.root.cert.expiry.cron.schedule}")
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

}
