package io.mosip.pms.encryptutility;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.encryptutility.service.DataEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@ComponentScan({ "${mosip.auth.adapter.impl.basepackage}","io.mosip.pms.common.*", "io.mosip.pms.encryptutility.*",
        "io.mosip.kernel.templatemanager.velocity.builder"})
public class PartnerDataEncryptUtilityApplication implements CommandLineRunner {

    private static final Logger LOGGER = PMSLogger.getLogger(PartnerDataEncryptUtilityApplication.class);

    private static final int SUCCESS_EXIT_CODE = 0;
    private static final int ERROR_EXIT_CODE = 1;

    @Autowired
    private DataEncryptionService dataEncryptionService;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        LOGGER.info("Starting Partner Data Encryption Utility Application...");
        SpringApplication.run(PartnerDataEncryptUtilityApplication.class, args);
    }

    @Override
    public void run(String... args) {
        int exitCode = SUCCESS_EXIT_CODE;

        try {
            LOGGER.info("Initiating encryption of partner data...");
            dataEncryptionService.encryptData();
            LOGGER.info("Partner data encryption process completed successfully.");

        } catch (Exception e) {
            LOGGER.error("Error during partner data encryption: {}", e.getMessage(), e);
            exitCode = ERROR_EXIT_CODE;
        }

        shutdownApplication(exitCode);
    }

    private void shutdownApplication(int exitCode) {
        LOGGER.info("Shutting down application with exit code: {}", exitCode);
        SpringApplication.exit(applicationContext, () -> exitCode);
        System.exit(exitCode);
    }
}
