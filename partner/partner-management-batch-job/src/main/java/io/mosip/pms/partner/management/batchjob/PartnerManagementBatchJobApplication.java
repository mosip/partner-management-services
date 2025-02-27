package io.mosip.pms.partner.management.batchjob;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
@ComponentScan(basePackages = { "io.mosip.pms.partner_batch_job", "${mosip.auth.adapter.impl.basepackage}"})
public class PartnerManagementBatchJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(PartnerManagementBatchJobApplication.class, args);
	}

}
