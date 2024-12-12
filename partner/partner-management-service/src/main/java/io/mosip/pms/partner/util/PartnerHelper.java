package io.mosip.pms.partner.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.entity.Partner;
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
import io.mosip.pms.partner.dto.KeycloakUserDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
import io.mosip.pms.partner.response.dto.OriginalCertDownloadResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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

    public final Map<String, String> partnerAliasToColumnMap = new HashMap<>();
    {
        partnerAliasToColumnMap.put("partnerId", "id");
        partnerAliasToColumnMap.put("partnerType", "partnerTypeCode");
        partnerAliasToColumnMap.put("orgName", "name");
        partnerAliasToColumnMap.put("policyGroupId", "policyGroupId");
        partnerAliasToColumnMap.put("policyGroupName", "pg.name");
        partnerAliasToColumnMap.put("emailAddress", "emailId");
        partnerAliasToColumnMap.put("certificateUploadStatus", "certificateAlias");
        partnerAliasToColumnMap.put("status", "approvalStatus");
        partnerAliasToColumnMap.put("isActive", "isActive");
        partnerAliasToColumnMap.put("createdDateTime", "crDtimes");
    }

    public final Map<String, String> partnerPolicyMappingAliasToColumnMap = new HashMap<>();
    {
        partnerPolicyMappingAliasToColumnMap.put("partnerId", "partnerId");
        partnerPolicyMappingAliasToColumnMap.put("partnerType", "p.partnerTypeCode");
        partnerPolicyMappingAliasToColumnMap.put("orgName", "p.name");
        partnerPolicyMappingAliasToColumnMap.put("policyId", "policyId");
        partnerPolicyMappingAliasToColumnMap.put("policyGroupName", "p.policyGroup.name");
        partnerPolicyMappingAliasToColumnMap.put("policyName", "ap.name");
        partnerPolicyMappingAliasToColumnMap.put("status", "statusCode");
        partnerPolicyMappingAliasToColumnMap.put("requestDetail", "requestDetail");
        partnerPolicyMappingAliasToColumnMap.put("createdDateTime", "createdDateTime");
    }

    public final Map<String, String> oidcClientsAliasToColumnMap = new HashMap<>();
    {
        oidcClientsAliasToColumnMap.put("partnerId", "rpId");
        oidcClientsAliasToColumnMap.put("orgName", "p.name");
        oidcClientsAliasToColumnMap.put("policyGroupName", "pg.name");
        oidcClientsAliasToColumnMap.put("policyName", "ap.name");
        oidcClientsAliasToColumnMap.put("clientId", "id");
        oidcClientsAliasToColumnMap.put("clientName", "name");
        oidcClientsAliasToColumnMap.put("status", "status");
        oidcClientsAliasToColumnMap.put("createdDateTime", "createdDateTime");
    }

    public final Map<String, String> apiKeyAliasToColumnMap = new HashMap<>();
    {
        apiKeyAliasToColumnMap.put("partnerId", "partnerId");
        apiKeyAliasToColumnMap.put("apiKeyLabel", "label");
        apiKeyAliasToColumnMap.put("orgName", "p.name");
        apiKeyAliasToColumnMap.put("policyName", "ap.name");
        apiKeyAliasToColumnMap.put("policyGroupName", "pg.name");
        apiKeyAliasToColumnMap.put("status", "isActive");
        apiKeyAliasToColumnMap.put("createdDateTime", "createdDateTime");
    }

    public final Map<String, String> ftmAliasToColumnMap = new HashMap<>();
    {
        ftmAliasToColumnMap.put("partnerId", "ftpProviderId");
        ftmAliasToColumnMap.put("orgName", "partnerOrganizationName");
        ftmAliasToColumnMap.put("make", "make");
        ftmAliasToColumnMap.put("model", "model");
        ftmAliasToColumnMap.put("status", "approvalStatus");
        ftmAliasToColumnMap.put("createdDateTime", "crDtimes");
    }

    public final Map<String, String> sbiAliasToColumnMap = new HashMap<>();
    {
        sbiAliasToColumnMap.put("partnerId", "providerId");
        sbiAliasToColumnMap.put("orgName", "partnerOrgName");
        sbiAliasToColumnMap.put("partnerType", "p.partnerTypeCode");
        sbiAliasToColumnMap.put("sbiId", "id");
        sbiAliasToColumnMap.put("sbiVersion", "swVersion");
        sbiAliasToColumnMap.put("sbiCreatedDateTime", "swCreateDateTime");
        sbiAliasToColumnMap.put("sbiExpiryDateTime", "swExpiryDateTime");
        sbiAliasToColumnMap.put("status", "approvalStatus");
        sbiAliasToColumnMap.put("createdDateTime", "crDtimes");
        sbiAliasToColumnMap.put("sbiExpiryStatus", "sbiExpiryStatus");
        sbiAliasToColumnMap.put("countOfAssociatedDevices", "countOfAssociatedDevices");
    }

    public final Map<String, String> deviceAliasToColumnMap = new HashMap<>();
    {
        deviceAliasToColumnMap.put("deviceId", "id");
        deviceAliasToColumnMap.put("sbiId", "s.id");
        deviceAliasToColumnMap.put("sbiVersion", "s.swVersion");
        deviceAliasToColumnMap.put("partnerId", "deviceProviderId");
        deviceAliasToColumnMap.put("orgName", "partnerOrganizationName");
        deviceAliasToColumnMap.put("deviceType", "deviceTypeCode");
        deviceAliasToColumnMap.put("deviceSubType", "deviceSubTypeCode");
        deviceAliasToColumnMap.put("make", "make");
        deviceAliasToColumnMap.put("model", "model");
        deviceAliasToColumnMap.put("status", "approvalStatus");
        deviceAliasToColumnMap.put("createdDateTime", "crDtimes");
    }

    public final Map<String, String> caCertificateAliasToColumnMap = new HashMap<>();
    {
        caCertificateAliasToColumnMap.put("caCertificateType", "caCertificateType");
        caCertificateAliasToColumnMap.put("certificateId", "certId");
        caCertificateAliasToColumnMap.put("partnerDomain", "partnerDomain");
        caCertificateAliasToColumnMap.put("issuedTo", "certSubject");
        caCertificateAliasToColumnMap.put("issuedBy", "certIssuer");
        caCertificateAliasToColumnMap.put("validFrom", "certNotBefore");
        caCertificateAliasToColumnMap.put("validTill", "certNotAfter");
        caCertificateAliasToColumnMap.put("uploadedDateTime", "createdtimes");
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

    public void populateFtmCertificateExpiryState(FtmCertificateDownloadResponseDto ftmCertificateDownloadResponseDto) {
        ftmCertificateDownloadResponseDto.setIsMosipSignedCertificateExpired(false);
        ftmCertificateDownloadResponseDto.setIsCaSignedCertificateExpired(false);

        X509Certificate decodedMosipSignedCert = MultiPartnerUtil.decodeCertificateData(ftmCertificateDownloadResponseDto.getMosipSignedCertificateData());
        ftmCertificateDownloadResponseDto.setMosipSignedCertExpiryDateTime(decodedMosipSignedCert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        ftmCertificateDownloadResponseDto.setMosipSignedCertUploadDateTime(decodedMosipSignedCert.getNotBefore().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        // Check mosip signed certificate expiry date
        if (isCertificateExpired(decodedMosipSignedCert)) {
            ftmCertificateDownloadResponseDto.setMosipSignedCertificateData("");
            ftmCertificateDownloadResponseDto.setIsMosipSignedCertificateExpired(true);
        }

        X509Certificate decodedCaSignedCert = MultiPartnerUtil.decodeCertificateData(ftmCertificateDownloadResponseDto.getCaSignedCertificateData());
        ftmCertificateDownloadResponseDto.setCaSignedCertExpiryDateTime(decodedMosipSignedCert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        ftmCertificateDownloadResponseDto.setCaSignedCertUploadDateTime(decodedMosipSignedCert.getNotBefore().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        // Check ca signed partner certificate expiry date
        if (isCertificateExpired(decodedCaSignedCert)) {
            ftmCertificateDownloadResponseDto.setCaSignedCertificateData("");
            ftmCertificateDownloadResponseDto.setIsCaSignedCertificateExpired(true);
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

    public void validateRequestParameters(Map<String, String> aliasToColumnMap, String sortFieldName, String sortType, int pageNo, int pageSize) {
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
    }

    public void checkIfPartnerIsNotActive(Partner partner) {
        if (!partner.getIsActive()) {
            LOGGER.error("Partner is not Active with id {}", partner.getId());
            throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
                    ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
        }
    }

    public Optional<KeycloakUserDto> getUserDetailsByPartnerId(String partnerId) throws Exception {
        try {
            Map<String, String> pathSegments = Map.of("username", partnerId);

            String apiUrl = environment.getProperty("auth.server.admin.uri") + "/users?username={username}";
            MediaType mediaType = MediaType.APPLICATION_JSON;

            List<Map<String, Object>> getApiResponse = restUtil.getApiWithContentType(apiUrl, pathSegments, List.class, mediaType);

            // Check if the response is empty or null
            if (getApiResponse == null || getApiResponse.isEmpty()) {
                LOGGER.error("Error while fetching user details for partnerId:", partnerId);
                return Optional.empty();
            }

            return Optional.ofNullable(mapper.readValue(mapper.writeValueAsString(getApiResponse.get(0)), KeycloakUserDto.class));
        } catch (Exception e) {
            LOGGER.error("Error while fetching user details for partnerId: {}", partnerId, e);
            return Optional.empty();
        }
    }

}
