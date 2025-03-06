package io.mosip.pms.batchjob.config;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class VelocityConfig {

    @Bean
    public VelocityEngine velocityEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loader", "string");
        props.setProperty("string.resource.loader.class", StringResourceLoader.class.getName());

        var velocityEngine = new VelocityEngine();
        velocityEngine.init(props);
        return velocityEngine;
    }
}
