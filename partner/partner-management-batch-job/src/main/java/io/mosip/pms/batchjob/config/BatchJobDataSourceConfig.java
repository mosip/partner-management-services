package io.mosip.pms.batchjob.config;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class BatchJobDataSourceConfig extends HibernateDaoConfig {

    public Map<String, Object> jpaProperties() {
        return super.jpaProperties();
    }
}
