package io.mosip.pms.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.interceptor.CryptoInterceptor;

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
@EntityScan(basePackages ="io.mosip.pms.device.authdevice.entity.*, io.mosip.pms.common.entity.*" )
public class PartnerManagerDataSourceConfig extends HibernateDaoConfig {

	@Autowired
	private CryptoInterceptor cryptoInterceptor;

	@Override
	public Map<String, Object> jpaProperties() {
		Map<String, Object> props = super.jpaProperties();
		props.put("hibernate.session_factory.interceptor", cryptoInterceptor);
		return props;
	}
}
