package io.mosip.pmp.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "io.mosip.pmp.policy.*", "io.mosip.kernel.auth.adapter.*" })
public class PmpPolicyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PmpPolicyApplication.class, args);
	}

}
