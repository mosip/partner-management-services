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
public class ApiKeyAutoDeactivationTasklet implements Tasklet {

    private Logger log = PMSLogger.getLogger(ApiKeyAutoDeactivationTasklet.class);

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
        log.info("ApiKeyAutoDeactivationTasklet: START");
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
                        deactivatedCount++;

                        log.info("Deactivated expired API Key with id {} for partner id : {}", apiKeyDetails.getPolicyApiKey(), apiKeyDetails.getPartner().getId());

                        // Audit log
                        sendAuditSuccess(apiKeyDetails);

                        // Send event to websub publisher
                        notifyWebSub(apiKeyDetails);

                        // TODO: Send email notification to partner

                    }
                } catch (Exception e) {
                    log.error("Error deactivating API Key with id {} for partner id {}: {}", apiKeyDetails.getPolicyApiKey(), apiKeyDetails.getPartner().getId(), e.getMessage(), e);
                    sendAuditFailure(apiKeyDetails);
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while running ApiKeyAutoDeactivationTasklet: {}", e.getMessage(), e);
        }
        log.info("ApiKeyAutoDeactivationTasklet: DONE, deactivated {} expired API Keys.", deactivatedCount);
        return RepeatStatus.FINISHED;
    }

    private void sendAuditSuccess(PartnerPolicy policy) {
        try {
            auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.DEACTIVATE_EXPIRED_API_KEY_SUCCESS,
                    policy.getPartner().getId(), "partnerId", AuditConstant.AUDIT_SYSTEM);
        } catch (Exception e) {
            log.error("Failed to log audit event for successful Expired API Key deactivation. API Key ID: {}, Error: {}", policy.getPolicyApiKey(), e.getMessage(), e);
        }
    }

    private void sendAuditFailure(PartnerPolicy policy) {
        try {
            auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.DEACTIVATE_EXPIRED_API_KEY_FAILURE,
                    policy.getPartner().getId(), "partnerId");
        } catch (Exception e) {
            log.error("Failed to log audit event for Expired API Key deactivation failure. API Key ID: {}, Error: {}", policy.getPolicyApiKey(), e.getMessage(), e);
        }
    }

    private void notifyWebSub(PartnerPolicy policy) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put(PartnerConstants.APIKEY_DATA, MapperUtils.mapKeyDataToPublishDto(policy));
            partnerManagementService.notify(data, EventType.APIKEY_UPDATED);
        } catch (Exception e) {
            log.error("WebSub notification failed for successful Expired API Key deactivation. API Key ID: {}, Error: {}", policy.getPolicyApiKey(), e.getMessage(), e);
        }
    }
}
