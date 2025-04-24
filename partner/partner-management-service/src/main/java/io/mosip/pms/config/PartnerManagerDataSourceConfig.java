package io.mosip.pms.config;

import java.util.Map;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;

/**
 * 
 * @author Mayura Deshmukh
 *
 */
@Configuration
@EnableJpaRepositories(
		entityManagerFactoryRef = "entityManagerFactory",
		basePackages = "io.mosip.pms.device.authdevice.repository.*, io.mosip.pms.common.repository.*",
		repositoryBaseClass = HibernateRepositoryImpl.class
)
public class PartnerManagerDataSourceConfig extends HibernateDaoConfig {

	@Override
	public Map<String, Object> jpaProperties() {
		Map<String, Object> props = super.jpaProperties();
		return props;
	}
}
