package io.mosip.pms.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.pms.config.AuthDeviceDaoConfig;

@Import(value = {AuthDeviceDaoConfig.class})
@SpringBootApplication(scanBasePackages = {"io.mosip.pms"})
public class PartnerManagementServiceTest {
	
	public static void main(String[] args) {
		SpringApplication.run(PartnerManagementServiceTest.class, args);
	}

}
