package io.mosip.pms.partner.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

@Component
public class PartnerHelper {

    private static final Logger LOGGER = PMSLogger.getLogger(PartnerHelper.class);
    public static final String APPROVED = "approved";
    public static final String PENDING_APPROVAL = "pending_approval";
    public static final String REJECTED = "rejected";
    public static final String DEVICE_PROVIDER = "Device_Provider";
    public static final String FTM_PROVIDER = "FTM_Provider";
    public static final String AUTH_PARTNER = "Auth_Partner";
    public static final String BLANK_STRING = "";

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
        ftmAliasToColumnMap.put("ftmId", "ftpChipDetailId");
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

    public final Map<String, String> trustCertificateAliasToColumnMap = new HashMap<>();
    {
        trustCertificateAliasToColumnMap.put("caCertificateType", "caCertificateType");
        trustCertificateAliasToColumnMap.put("certificateId", "certId");
        trustCertificateAliasToColumnMap.put("partnerDomain", "partnerDomain");
        trustCertificateAliasToColumnMap.put("issuedTo", "certSubject");
        trustCertificateAliasToColumnMap.put("issuedBy", "certIssuer");
        trustCertificateAliasToColumnMap.put("validFrom", "certNotBefore");
        trustCertificateAliasToColumnMap.put("validTill", "certNotAfter");
        trustCertificateAliasToColumnMap.put("uploadedDateTime", "createdtimes");
    }

    @Autowired
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @Autowired
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @Autowired
    DeviceDetailRepository deviceDetailRepository;

