/**
 * 
 */
package io.mosip.pmp.policy.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.policy.validator.impl.PolicySchemaValidator;

/**
 * @author Nagarjuna
 *
 */
@Import(value = {WebSubPublisher.class,RestUtil.class,PolicySchemaValidator.class,SearchHelper.class,FilterHelper.class,PageUtils.class,
		FilterColumnValidator.class})
@SpringBootApplication(scanBasePackages = { "io.mosip.pmp.policy.*","io.mosip.pmp.common.*"})
public class PolicyServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PolicyServiceTest.class, args);

	}

}
