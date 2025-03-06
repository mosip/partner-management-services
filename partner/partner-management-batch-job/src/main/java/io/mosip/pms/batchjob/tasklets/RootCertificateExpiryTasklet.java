package io.mosip.pms.batchjob.tasklets;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.batchjob.impl.CertificateExpiryService;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RootCertificateExpiryTasklet implements Tasklet {

    private Logger log = LoggerConfiguration.logConfig(RootCertificateExpiryTasklet.class);

    @Autowired
    CertificateExpiryService certificateExpiryService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws JsonProcessingException {
        log.info("RootCertificateExpiryTasklet: START");
        try {
        	OriginalCertDownloadResponseDto dto = certificateExpiryService.getPartnerCertificate();
        	log.info(dto.getMosipSignedCertificateData());
        } catch (Exception e) {
            log.error("Error occurred while running RootCertificateExpiryTasklet: {}", e.getMessage(), e);
        }
        log.info("RootCertificateExpiryTasklet: DONE");
        return RepeatStatus.FINISHED;
    }

}
