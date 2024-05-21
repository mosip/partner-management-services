package io.mosip.pms.config;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Map;

/*
 * @author Kamesh Shekhar Prasad
 *
 */

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory",
		basePackages = "io.mosip.pms.device.authdevice.repository.*",
		repositoryBaseClass = HibernateRepositoryImpl.class)
@EntityScan(basePackageClasses = { DeviceDetail.class })
public class PartnerManagerDataSourceConfig extends HibernateDaoConfig {


	public Map<String, Object> jpaProperties() {
        return super.jpaProperties();
	}
}
