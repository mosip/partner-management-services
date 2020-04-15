package io.mosip.pmp.misp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PmpMispApplication extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(PmpMispApplication.class, args);
	}
} 
