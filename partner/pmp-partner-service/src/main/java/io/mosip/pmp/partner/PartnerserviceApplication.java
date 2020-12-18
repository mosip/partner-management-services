package io.mosip.pmp.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pmp.partner.util.WebSubPublisher;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author sanjeev.shrivastava
 *
 */

@SpringBootApplication
@Import(value = {WebSubPublisher.class})
@ComponentScan({ "io.mosip.pmp.*", "${mosip.auth.adapter.impl.basepackage}"})
@EnableSwagger2
public class PartnerserviceApplication{

	public static void main(String[] args) {
		SpringApplication.run(PartnerserviceApplication.class, args);
	}
}
