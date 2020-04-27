package io.mosip.pmp.partnermanagement.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author sanjeev.shrivastava
 *
 */
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
