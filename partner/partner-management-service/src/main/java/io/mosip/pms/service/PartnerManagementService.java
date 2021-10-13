package io.mosip.pms.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;

@SpringBootApplication
@Import(value = {WebSubPublisher.class,RestUtil.class,FilterColumnValidator.class,FilterHelper.class,SearchHelper.class,PageUtils.class})
@ComponentScan(basePackages = {"io.mosip.pms.*", "${mosip.auth.adapter.impl.basepackage}"})
public class PartnerManagementService {

	public static void main(String[] args) {
		SpringApplication.run(PartnerManagementService.class, args);
	}
}
