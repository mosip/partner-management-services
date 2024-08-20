package io.mosip.pms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "mosip.pms.api")
@Configuration
public class Config {
    /** The id. */
    private Map<String, String> id;

    /**
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(Map<String, String> id) {
        this.id = id;
    }

    public Map<String, String> getId() {
        return id;
    }

    /**
     * Id.
     *
     * @return the map
     */
    @Bean
    public Map<String, String> ic() {
        return Collections.unmodifiableMap(id);
    }
}

