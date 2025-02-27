package io.mosip.pms.partner.management.batch.job.tasklets;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.pms.partner.management.batch.job.config.LoggerConfiguration;
import io.mosip.pms.partner.management.batch.job.impl.SampleService;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SampleTasklet implements Tasklet {

    private Logger log = LoggerConfiguration.logConfig(SampleTasklet.class);

    @Autowired
    SampleService sampleService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws JsonProcessingException {
        log.info("SampleTasklet start...");
        try {
            sampleService.getPartnerCertificate();
        } catch (Exception e) {
            log.error("Error occurred while fetching partner certificate: {}", e.getMessage(), e);
        }
        log.info("SampleTasklet done...");
        return RepeatStatus.FINISHED;
    }

}
