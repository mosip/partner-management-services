package io.mosip.pms.partner.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.service.impl.DeviceDetailServiceImpl;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.service.MultiPartnerAdminService;
import io.mosip.pms.partner.util.MultiPartnerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class MultiPartnerAdminServiceImpl implements MultiPartnerAdminService {

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerAdminServiceImpl.class);
    public static final String APPROVED = "approved";
    public static final String ACTIVATE = "Activate";
    public static final String DEACTIVATE = "De-activate";

    @Autowired
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @Autowired
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @Autowired
    DeviceDetailServiceImpl deviceDetailService;

    @Autowired
    MultiPartnerHelper multiPartnerHelper;

    @Override
    public Boolean approveOrRejectDeviceWithSbiMapping(SbiAndDeviceMappingRequestDto requestDto, boolean rejectFlag) {
        Boolean approveDeviceWithSbiMappingFlag = false;
        try {
            String partnerId = requestDto.getPartnerId();
            String sbiId = requestDto.getSbiId();
            String deviceDetailId = requestDto.getDeviceDetailId();
            if (Objects.isNull(partnerId) || Objects.isNull(sbiId) || Objects.isNull(deviceDetailId)) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_PARAM.getErrorMessage());
            }
            // validate sbi and device mapping
            multiPartnerHelper.validateSbiDeviceMapping(partnerId, sbiId, deviceDetailId);

            DeviceDetailSBI deviceDetailSBI = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(partnerId, sbiId, deviceDetailId);
            if (Objects.isNull(deviceDetailSBI)) {
                LOGGER.info("sessionId", "idType", "id", "SBI and Device mapping already exists in DB.");
                throw new PartnerServiceException(ErrorCode.SBI_DEVICE_MAPPING_NOT_EXISTS.getErrorCode(),
                        ErrorCode.SBI_DEVICE_MAPPING_NOT_EXISTS.getErrorMessage());
            }

            if (rejectFlag) {
                UpdateDeviceDetailStatusDto deviceDetails = new UpdateDeviceDetailStatusDto();
                deviceDetails.setId(deviceDetailId);
                deviceDetails.setApprovalStatus(DEACTIVATE);
                deviceDetailService.updateDeviceDetailStatus(deviceDetails);
            } else {
                UpdateDeviceDetailStatusDto deviceDetails = new UpdateDeviceDetailStatusDto();
                deviceDetails.setId(deviceDetailId);
                deviceDetails.setApprovalStatus(ACTIVATE);
                deviceDetailService.updateDeviceDetailStatus(deviceDetails);
            }

            DeviceDetailSBI entity = deviceDetailSBI;
            entity.setIsActive(true);

            DeviceDetailSBI savedEntity = deviceDetailSbiRepository.save(entity);
            LOGGER.info("sessionId", "idType", "id", "updated device mapping to sbi successfully in Db.");
            approveDeviceWithSbiMappingFlag = true;
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In approveOrRejectDeviceWithSbiMapping method of MultiPartnerAdminServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In approveOrRejectDeviceWithSbiMapping method of MultiPartnerAdminServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.APPROVE_OR_REJECT_DEVICE_WITH_SBI__MAPPING_ERROR.getErrorCode(),
                    ErrorCode.APPROVE_OR_REJECT_DEVICE_WITH_SBI__MAPPING_ERROR.getErrorMessage());
        }
        return approveDeviceWithSbiMappingFlag;
    }
}
