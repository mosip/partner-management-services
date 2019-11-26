/**
 * 
 */
package io.mosip.pmp.misp.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Nagarjuna Kuchi
 *
 */
@SpringBootApplication(scanBasePackages = "io.mosip.pmp.misp.*")
public class MispServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(MispServiceTest.class, args);
	}

}
