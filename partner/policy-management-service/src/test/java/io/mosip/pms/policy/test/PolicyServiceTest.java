/**
 *
 */
package io.mosip.pms.policy.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;

/**
 * @author Nagarjuna
 *
 */
@Import(value = { WebSubPublisher.class, SearchHelper.class, FilterHelper.class, PageUtils.class,
		FilterColumnValidator.class })
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = { "io.mosip.pms.policy.*", "io.mosip.pms.common.*",
		"io.mosip.kernel.websub.api.config", "io.mosip.kernel.templatemanager" })
public class PolicyServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PolicyServiceTest.class, args);

	}

}