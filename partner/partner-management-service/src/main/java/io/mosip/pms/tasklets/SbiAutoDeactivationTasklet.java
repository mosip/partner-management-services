package io.mosip.pms.tasklets;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.AuditConstant;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterfaceHistory;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.service.impl.SecureBiometricInterfaceServiceImpl;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class SbiAutoDeactivationTasklet implements Tasklet {

    private Logger log = PMSLogger.getLogger(SbiAutoDeactivationTasklet.class);

    @Autowired
    BatchJobHelper batchJobHelper;

    @Autowired
    SecureBiometricInterfaceRepository sbiRepository;

    @Autowired
    DeviceDetailRepository deviceDetailRepository;

    @Autowired
    SecureBiometricInterfaceHistoryRepository sbiHistoryRepository;

    @Autowired
    SecureBiometricInterfaceServiceImpl secureBiometricInterfaceServiceImpl;

    @Autowired
    AuditUtil auditUtil;

    @Value("${mosip.pms.batch.job.enable.sbi.auto.deactivation}")
    private Boolean enableSbiAutoDeactivation;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        if (enableSbiAutoDeactivation) {
            log.info("SbiAutoDeactivationTasklet: START");
            int deviceProvidersCount = 0;
            int countOfSbiDeactivated = 0;
            int countOfSbiRejected = 0;
            try {
                // Step 1: get all the SBI's which are approved and pending_approval
                List<SecureBiometricInterface> sbiList = sbiRepository.findAllApprovedAndPendingSBI();
                int sbiCount = sbiList.size();
                log.info("Found {} SBIs which are approved and pending_approval.", sbiCount);
                for (SecureBiometricInterface sbiDetail : sbiList) {
                    // Step 2: For each SBI check if it is expired or not
                    try {
                        String sbiId = sbiDetail.getId();
                        String sbiStatus = sbiDetail.getApprovalStatus();
                        if (sbiDetail.getSwExpiryDateTime() != null) {
                            LocalDateTime sbiExpiryDateTime = sbiDetail.getSwExpiryDateTime();
                            if (sbiExpiryDateTime.isBefore(LocalDateTime.now())) {
                                if (sbiStatus.equals(PartnerConstants.APPROVED) && sbiDetail.isActive()) {
                                    // Step 4: Deactivate approved devices
                                    List<DeviceDetail> approvedDevices = deviceDetailRepository.findApprovedDevicesBySbiId(sbiId);
                                    if (!approvedDevices.isEmpty()) {
                                        for (DeviceDetail deviceDetail : approvedDevices) {
                                            deviceDetail.setIsActive(false);
                                            deviceDetail.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
                                            deviceDetail.setUpdBy(PartnerConstants.SYSTEM_USER);
                                            deviceDetailRepository.save(deviceDetail);
                                        }
                                        log.info("{} approved devices have been deactivated for SBI id: {}", approvedDevices.size(), sbiId);
                                    }
                                    // Step 5: Reject pending_approval devices
                                    List<DeviceDetail> pendingApprovalDevices = deviceDetailRepository.findPendingApprovalDevicesBySbiId(sbiId);
                                    if (!pendingApprovalDevices.isEmpty()) {
                                        for (DeviceDetail deviceDetail : pendingApprovalDevices) {
                                            deviceDetail.setApprovalStatus(PartnerConstants.REJECTED);
                                            deviceDetail.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
                                            deviceDetail.setUpdBy(PartnerConstants.SYSTEM_USER);
                                            deviceDetailRepository.save(deviceDetail);
                                            auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REJECT_DEVICE_WITH_EXPIRED_SBI_SUCCESS, deviceDetail.getId(), "deviceId", AuditConstant.AUDIT_SYSTEM);
                                        }
                                        log.info("{} pending approval devices have been rejected for SBI id: {}", pendingApprovalDevices.size(), sbiId);
                                    }
                                    // Step 6: deactivate SBI if it is approved
                                    sbiDetail.setActive(false);
                                    countOfSbiDeactivated++;
                                    log.info("SBI with id {} has been deactivated for Partner id: {}", sbiId, sbiDetail.getProviderId());
                                } else {
                                    // Step 6: reject SBI if it is pending_approval
                                    sbiDetail.setApprovalStatus(PartnerConstants.REJECTED);
                                    countOfSbiRejected++;
                                    log.info("SBI with id {} has been rejected for Partner id : {}", sbiId, sbiDetail.getProviderId());
                                }
                                sbiDetail.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
                                sbiDetail.setUpdBy(this.getClass().getName());
                                SecureBiometricInterface updatedSbi = sbiRepository.save(sbiDetail);
                                SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
                                secureBiometricInterfaceServiceImpl.getUpdateHistoryMapping(history, updatedSbi);
                                sbiHistoryRepository.save(history);
                                auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.DEACTIVATE_EXPIRED_SBI_SUCCESS, sbiDetail.getId(), "sbiId", AuditConstant.AUDIT_SYSTEM);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error deactivating the SBI with id {} for partner id {}: {} }", sbiDetail.getId(), sbiDetail.getProviderId(), e.getMessage(), e);
                        auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.DEACTIVATE_EXPIRED_SBI_FAILURE, sbiDetail.getId(), "sbiId", AuditConstant.AUDIT_SYSTEM);
                    }
                }
            } catch (Exception e) {
                log.error("Error occurred while running SbiAutoDeactivationTasklet: {}", e.getMessage(), e);
            }
            log.info("SbiAutoDeactivationTasklet: DONE — " + countOfSbiDeactivated + " SBIs deactivated, "
                    + countOfSbiRejected + " SBIs rejected. Checked SBI expiry for "
                    + deviceProvidersCount + " device providers.");
        }
        return RepeatStatus.FINISHED;
    }
}
