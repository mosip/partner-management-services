package io.mosip.pms.tasklets;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.helper.WebSubPublisher;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.entity.MISPLicenseEntityV2;
import io.mosip.pms.common.repository.MispLicenseV2Repository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.tasklets.util.BatchJobHelper;
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

    @Autowired
    BatchJobHelper batchJobHelper;


    @Autowired
    WebSubPublisher webSubPublisher;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("MISPLicenseExpiryAutoDeactivationTasklet: START");
        int deactivatedCount = 0;
        try {
            // Fetch all expired active MISP Licenses
            LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));
            List<MISPLicenseEntityV2> mispLicenseList = mispLicenseRepository.findAllExpiredActiveMISPLicenses(currentDateTime);
            log.info("MISPLicenseExpiryAutoDeactivationTasklet: Found {} expired active MISP Licenses", mispLicenseList.size());
            for (MISPLicenseEntityV2 mispLicenseDetails : mispLicenseList) {
                try {
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
                    log.info("Deactivated expired MISP License key with id {} for misp id : {}", mispLicenseDetails.getId().getLicenseKey(), mispLicenseDetails.getId().getMispId());
                } catch (Exception e) {
                    log.error("Error deactivating MISP License key with id {} for misp id {}: {}", mispLicenseDetails.getId().getLicenseKey(), mispLicenseDetails.getId().getMispId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while running MispLicenseExpiryAutoDeactivationTasklet: {}", e.getMessage(), e);
        }
        log.info("MISPpLicenseExpiryAutoDeactivationTasklet: DONE, deactivated {} expired MISP License keys.", deactivatedCount);
        return RepeatStatus.FINISHED;
    }
}
