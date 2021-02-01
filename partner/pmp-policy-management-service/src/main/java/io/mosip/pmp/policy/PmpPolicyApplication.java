package io.mosip.pmp.policy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.policy.validator.impl.PolicySchemaValidator;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@Import(value = {WebSubPublisher.class,PolicySchemaValidator.class,SearchHelper.class,FilterHelper.class,PageUtils.class,
		FilterColumnValidator.class})
@ComponentScan({ "io.mosip.pmp.policy.*", "${mosip.auth.adapter.impl.basepackage}","io.mosip.pmp.common.*"})
@EnableSwagger2
public class PmpPolicyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PmpPolicyApplication.class, args);
	}

}
