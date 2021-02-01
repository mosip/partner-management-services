/**
 * 
 */
package io.mosip.pmp.misp.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.RestUtil;

/**
 * @author Nagarjuna Kuchi
 *
 */
@Import(value = {WebSubPublisher.class,RestUtil.class})
@SpringBootApplication(scanBasePackages = "io.mosip.pmp.misp.*")
public class MispServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(MispServiceTest.class, args);
	}
}
