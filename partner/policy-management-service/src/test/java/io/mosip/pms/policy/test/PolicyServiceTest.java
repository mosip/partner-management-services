/**
 *
 */
package io.mosip.pms.policy.test;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;

/**
 * @author Nagarjuna
 *
 */
@Import(value = {WebSubPublisher.class,RestUtil.class,SearchHelper.class,FilterHelper.class,PageUtils.class,FilterColumnValidator.class,RestUtil.class})
@SpringBootApplication
@ComponentScan(value = {"io.mosip.pms.*"}, excludeFilters  = {@ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE, classes = { HibernateDaoConfig.class})})
public class PolicyServiceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PolicyServiceTest.class, args);

	}

}