package io.mosip.pms.policy.config;

/*
 * @author Kamesh Shekhar Prasad
 *
 */

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.pms.common.entity.AuthPolicy;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Map;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory",
        basePackages = "io.mosip.pms.common.repository.*",
		repositoryBaseClass = HibernateRepositoryImpl.class)
@EntityScan(basePackageClasses = { AuthPolicy.class })
public class PolicyManagerDataSourceConfig extends HibernateDaoConfig {


	public Map<String, Object> jpaProperties() {
        return super.jpaProperties();
	}
}
