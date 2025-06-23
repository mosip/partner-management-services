package io.mosip.pms.tasklets;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.pms.common.repository.PartnerServiceRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.tasklets.util.PartnerCertificateExpiryHelper;

/**
 * Batch Job to auto deactivate API Keys after expiry.
 */
@Component
public class ApiKeyAutoDeactivationTasklet implements Tasklet {

    private Logger log = PMSLogger.getLogger(ApiKeyAutoDeactivationTasklet.class);

    @Autowired
    PartnerPolicyRepository partnerPolicyRepository;

    @Autowired
    PartnerServiceRepository partnerServiceRepository;

    @Autowired
    BatchJobHelper batchJobHelper;

    @Autowired
    PartnerHelper partnerHelper;

    @Autowired
    PartnerCertificateExpiryHelper partnerCertificateExpiryHelper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("ApiKeyAutoDeactivationTasklet: START");
        int deactivatedCount = 0;
        try {
            // Get all Auth Partners
            List<Partner> authPartnersList = partnerServiceRepository.findByPartnerTypeCode(PartnerConstants.AUTH_PARTNER_TYPE);
            for (Partner authPartner : authPartnersList) {
                String authPartnerId = authPartner.getId();
                List<PartnerPolicy> apiKeyList = partnerPolicyRepository.findByPartnerIdAndIsActiveTrue(authPartnerId);
                for (PartnerPolicy apiKeyDetails : apiKeyList) {
                    if (apiKeyDetails.getValidToDatetime() != null) {
                        LocalDateTime apiKeyExpiryDateTime = apiKeyDetails.getValidToDatetime().toLocalDateTime();
                        if (apiKeyExpiryDateTime.isBefore(LocalDateTime.now())) {
                            // Deactivate API Key
                            apiKeyDetails.setIsActive(false);
                            partnerPolicyRepository.save(apiKeyDetails);
                            deactivatedCount++;
                            log.info("Deactivated expired API Key with id {} for partner id : {}", apiKeyDetails.getPolicyApiKey(), authPartnerId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while running ApiKeyAutoDeactivationTasklet: {}", e.getMessage(), e);
        }
        log.info("ApiKeyAutoDeactivationTasklet: DONE, deactivated {} expired API Keys.", deactivatedCount);
        return RepeatStatus.FINISHED;
    }
}

