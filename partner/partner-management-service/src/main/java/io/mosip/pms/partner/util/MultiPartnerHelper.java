package io.mosip.pms.partner.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MultiPartnerHelper {

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerHelper.class);
    public static final String APPROVED = "approved";
    public static final String PENDING_APPROVAL = "pending_approval";

    @Autowired
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @Autowired
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @Autowired
    DeviceDetailRepository deviceDetailRepository;

    public void validateSbiDeviceMapping(String partnerId, String sbiId, String deviceDetailId) {
        Optional<SecureBiometricInterface> secureBiometricInterface = secureBiometricInterfaceRepository.findById(sbiId);
        if (secureBiometricInterface.isEmpty()) {
            LOGGER.info("sessionId", "idType", "id", "Sbi does not exists.");
            throw new PartnerServiceException(ErrorCode.SBI_NOT_EXISTS.getErrorCode(),
                    ErrorCode.SBI_NOT_EXISTS.getErrorMessage());
        } else if (!secureBiometricInterface.get().getProviderId().equals(partnerId)) {
            LOGGER.info("sessionId", "idType", "id", "Sbi is not associated with partner Id.");
            throw new PartnerServiceException(ErrorCode.SBI_NOT_ASSOCIATED_WITH_PARTNER_ID.getErrorCode(),
                    ErrorCode.SBI_NOT_ASSOCIATED_WITH_PARTNER_ID.getErrorMessage());
        } else if (!(secureBiometricInterface.get().getApprovalStatus().equals(APPROVED) && secureBiometricInterface.get().isActive())) {
            LOGGER.info("sessionId", "idType", "id", "Sbi is not approved.");
            throw new PartnerServiceException(ErrorCode.SBI_NOT_APPROVED.getErrorCode(),
                    ErrorCode.SBI_NOT_APPROVED.getErrorMessage());
        }

        Optional<DeviceDetail> deviceDetail = deviceDetailRepository.findById(deviceDetailId);
        if (deviceDetail.isEmpty()) {
            LOGGER.info("sessionId", "idType", "id", "Device does not exists.");
            throw new PartnerServiceException(ErrorCode.DEVICE_NOT_EXISTS.getErrorCode(),
                    ErrorCode.DEVICE_NOT_EXISTS.getErrorMessage());
        } else if (!deviceDetail.get().getDeviceProviderId().equals(partnerId)) {
            LOGGER.info("sessionId", "idType", "id", "Device is not associated with partner Id.");
            throw new PartnerServiceException(ErrorCode.DEVICE_NOT_ASSOCIATED_WITH_PARTNER_ID.getErrorCode(),
                    ErrorCode.DEVICE_NOT_ASSOCIATED_WITH_PARTNER_ID.getErrorMessage());
        } else if (!deviceDetail.get().getApprovalStatus().equals(PENDING_APPROVAL)) {
            LOGGER.info("sessionId", "idType", "id", "Device is not in pending for approval state.");
            throw new PartnerServiceException(ErrorCode.DEVICE_NOT_PENDING_FOR_APPROVAL.getErrorCode(),
                    ErrorCode.DEVICE_NOT_PENDING_FOR_APPROVAL.getErrorMessage());
        }
    }
}
