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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PartnerHelper {

    private static final Logger LOGGER = PMSLogger.getLogger(PartnerHelper.class);
    public static final String APPROVED = "approved";
    public static final String PENDING_APPROVAL = "pending_approval";
    public static final String CERTIFICATE_UPLOADED = "uploaded";
    public static final String CERTIFICATE_NOT_UPLOADED = "not_uploaded";

    public final Map<String, String> aliasToColumnMap = new HashMap<>();
    {
        aliasToColumnMap.put("partnerId", "id");
        aliasToColumnMap.put("partnerType", "partnerTypeCode");
        aliasToColumnMap.put("orgName", "name");
        aliasToColumnMap.put("policyGroupId", "policyGroupId");
        aliasToColumnMap.put("policyGroupName", "pg.name");
        aliasToColumnMap.put("emailAddress", "emailId");
        aliasToColumnMap.put("certificateUploadStatus", "certificateAlias");
        aliasToColumnMap.put("status", "approvalStatus");
        aliasToColumnMap.put("isActive", "isActive");
        aliasToColumnMap.put("createdDateTime", "crDtimes");
    }

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

        // Check mosip signed certificate expiry date
        X509Certificate decodedMosipSignedCert = MultiPartnerUtil.decodeCertificateData(originalCertDownloadResponseDto.getMosipSignedCertificateData());
        if (isCertificateExpired(decodedMosipSignedCert)) {
            originalCertDownloadResponseDto.setMosipSignedCertificateData("");
            originalCertDownloadResponseDto.setIsMosipSignedCertificateExpired(true);
        }

        // Check ca signed partner certificate expiry date
        X509Certificate decodedCaSignedCert = MultiPartnerUtil.decodeCertificateData(originalCertDownloadResponseDto.getCaSignedCertificateData());
        if (isCertificateExpired(decodedCaSignedCert)) {
            originalCertDownloadResponseDto.setCaSignedCertificateData("");
            originalCertDownloadResponseDto.setIsCaSignedCertificateExpired(true);
        }
    }

    public boolean isCertificateExpired(X509Certificate cert) {
        // Get the current date and time in UTC
        LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime expiryDate = cert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();

        // Check if the certificate has expired
        return expiryDate.isBefore(currentDateTime);
    }

    public Sort getSortingRequest (String fieldName, String sortType) {
        Sort sortingRequest = null;
        if (sortType.equalsIgnoreCase(PartnerConstants.ASC)) {
            sortingRequest = Sort.by(fieldName).ascending();
        }
        if (sortType.equalsIgnoreCase(PartnerConstants.DESC)) {
            sortingRequest = Sort.by(fieldName).descending();
        }
        return sortingRequest;
    }

    public boolean isPartnerAdmin(String roles) {
        if (roles.contains(PartnerConstants.PARTNER_ADMIN)) {
            return true;
        }
        return false;
    }

    public void validateGetAllPartnersRequestParameters(String sortFieldName, String sortType, int pageNo, int pageSize, String certificateUploadStatus) {
        // Validate sortFieldName
        if (sortFieldName != null && !aliasToColumnMap.containsKey(sortFieldName)) {
            LOGGER.error("Invalid sort field name: " + sortFieldName);
            throw new PartnerServiceException(ErrorCode.INVALID_SORT_FIELD.getErrorCode(),
                    String.format(ErrorCode.INVALID_SORT_FIELD.getErrorMessage(), sortFieldName));
        }

        // Validate sortType
        if (sortType != null &&
                !sortType.equalsIgnoreCase(PartnerConstants.ASC) &&
                !sortType.equalsIgnoreCase(PartnerConstants.DESC)) {
            LOGGER.error("Invalid sort type: " + sortType);
            throw new PartnerServiceException(ErrorCode.INVALID_SORT_TYPE.getErrorCode(),
                    String.format(ErrorCode.INVALID_SORT_TYPE.getErrorMessage(), sortType));
        }

        // Validate pageNo
        if (pageNo < 0) {
            LOGGER.error("Invalid page no: " + pageNo);
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_NO.getErrorCode(),
                    ErrorCode.INVALID_PAGE_NO.getErrorMessage());
        }

        // Validate pageSize
        if (pageSize <= 0) {
            LOGGER.error("Invalid page size: " + pageSize);
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_SIZE.getErrorCode(),
                    ErrorCode.INVALID_PAGE_SIZE.getErrorMessage());
        }

        if (!certificateUploadStatus.equals(CERTIFICATE_UPLOADED) && !certificateUploadStatus.equals(CERTIFICATE_NOT_UPLOADED)) {
            LOGGER.error("Invalid certificate status: " + certificateUploadStatus);
            throw new PartnerServiceException(ErrorCode.INVALID_CERTIFICATE_UPLOAD_STATUS.getErrorCode(),
                    ErrorCode.INVALID_CERTIFICATE_UPLOAD_STATUS.getErrorMessage());
        }
    }
}
