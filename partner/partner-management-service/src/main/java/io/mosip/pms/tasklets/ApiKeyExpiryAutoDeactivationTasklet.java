package io.mosip.pms.tasklets;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.manager.constant.AuditConstant;
import io.mosip.pms.partner.manager.service.impl.PartnerManagementServiceImpl;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.util.MapperUtils;

/**
 * Batch Job to auto deactivate API Keys after expiry.
 */
@Component
public class ApiKeyExpiryAutoDeactivationTasklet implements Tasklet {

    private Logger log = PMSLogger.getLogger(ApiKeyExpiryAutoDeactivationTasklet.class);

    @Autowired
    PartnerPolicyRepository partnerPolicyRepository;

    @Autowired
    PartnerManagementServiceImpl partnerManagementService;

    @Autowired
    BatchJobHelper batchJobHelper;

    @Autowired
    private AuditUtil auditUtil;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("ApiKeyExpiryAutoDeactivationTasklet: START");
        int deactivatedCount = 0;
        try {
            // Fetch all active API keys
            List<PartnerPolicy> apiKeyList = partnerPolicyRepository.findAllActiveApiKeys();
            for (PartnerPolicy apiKeyDetails : apiKeyList) {
                try {
                    if (apiKeyDetails.getValidToDatetime() != null &&
                            apiKeyDetails.getValidToDatetime().toLocalDateTime().isBefore(LocalDateTime.now())) {

                        // Deactivate API Key
                        apiKeyDetails.setIsActive(false);
                        apiKeyDetails.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
                        apiKeyDetails.setUpdBy(this.getClass().getSimpleName());
                        partnerPolicyRepository.save(apiKeyDetails);

                        // Send event to websub publisher
                        Map<String, Object> data = new HashMap<>();
                        data.put(PartnerConstants.APIKEY_DATA, MapperUtils.mapKeyDataToPublishDto(apiKeyDetails));
                        partnerManagementService.notify(data, EventType.APIKEY_UPDATED);

                        // Audit log
                        auditUtil.setAuditRequestDto(
                                PartnerServiceAuditEnum.DEACTIVATE_EXPIRED_API_KEY_SUCCESS,
                                apiKeyDetails.getPolicyApiKey(),
                                "apiKeyId",
                                AuditConstant.AUDIT_SYSTEM
                        );

                        // TODO: Send email notification to partner
                        
                        deactivatedCount++;
                        log.info("Deactivated expired API Key with id {} for partner id : {}", apiKeyDetails.getPolicyApiKey(), apiKeyDetails.getPartner().getId());
                    }
                } catch (Exception e) {
                    log.error("Error deactivating API Key with id {} for partner id {}: {}", apiKeyDetails.getPolicyApiKey(), apiKeyDetails.getPartner().getId(), e.getMessage(), e);
                    auditUtil.setAuditRequestDto(
                            PartnerServiceAuditEnum.DEACTIVATE_EXPIRED_API_KEY_FAILURE,
                            apiKeyDetails.getPolicyApiKey(),
                            "apiKeyId",
                            AuditConstant.AUDIT_SYSTEM
                    );
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while running ApiKeyExpiryAutoDeactivationTasklet: {}", e.getMessage(), e);
        }
        log.info("ApiKeyExpiryAutoDeactivationTasklet: DONE, deactivated {} expired API Keys.", deactivatedCount);
        return RepeatStatus.FINISHED;
    }
}
