package io.mosip.pmp.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;


/**
 * @author sanjeev.shrivastava
 *
 */

@SpringBootApplication
@ComponentScan({ "io.mosip.pmp.partner.*", "io.mosip.kernel.auth.adapter.*" })
public class PartnerserviceApplication{

	public static void main(String[] args) {
		SpringApplication.run(PartnerserviceApplication.class, args);
	}
}
