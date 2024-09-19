package io.mosip.pms.partner.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.response.dto.OriginalCertDownloadResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PartnerHelper {

    private static final Logger LOGGER = PMSLogger.getLogger(PartnerHelper.class);
    public static final String APPROVED = "approved";
    public static final String PENDING_APPROVAL = "pending_approval";

    @Autowired
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @Autowired
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @Autowired
    DeviceDetailRepository deviceDetailRepository;

    @Autowired
    RestUtil restUtil;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Environment environment;

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
            throw new PartnerServiceException(ErrorCode.SBI_NOT_APPROVED_OR_INACTIVE.getErrorCode(),
                    ErrorCode.SBI_NOT_APPROVED_OR_INACTIVE.getErrorMessage());
        } else if (secureBiometricInterface.get().getSwExpiryDateTime().toLocalDate().isBefore(LocalDate.now())) {
            LOGGER.info("sessionId", "idType", "id", "Sbi is expired.");
            throw new PartnerServiceException(ErrorCode.SBI_EXPIRED.getErrorCode(),
                    ErrorCode.SBI_EXPIRED.getErrorMessage());
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

    public <T> T getCertificate(String certificateAlias, String uriProperty, Class<T> responseType ) throws JsonProcessingException {
        T responseObject = null;
        Map<String, String> pathsegments = new HashMap<>();
        pathsegments.put("partnerCertId", certificateAlias);
        Map<String, Object> getApiResponse = restUtil
                .getApi(environment.getProperty(uriProperty), pathsegments, Map.class);
        responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")), responseType);

        if (responseObject == null && getApiResponse.containsKey(PartnerConstants.ERRORS)) {
            List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse
                    .get(PartnerConstants.ERRORS);
            if (!certServiceErrorList.isEmpty()) {
                LOGGER.error("Error occurred while getting the cert from keymanager");
                throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
                        certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
            } else {
                LOGGER.error("Error occurred while getting the cert {}", getApiResponse);
                throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
                        ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
            }
        }

        if (responseObject == null) {
            LOGGER.error("Got null response from {}", environment.getProperty(uriProperty));
            throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
                    ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
        }
        return responseObject;
    }

    public void populateCertificateExpiryState(OriginalCertDownloadResponseDto originalCertDownloadResponseDto) {
        originalCertDownloadResponseDto.setIsMosipSignedCertificateExpired(false);
        originalCertDownloadResponseDto.setIsCaSignedCertificateExpired(false);
        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));

        // Check mosip signed certificate expiry date
        X509Certificate decodedMosipSignedCert = MultiPartnerUtil.decodeCertificateData(originalCertDownloadResponseDto.getMosipSignedCertificateData());
        LocalDateTime mosipSignedCertExpiryDate = decodedMosipSignedCert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        if (mosipSignedCertExpiryDate.isBefore(currentDateTime)) {
            originalCertDownloadResponseDto.setMosipSignedCertificateData("");
            originalCertDownloadResponseDto.setIsMosipSignedCertificateExpired(true);
        }

        // Check ca signed partner certificate expiry date
        X509Certificate decodedCaSignedCert = MultiPartnerUtil.decodeCertificateData(originalCertDownloadResponseDto.getCaSignedCertificateData());
        LocalDateTime caSignedCertExpiryDate = decodedCaSignedCert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        if (caSignedCertExpiryDate.isBefore(currentDateTime)) {
            originalCertDownloadResponseDto.setCaSignedCertificateData("");
            originalCertDownloadResponseDto.setIsCaSignedCertificateExpired(true);
        }
    }
}
