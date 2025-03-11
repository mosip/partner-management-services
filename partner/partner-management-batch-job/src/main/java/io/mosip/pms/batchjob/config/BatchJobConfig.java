/* 
 * Copyright
 * 
 */
package io.mosip.pms.batchjob.config;

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

import io.mosip.pms.batchjob.tasklets.RootAndIntermediateCertificateExpiryTasklet;

@Configuration
public class BatchJobConfig {

	@Autowired
	private RootAndIntermediateCertificateExpiryTasklet rootCertificateExpiryTasklet;

	@Bean
	public Step rootCertificateExpiryStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("rootCertificateExpiryStep", jobRepository)
				.tasklet(rootCertificateExpiryTasklet, transactionManager).build();
	}

	@Bean
	public Job rootCertificateExpiryJob(JobRepository jobRepository,
			@Qualifier("rootCertificateExpiryStep") Step rootCertificateExpiryStep) {
		return new JobBuilder("rootCertificateExpiryJob", jobRepository).incrementer(new RunIdIncrementer())
				.start(rootCertificateExpiryStep).build();
	}
}