    @Autowired
    PolicyGroupRepository policyGroupRepository;

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
        } else if (secureBiometricInterface.get().getApprovalStatus().equals(PENDING_APPROVAL)) {
            LOGGER.info("sessionId", "idType", "id", "Sbi is not approved.");
            throw new PartnerServiceException(ErrorCode.PENDING_APPROVAL_SBI.getErrorCode(),
                    ErrorCode.PENDING_APPROVAL_SBI.getErrorMessage());
        } else if (secureBiometricInterface.get().getApprovalStatus().equals(REJECTED)) {
            LOGGER.info("sessionId", "idType", "id", "Sbi is already rejected.");
            throw new PartnerServiceException(ErrorCode.REJECTED_SBI.getErrorCode(),
                    ErrorCode.REJECTED_SBI.getErrorMessage());
        } else if (secureBiometricInterface.get().getApprovalStatus().equals(APPROVED) && !secureBiometricInterface.get().isActive()) {
            LOGGER.info("sessionId", "idType", "id", "Sbi is already deactivated.");
            throw new PartnerServiceException(ErrorCode.DEACTIVATED_SBI.getErrorCode(),
                    ErrorCode.DEACTIVATED_SBI.getErrorMessage());
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

        if (getApiResponse.get("response") == null && getApiResponse.containsKey(PartnerConstants.ERRORS)) {
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

        if (getApiResponse.get("response") == null) {
            LOGGER.error("Got null response from {}", environment.getProperty(uriProperty));
            throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
                    ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
        }
        responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")), responseType);
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
        ftmCertificateDownloadResponseDto.setCaSignedCertExpiryDateTime(decodedCaSignedCert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        ftmCertificateDownloadResponseDto.setCaSignedCertUploadDateTime(decodedCaSignedCert.getNotBefore().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
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

    public void validateRequestParameters(Map<String, String> aliasToColumnMap, String sortFieldName, String sortType, Integer pageNo, Integer pageSize) {
        // Validate sortFieldName and sortType
        if ((Objects.nonNull(sortFieldName) && Objects.isNull(sortType)) || (Objects.isNull(sortFieldName) && Objects.nonNull(sortType))) {
            LOGGER.error("Both sortFieldName and sortType must be provided together.");
            throw new PartnerServiceException(ErrorCode.INVALID_SORT_PARAMETERS.getErrorCode(),
                    ErrorCode.INVALID_SORT_PARAMETERS.getErrorMessage());
        }

        // Validate pageNo and pageSize
        if ((Objects.nonNull(pageNo) && Objects.isNull(pageSize)) || (Objects.isNull(pageNo) && Objects.nonNull(pageSize))) {
            LOGGER.error("Both pageNo and pageSize must be provided together.");
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_PARAMETERS.getErrorCode(),
                    ErrorCode.INVALID_PAGE_PARAMETERS.getErrorMessage());
        }

        if (isSortingRequestedWithoutPagination(sortFieldName, sortType, pageNo, pageSize)) {
            LOGGER.error("Please provide pagination parameters ('pageNo' and 'pageSize') when requesting sorted data.");
            throw new PartnerServiceException(
                    ErrorCode.MISSING_PAGINATION_FOR_SORT.getErrorCode(),
                    ErrorCode.MISSING_PAGINATION_FOR_SORT.getErrorMessage()
            );
        }

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
        if (Objects.nonNull(pageNo) && pageNo < 0) {
            LOGGER.error("Invalid page no: " + pageNo);
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_NO.getErrorCode(),
                    ErrorCode.INVALID_PAGE_NO.getErrorMessage());
        }

        // Validate pageSize
        if (Objects.nonNull(pageSize) && pageSize <= 0) {
            LOGGER.error("Invalid page size: " + pageSize);
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_SIZE.getErrorCode(),
                    ErrorCode.INVALID_PAGE_SIZE.getErrorMessage());
        }
    }

    private boolean isSortingRequestedWithoutPagination(String sortFieldName, String sortType, Integer pageNo, Integer pageSize) {
        return Objects.nonNull(sortFieldName) && Objects.nonNull(sortType)
                && Objects.isNull(pageNo) && Objects.isNull(pageSize);
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

            String apiUrl = UriComponentsBuilder
                    .fromHttpUrl(Objects.requireNonNull(environment.getProperty("mosip.iam.admin-url")))
                    .path(Objects.requireNonNull(environment.getProperty("mosip.iam.users-extn-url")))
                    .queryParam("username", "{username}")
                    .build()
                    .toUriString();
            MediaType mediaType = MediaType.APPLICATION_JSON;

            List<Map<String, Object>> getApiResponse = restUtil.getApiWithContentType(apiUrl, pathSegments, List.class, mediaType);

            // Check if the response is empty or null
            if (getApiResponse == null || getApiResponse.isEmpty()) {
                LOGGER.error("Error while fetching user details for partnerId:", partnerId);
                return Optional.empty();
            }

            return Optional.ofNullable(mapper.readValue(mapper.writeValueAsString(getApiResponse.get(0)), KeycloakUserDto.class));
        } catch (Exception e) {
            LOGGER.error("Error while fetching user details for partnerId: {}", partnerId, e.getStackTrace());
            return Optional.empty();
        }
    }


    public boolean checkIfPartnerIsDevicePartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        return partnerType.equals(DEVICE_PROVIDER);
    }

    public boolean checkIfPartnerIsFtmPartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        return partnerType.equals(FTM_PROVIDER);
    }

    public boolean skipDeviceOrFtmPartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        if (Objects.isNull(partnerType) || partnerType.equals(BLANK_STRING)) {
            LOGGER.info("Partner Type is null or empty for partner id : " + partner.getId());
            throw new PartnerServiceException(ErrorCode.PARTNER_TYPE_NOT_EXISTS.getErrorCode(),
                    ErrorCode.PARTNER_TYPE_NOT_EXISTS.getErrorMessage());
        }
        return partnerType.equals(DEVICE_PROVIDER) || partnerType.equals(FTM_PROVIDER);
    }

    public void validatePolicyGroupId(Partner partner, String userId) {
        if (Objects.isNull(partner.getPolicyGroupId()) || partner.getPolicyGroupId().equals(BLANK_STRING)) {
            LOGGER.info("Policy group Id is null or empty for user id : " + userId);
            throw new PartnerServiceException(ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorCode(),
                    ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorMessage());
        }
    }

    public PolicyGroup validatePolicyGroup(Partner partner) throws PartnerServiceException {
        PolicyGroup policyGroup = policyGroupRepository.findPolicyGroupById(partner.getPolicyGroupId());
        if (Objects.isNull(policyGroup) || Objects.isNull(policyGroup.getName()) || policyGroup.getName().isEmpty()) {
            LOGGER.info("Policy Group is null or empty for partner id : {}", partner.getId());
            throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode(), ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
        }
        return policyGroup;
    }

    public void validatePartnerId(Partner partner, String userId) {
        if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
            LOGGER.info("Partner Id is null or empty for user id : " + userId);
            throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
                    ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
        }
    }

    public void validateIfPartnerIsApprovedAuthPartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        String approvalStatus = partner.getApprovalStatus();
        if (Objects.isNull(partnerType) || partnerType.equals(BLANK_STRING)) {
            LOGGER.info("Partner Type is null or empty for partner id : " + partner.getId());
            throw new PartnerServiceException(ErrorCode.PARTNER_TYPE_NOT_EXISTS.getErrorCode(),
                    ErrorCode.PARTNER_TYPE_NOT_EXISTS.getErrorMessage());
        }
        if ((Objects.isNull(approvalStatus) || approvalStatus.equals(BLANK_STRING))) {
            LOGGER.info("Approval status is null or empty for partner id : " + partner.getId());
            throw new PartnerServiceException(ErrorCode.APPROVAL_STATUS_NOT_EXISTS.getErrorCode(),
                    ErrorCode.APPROVAL_STATUS_NOT_EXISTS.getErrorMessage());
        }
        if (!partnerType.equals(AUTH_PARTNER)) {
            LOGGER.info("The specified partner is not of type Authentication Partner " + partner.getId());
            throw new PartnerServiceException(ErrorCode.NOT_AUTH_PARTNER_TYPE_ERROR.getErrorCode(),
                    ErrorCode.NOT_AUTH_PARTNER_TYPE_ERROR.getErrorMessage());
        }
        if (!approvalStatus.equals(APPROVED)) {
            LOGGER.info("The specified partner is not of type Authentication Partner " + partner.getId());
            throw new PartnerServiceException(ErrorCode.PARTNER_NOT_APPROVED_ERROR.getErrorCode(),
                    ErrorCode.PARTNER_NOT_APPROVED_ERROR.getErrorMessage());
        }
    }
}
