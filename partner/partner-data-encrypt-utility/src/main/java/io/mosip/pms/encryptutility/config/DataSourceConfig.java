package io.mosip.pms.encryptutility.config;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
public class DataSourceConfig extends HibernateDaoConfig {

    @Override
    public java.util.Map<String, Object> jpaProperties() {
        return super.jpaProperties();
    }
} 