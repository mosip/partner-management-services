package io.mosip.pms.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.WebSubPublisher;

@SpringBootApplication
@Import(value = {WebSubPublisher.class})
@ComponentScan({ "io.mosip.pms.policy.*", "${mosip.auth.adapter.impl.basepackage}","io.mosip.pms.common.*"
		,"io.mosip.kernel.websub.api.config", "io.mosip.kernel.templatemanager.velocity.builder"})
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class PmpPolicyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PmpPolicyApplication.class, args);
	}

}
