package io.mosip.pms.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"io.mosip.pms"})
public class PartnerManagementServiceTest {
	
	public static void main(String[] args) {
		SpringApplication.run(PartnerManagementServiceTest.class, args);
	}

}
