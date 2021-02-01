package io.mosip.pmp.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author sanjeev.shrivastava
 *
 */

@SpringBootApplication
@Import(value = {WebSubPublisher.class,SearchHelper.class,FilterHelper.class,PageUtils.class,
		FilterColumnValidator.class})
@ComponentScan({ "io.mosip.pmp.*", "${mosip.auth.adapter.impl.basepackage}"})
@EnableSwagger2
public class PartnerserviceApplication{

	public static void main(String[] args) {
		SpringApplication.run(PartnerserviceApplication.class, args);
	}
}
