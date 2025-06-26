/* 
 * Copyright
 * 
 */
package io.mosip.pms.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import io.mosip.pms.tasklets.ApiKeyExpiryTasklet;
import io.mosip.pms.tasklets.DeletePastNotificationsTasklet;
import io.mosip.pms.tasklets.FTMChipCertificateExpiryTasklet;
import io.mosip.pms.tasklets.PartnerCertificateExpiryTasklet;
import io.mosip.pms.tasklets.RootAndIntermediateCertificateExpiryTasklet;
import io.mosip.pms.tasklets.SbiExpiryTasklet;
import io.mosip.pms.tasklets.WeeklyNotificationsTasklet;
import io.mosip.pms.tasklets.SbiExpiryAutoDeactivationTasklet;

@Configuration
public class BatchJobConfig {

	@Autowired
	private RootAndIntermediateCertificateExpiryTasklet rootAndIntermediateCertificateExpiryTasklet;

	@Autowired
	private PartnerCertificateExpiryTasklet partnerCertificateExpiryTasklet;

	@Autowired
	private WeeklyNotificationsTasklet weeklyNotificationsTasklet;

	@Autowired
	private DeletePastNotificationsTasklet deletePastNotificationsTasklet;

	@Autowired
	private FTMChipCertificateExpiryTasklet ftmChipCertificateExpiryTasklet;

	@Autowired
	private SbiExpiryTasklet sbiExpiryTasklet;

	@Autowired
	private ApiKeyExpiryTasklet apiKeyExpiryTasklet;

	@Autowired
	private SbiExpiryAutoDeactivationTasklet sbiExpiryAutoDeactivationTasklet;

	@Bean
	public Step rootAndIntermediateCertificateExpiryStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("rootAndIntermediateCertificateExpiryStep", jobRepository)
				.tasklet(rootAndIntermediateCertificateExpiryTasklet, transactionManager).build();
	}

	@Bean
	public Step partnerCertificateExpiryStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("partnerCertificateExpiryStep", jobRepository)
				.tasklet(partnerCertificateExpiryTasklet, transactionManager).build();
	}

	@Bean
	public Step weeklyNotificationsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("weeklyNotificationsStep", jobRepository)
				.tasklet(weeklyNotificationsTasklet, transactionManager).build();
	}

	@Bean
	public Step deletePastNotificationsStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("deletePastNotificationsStep", jobRepository)
				.tasklet(deletePastNotificationsTasklet, transactionManager).build();
	}

	@Bean
	public Step ftmChipExpiryNotificationsStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("ftmChipExpiryNotificationsStep", jobRepository)
				.tasklet(ftmChipCertificateExpiryTasklet, transactionManager).build();
	}

	@Bean
	public Step sbiExpiryNotificationsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("sbiExpiryNotificationsStep", jobRepository)
				.tasklet(sbiExpiryTasklet, transactionManager).build();
	}

	@Bean
	public Step apiKeyExpiryNotificationsStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager) {
		return new StepBuilder("apiKeyExpiryNotificationsStep", jobRepository)
				.tasklet(apiKeyExpiryTasklet, transactionManager).build();
	}

	@Bean
	public Step sbiExpiryAutoDeactivationStep(JobRepository jobRepository,
											  PlatformTransactionManager transactionManager) {
		return new StepBuilder("sbiExpiryAutoDeactivationStep", jobRepository)
				.tasklet(sbiExpiryAutoDeactivationTasklet, transactionManager).build();
	}

	@Bean
	public Job rootAndIntermediateCertificateExpiryJob(JobRepository jobRepository,
			@Qualifier("rootAndIntermediateCertificateExpiryStep") Step rootAndIntermediateCertificateExpiryStep) {
		return new JobBuilder("rootAndIntermediateCertificateExpiryJob", jobRepository)
				.incrementer(new RunIdIncrementer()).start(rootAndIntermediateCertificateExpiryStep).build();
	}

	@Bean
	public Job partnerCertificateExpiryJob(JobRepository jobRepository,
			@Qualifier("partnerCertificateExpiryStep") Step partnerCertificateExpiryStep) {
		return new JobBuilder("partnerCertificateExpiryJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(partnerCertificateExpiryStep).build();
	}

	@Bean
	public Job weeklyNotificationsJob(JobRepository jobRepository,
			@Qualifier("weeklyNotificationsStep") Step weeklyNotificationsStep) {
		return new JobBuilder("weeklyNotificationsJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(weeklyNotificationsStep).build();
	}

	@Bean
	public Job deletePastNotificationsJob(JobRepository jobRepository,
			@Qualifier("deletePastNotificationsStep") Step deletePastNotificationsStep) {
		return new JobBuilder("deletePastNotificationsStep", jobRepository).incrementer(new RunIdIncrementer())
				.start(deletePastNotificationsStep).build();
	}

	@Bean
	public Job ftmChipExpiryNotificationsJob(JobRepository jobRepository,
			@Qualifier("ftmChipExpiryNotificationsStep") Step ftmChipExpiryNotificationsStep) {
		return new JobBuilder("ftmChipExpiryNotificationsStep", jobRepository).incrementer(new RunIdIncrementer())
				.start(ftmChipExpiryNotificationsStep).build();
	}

	@Bean
	public Job sbiExpiryNotificationsJob(JobRepository jobRepository,
			@Qualifier("sbiExpiryNotificationsStep") Step sbiExpiryNotificationsStep) {
		return new JobBuilder("sbiExpiryNotificationsStep", jobRepository).incrementer(new RunIdIncrementer())
				.start(sbiExpiryNotificationsStep).build();
	}

	@Bean
	public Job apiKeyExpiryNotificationsJob(JobRepository jobRepository,
			@Qualifier("apiKeyExpiryNotificationsStep") Step apiKeyExpiryNotificationsStep) {
		return new JobBuilder("apiKeyExpiryNotificationsStep", jobRepository).incrementer(new RunIdIncrementer())
				.start(apiKeyExpiryNotificationsStep).build();
	}

	@Bean
	public Job sbiExpiryAutoDeactivationJob(JobRepository jobRepository,
											@Qualifier("sbiExpiryAutoDeactivationStep") Step sbiExpiryAutoDeactivationStep) {
		return new JobBuilder("sbiExpiryAutoDeactivationStep", jobRepository).incrementer(new RunIdIncrementer())
				.start(sbiExpiryAutoDeactivationStep).build();
	}
}
