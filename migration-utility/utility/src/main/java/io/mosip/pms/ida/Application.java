package io.mosip.pms.ida;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.ida.service.PMSDataMigrationService;
import io.mosip.pms.ida.util.RestUtil;
import io.mosip.pms.ida.websub.WebSubPublisher;

@SpringBootApplication
@Import(value = {WebSubPublisher.class,RestUtil.class})
@ComponentScan(basePackages = {"io.mosip.pms.*"})
public class Application implements CommandLineRunner {

	@Autowired
	PMSDataMigrationService pmsIdaDataMigrationService;

	Logger logger = org.slf4j.LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);
		SpringApplication.exit(run);
	}

	@Override
	public void run(String... args) throws Exception {
		pmsIdaDataMigrationService.initialize();

	}
}
