package io.mosip.pms.service;

import io.mosip.kernel.auditmanager.dto.AuthorizedRolesDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;

@SpringBootApplication
@Import(value = {WebSubPublisher.class,RestUtil.class,FilterColumnValidator.class,FilterHelper.class,SearchHelper.class,PageUtils.class})
@ComponentScan(basePackages = {"io.mosip.pms.*","io.mosip.kernel.websub.api.config",
		"io.mosip.kernel.templatemanager.velocity.builder", "${mosip.auth.adapter.impl.basepackage}", "io.mosip.kernel.authcodeflowproxy.*"},excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = AuthorizedRolesDto.class) })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
public class  PartnerManagementService {

	public static void main(String[] args) {
		SpringApplication.run(PartnerManagementService.class, args);
	}
}
