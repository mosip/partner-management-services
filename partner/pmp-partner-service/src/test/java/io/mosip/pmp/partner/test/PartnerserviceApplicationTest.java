package io.mosip.pmp.partner.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;


/**
 * @author sanjeev.shrivastava
 *
 */
@Import(value = {SearchHelper.class,FilterHelper.class,PageUtils.class,
		FilterColumnValidator.class})
@SpringBootApplication(scanBasePackages = {"io.mosip.pmp.keycloak.*","io.mosip.pmp.partner.*","io.mosip.pmp.authdevice.*","io.mosip.pmp.regdevice.*"})
public class PartnerserviceApplicationTest {
	
	/**
	 * Function to run the Master-Data-Service application
	 * 
	 * @param args The arguments to pass will executing the main function
	 */
	public static void main(String[] args) {
		SpringApplication.run(PartnerserviceApplicationTest.class, args);
	}
}
