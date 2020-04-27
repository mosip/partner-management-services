/**
 * 
 */
package io.mosip.pmp.policy.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Nagarjuna
 *
 */
@SpringBootApplication(scanBasePackages = "io.mosip.pmp.policy.*")
public class PolicyServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PolicyServiceTest.class, args);

	}

}
