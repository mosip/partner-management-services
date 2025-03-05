package io.mosip.pms.batchjob.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggerConfiguration {

    // Private constructor to prevent instantiation
    private LoggerConfiguration() {
    }

    /**
     * Get logger instance for the given class.
     *
     * @param clazz the class for which the logger is requested
     * @return a Logger instance
     */
    public static Logger logConfig(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
