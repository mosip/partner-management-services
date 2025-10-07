package io.mosip.pms.device.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.DeviceDetailExceptionsConstant;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class DeviceHelper {

    private static final Logger LOGGER = PMSLogger.getLogger(DeviceHelper.class);

    @Value("${mosip.pms.id.generation.max.retries}")
    private int maxRetries;

    @Autowired
    DeviceDetailRepository deviceDetailRepository;

    @Autowired
    AuditUtil auditUtil;

    public DeviceDetail getCreateMapping(DeviceDetail deviceDetail, DeviceDetailDto deviceDetailDto) {
        String id = deviceDetailDto.getId();

        if (id != null && !id.isBlank()) {
            if (deviceDetailRepository.existsById(id)) {
                LOGGER.error("Device Detail with same Id already exists: {}", id);
                sendAuditLogAndThrowException(
                        DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorCode(),
                        DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorMessage(),
                        "AUT-002",
                        deviceDetailDto.getDeviceProviderId(),
                        ErrorCode.DEVICE_DETAIL_ID_ALREADY_EXISTS.getErrorCode(),
                        ErrorCode.DEVICE_DETAIL_ID_ALREADY_EXISTS.getErrorMessage()
                );
            }
        } else {
            id = generateUniqueDeviceId(deviceDetail, deviceDetailDto);
        }

        deviceDetail.setId(id);
        deviceDetail.setIsActive(false);
        deviceDetail.setIsDeleted(false);
        deviceDetail.setApprovalStatus(CommonConstant.PENDING_APPROVAL);
        Authentication authN = SecurityContextHolder.getContext().getAuthentication();
        if (!EmptyCheckUtils.isNullEmpty(authN)) {
            deviceDetail.setCrBy(authN.getName());
        }
        deviceDetail.setCrDtimes(LocalDateTime.now(ZoneId.of("UTC")));
        deviceDetail.setDeviceProviderId(deviceDetailDto.getDeviceProviderId());
        deviceDetail.setMake(deviceDetailDto.getMake());
        deviceDetail.setModel(deviceDetailDto.getModel());
        return deviceDetail;
    }

    private String generateUniqueDeviceId(DeviceDetail deviceDetail, DeviceDetailDto deviceDetailDto) {
        String id;
        // Generate ID and ensure uniqueness
        int attempts = 0;
        id = DeviceUtil.generateId();

        while (deviceDetailRepository.existsById(id)) {
            if (attempts >= maxRetries) {
                LOGGER.error("Failed to generate unique {} (field: '{}') for entity '{}' after {} attempts",
                        "Device Detail ID", "id", deviceDetail.getClass().getSimpleName(), maxRetries);

                sendAuditLogAndThrowException(
                        DeviceDetailExceptionsConstant.DEVICE_DETAIL_ID_GENERATION_FAILURE.getErrorCode(),
                        DeviceDetailExceptionsConstant.DEVICE_DETAIL_ID_GENERATION_FAILURE.getErrorMessage(),
                        "AUT-003",
                        deviceDetailDto.getDeviceProviderId(),
                        ErrorCode.UNABLE_TO_GENERATE_UNIQUE_ID.getErrorCode(),
                        String.format(ErrorCode.UNABLE_TO_GENERATE_UNIQUE_ID.getErrorMessage(),
                                "Device Detail ID", "id", deviceDetail.getClass().getSimpleName(), maxRetries)
                );
            }
            id = DeviceUtil.generateId();
            attempts++;
        }
        return id;
    }

    private void sendAuditLogAndThrowException(
            String auditErrorCode,
            String auditErrorMessage,
            String auditCode,
            String deviceProviderId,
            String exceptionErrorCode,
            String exceptionErrorMessage) {

        // Audit
        auditUtil.auditRequest(
                String.format(DeviceConstant.FAILURE_CREATE, DeviceDetail.class.getCanonicalName()),
                DeviceConstant.AUDIT_SYSTEM,
                String.format(DeviceConstant.FAILURE_DESC, auditErrorCode, auditErrorMessage),
                auditCode,
                deviceProviderId,
                "partnerId"
        );

        // Throw
        throw new PartnerServiceException(exceptionErrorCode, exceptionErrorMessage);
    }
}
