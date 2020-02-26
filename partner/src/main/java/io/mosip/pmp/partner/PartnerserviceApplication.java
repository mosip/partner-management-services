package io.mosip.pmp.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


/**
 * @author sanjeev.shrivastava
 *
 */

@SpringBootApplication
public class PartnerserviceApplication{

	public static void main(String[] args) {
		SpringApplication.run(PartnerserviceApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate() {
		
		return new RestTemplate();
	}
}
