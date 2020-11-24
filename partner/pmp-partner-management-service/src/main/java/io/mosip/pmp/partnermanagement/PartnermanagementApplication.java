package io.mosip.pmp.partnermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pmp.common.helper.WebSubPublisher;
import io.mosip.pmp.common.util.RestUtil;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@Import(value = {WebSubPublisher.class,RestUtil.class})
@ComponentScan({ "io.mosip.pmp.partnermanagement.*", "${mosip.auth.adapter.impl.basepackage}"})
@EnableSwagger2
public class PartnermanagementApplication {
		
	public static void main(String[] args) {
		SpringApplication.run(PartnermanagementApplication.class, args);
	}
}
