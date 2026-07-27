package io.mosip.pms.tasklets;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.helper.WebSubPublisher;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.entity.MISPLicenseEntityV2;
import io.mosip.pms.common.repository.MispLicenseV2Repository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.util.MapperUtils;

/**
 * Batch Job to auto deactivate MISP License key after expiry.
 */
@Component
public class MispLicenseExpiryAutoDeactivationTasklet implements Tasklet {

    private Logger log = PMSLogger.getLogger(MispLicenseExpiryAutoDeactivationTasklet.class);

    @Autowired
    MispLicenseV2Repository mispLicenseRepository;

    @Value("${mosip.pms.batch.job.skips.partner.ids:}")
    private String skipPartnerIdsConfig;

    private Set<String> skipPartnerIds = Collections.emptySet();

    @Autowired
    WebSubPublisher webSubPublisher;

    @PostConstruct
    public void initSkipPartnerIds() {
        if (skipPartnerIdsConfig != null && !skipPartnerIdsConfig.trim().isEmpty()) {
            skipPartnerIds = Arrays.stream(skipPartnerIdsConfig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("MISPLicenseExpiryAutoDeactivationTasklet: START");
        int deactivatedCount = 0;
        try {
            log.info("As per configuration, skip the MISP License keys which created by partner ids: {}", skipPartnerIds);
            // Fetch all expired active MISP Licenses
            LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));
            List<MISPLicenseEntityV2> mispLicenseList = mispLicenseRepository.findAllExpiredActiveMISPLicenses(currentDateTime);
            log.info("MISPLicenseExpiryAutoDeactivationTasklet: Found {} expired active MISP Licenses", mispLicenseList.size());
            for (MISPLicenseEntityV2 mispLicenseDetails : mispLicenseList) {
                try {
                    if (!skipPartnerIds.contains(mispLicenseDetails.getPartner().getId())) {
                        // Deactivate MISP License
                        mispLicenseDetails.setIsActive(false);
                        mispLicenseDetails.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
                        mispLicenseDetails.setUpdatedBy(this.getClass().getSimpleName());
                        mispLicenseRepository.save(mispLicenseDetails);

                        // Send event to websub publisher
                        Map<String, Object> data = new HashMap<>();
                        data.put(PartnerConstants.MISP_DATA, MapperUtils.mapDataToPublishDtoV2(mispLicenseDetails));
                        Type type = new Type();
                        type.setName("InfraProviderServiceImpl");
                        type.setNamespace("io.mosip.pmp.partner.service.impl.InfraProviderServiceImpl");
                        webSubPublisher.notify(EventType.MISP_LICENSE_UPDATED, data, type);

                        deactivatedCount++;
                        log.info("Deactivated expired MISP License key with id {} for misp id : {}", mispLicenseDetails.getMispLicenseId(), mispLicenseDetails.getMispId());
                    }
                } catch(Exception e){
                    log.error("Error deactivating MISP License key with id {} for misp id {}: {}", mispLicenseDetails.getMispLicenseId(), mispLicenseDetails.getMispId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while running MispLicenseExpiryAutoDeactivationTasklet: {}", e.getMessage(), e);
        }
        log.info("MISPpLicenseExpiryAutoDeactivationTasklet: DONE, deactivated {} expired MISP License keys.", deactivatedCount);
        return RepeatStatus.FINISHED;
    }
}
