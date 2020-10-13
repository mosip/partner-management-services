package io.mosip.pmp.misp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ComponentScan({ "io.mosip.pmp.misp.*","${mosip.auth.adapter.impl.basepackage}"})
@EnableSwagger2
public class PmpMispApplication extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(PmpMispApplication.class, args);
	}
	

} 
