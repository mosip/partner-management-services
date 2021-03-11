package io.mosip.pmp.misp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pmp.common.helper.WebSubPublisher;
import io.mosip.pmp.common.util.RestUtil;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@Import(value = {WebSubPublisher.class,RestUtil.class})
@ComponentScan({ "io.mosip.pmp.misp.*","${mosip.auth.adapter.impl.basepackage}"})
@EnableSwagger2
public class PmpMispApplication extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(PmpMispApplication.class, args);
	}
} 
