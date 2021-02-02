package io.mosip.pms.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.WebSubPublisher;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@Import(value = {WebSubPublisher.class})
@ComponentScan({ "io.mosip.pms.policy.*", "${mosip.auth.adapter.impl.basepackage}","io.mosip.pms.common.*"})
@EnableSwagger2
public class PmpPolicyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PmpPolicyApplication.class, args);
	}

}
