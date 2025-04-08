package io.mosip.pms.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.test.config.TestSecurityConfig;

@SpringBootApplication
@ComponentScan(basePackages = { "io.mosip.pms.*"})
@Import(TestSecurityConfig.class)
public class PartnerManagementServiceTest {

	public static void main(String[] args) {
		SpringApplication.run(PartnerManagementServiceTest.class, args);
	}

}
