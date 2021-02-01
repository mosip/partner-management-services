package io.mosip.pmp.partnermanagement.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.RestUtil;

/**
 * @author sanjeev.shrivastava
 *
 */
@Import(value = {WebSubPublisher.class,RestUtil.class})
@SpringBootApplication(scanBasePackages = "io.mosip.pmp.partnermanagement.*")
public class PartnermanagementApplicationTest {
	
	/**
	 * Function to run the Master-Data-Service application
	 * 
	 * @param args The arguments to pass will executing the main function
	 */
	public static void main(String[] args) {
		SpringApplication.run(PartnermanagementApplicationTest.class, args);
	}
}
