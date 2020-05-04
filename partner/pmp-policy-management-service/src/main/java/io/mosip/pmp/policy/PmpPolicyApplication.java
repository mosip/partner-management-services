package io.mosip.pmp.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan({ "io.mosip.pmp.policy.*", "io.mosip.kernel.auth.adapter.*" })
@EnableSwagger2
public class PmpPolicyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PmpPolicyApplication.class, args);
	}

}
