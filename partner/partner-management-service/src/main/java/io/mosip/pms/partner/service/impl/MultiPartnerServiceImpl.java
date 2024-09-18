package io.mosip.pms.partner.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.service.impl.FTPChipDetailServiceImpl;
import io.mosip.pms.device.request.dto.FtpChipCertDownloadRequestDto;
import io.mosip.pms.device.response.dto.FtpCertDownloadResponeDto;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class MultiPartnerServiceImpl implements MultiPartnerService {

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerServiceImpl.class);
    public static final String BLANK_STRING = "";
    public static final String DEVICE_PROVIDER = "Device_Provider";
    public static final String FTM_PROVIDER = "FTM_Provider";
    public static final String AUTH_PARTNER = "Auth_Partner";
    public static final String APPROVED = "approved";
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String YES = "YES";
    public static final String PENDING_APPROVAL = "pending_approval";
    public static final String VERSION = "1.0";

    @Value("${mosip.pms.api.id.partner.certificates.get}")
    private String getPartnerCertificatesId;

    @Value("${mosip.pms.api.id.policy.requests.get}")
    private String getPolicyRequestsId;

    @Value("${mosip.pms.api.id.approved.partner.ids.with.policy.groups.get}")
    private String getApprovedPartnerIdsWithPolicyGroupsId;

    @Value("${mosip.pms.api.id.auth.partners.policies.get}")
    private String getAuthPartnersPoliciesId;

    @Value("${mosip.pms.api.id.api.keys.for.auth.partners.get}")
    private String getApiKeysForAuthPartnersId;

    @Value("${mosip.pms.api.id.sbi.details.get}")
    private  String getSbiDetailsId;

    @Value("${mosip.pms.api.id.approved.device.provider.ids.get}")
    private  String getApprovedDeviceProviderIds;

    @Value("${mosip.pms.api.id.ftm.chip.details.get}")
    private String getFtmChipDetailsId;

    @Value("${mosip.pms.api.id.approved.ftm.provider.ids.get}")
    private String getApprovedFtmProviderIds;

    @Autowired
    PartnerServiceRepository partnerRepository;

    @Autowired
    PolicyGroupRepository policyGroupRepository;

    @Autowired
    AuthPolicyRepository authPolicyRepository;

    @Autowired
    PartnerPolicyRepository partnerPolicyRepository;

    @Autowired
    PartnerServiceImpl partnerServiceImpl;

    @Autowired
    FTPChipDetailServiceImpl ftpChipDetailServiceImpl;

    @Autowired
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @Autowired
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @Autowired
    DeviceDetailRepository deviceDetailRepository;

    @Autowired
    FTPChipDetailRepository ftpChipDetailRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PartnerHelper partnerHelper;

    @Override
    public ResponseWrapperV2<List<CertificateDto>> getPartnerCertificates() {
        ResponseWrapperV2<List<CertificateDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                List<CertificateDto> certificateDtoList = new ArrayList<>();
                for (Partner partner : partnerList) {
                    CertificateDto certificateDto = new CertificateDto();
                    try {
                        if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
                            LOGGER.info("Partner Id is null or empty for user id : " + userId);
                            throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
                                    ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
                        }
                        PartnerCertDownloadRequestDto requestDto = new PartnerCertDownloadRequestDto();
                        requestDto.setPartnerId(partner.getId());
                        PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = partnerServiceImpl.getPartnerCertificate(requestDto);
                        X509Certificate cert = MultiPartnerUtil.decodeCertificateData(partnerCertDownloadResponeDto.getCertificateData());

                        certificateDto.setIsCertificateAvailable(true);
                        certificateDto.setCertificateIssuedTo(getCertificateName(cert.getSubjectDN().getName()));
                        certificateDto.setCertificateUploadDateTime(cert.getNotBefore());
                        certificateDto.setCertificateExpiryDateTime(cert.getNotAfter());
                        certificateDto.setPartnerId(partner.getId());
                        certificateDto.setPartnerType(partner.getPartnerTypeCode());
                    } catch (PartnerServiceException ex) {
                        LOGGER.info("Could not fetch partner certificate :" + ex.getMessage());
                        certificateDto.setIsCertificateAvailable(false);
                        certificateDto.setPartnerId(partner.getId());
                        certificateDto.setPartnerType(partner.getPartnerTypeCode());
                    }
                    certificateDtoList.add(certificateDto);
                }
                responseWrapper.setResponse(certificateDtoList);
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getPartnerCertificates method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getPartnerCertificates method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getPartnerCertificatesId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<PolicyDto>> getPolicyRequests() {
        ResponseWrapperV2<List<PolicyDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                List<PolicyDto> policyDtoList = new ArrayList<>();
                for (Partner partner : partnerList) {
                    if (!skipDeviceOrFtmPartner(partner)) {
                        validatePartnerId(partner, userId);
                        validatePolicyGroupId(partner, userId);
                        PolicyGroup policyGroup = validatePolicyGroup(partner);
                        List<PartnerPolicyRequest> partnerPolicyRequestList = partner.getPartnerPolicyRequests();
                        if (!partnerPolicyRequestList.isEmpty()) {
                            for (PartnerPolicyRequest partnerPolicyRequest : partnerPolicyRequestList) {
                                AuthPolicy policyDetails = authPolicyRepository.findByPolicyGroupAndId(partner.getPolicyGroupId(), partnerPolicyRequest.getPolicyId());
                                if (Objects.nonNull(policyDetails)) {
                                    PolicyDto policyDto = new PolicyDto();
                                    policyDto.setPartnerId(partner.getId());
                                    policyDto.setPartnerType(partner.getPartnerTypeCode());

                                    policyDto.setPolicyGroupId(policyGroup.getId());
                                    policyDto.setPolicyGroupDescription(policyGroup.getDesc());
                                    policyDto.setPolicyGroupName(policyGroup.getName());

                                    policyDto.setPolicyId(policyDetails.getId());
                                    policyDto.setPolicyDescription(policyDetails.getDescr());
                                    policyDto.setPolicyName(policyDetails.getName());

                                    policyDto.setPartnerComments(partnerPolicyRequest.getRequestDetail());
                                    policyDto.setUpdatedDateTime(partnerPolicyRequest.getUpdDtimes());
                                    policyDto.setCreatedDateTime(partnerPolicyRequest.getCrDtimes());
                                    policyDto.setStatus(partnerPolicyRequest.getStatusCode());
                                    policyDtoList.add(policyDto);
                                } else {
                                    LOGGER.info("No matching policy not found for policy group ID :" + partner.getPolicyGroupId() + "and Policy ID :" + partnerPolicyRequest.getPolicyId());
                                    throw new PartnerServiceException(ErrorCode.MATCHING_POLICY_NOT_FOUND.getErrorCode(),
                                            ErrorCode.MATCHING_POLICY_NOT_FOUND.getErrorMessage());
                                }
                            }
                        }
                    }
                }
                responseWrapper.setResponse(policyDtoList);
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getPolicyRequests method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getPolicyRequests method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getPolicyRequestsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<PolicyGroupDto>> getApprovedPartnerIdsWithPolicyGroups() {
        ResponseWrapperV2<List<PolicyGroupDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                List<PolicyGroupDto> policyGroupDtoList = new ArrayList<>();
                for (Partner partner : partnerList) {
                    String partnerType = partner.getPartnerTypeCode();
                    // Ignore, If the partner is a DEVICE or FTM partnertype
                    if (!skipDeviceOrFtmPartner(partner)
                            && partner.getApprovalStatus().equalsIgnoreCase(APPROVED)) {
                        PolicyGroupDto policyGroupDto = new PolicyGroupDto();
                        validatePartnerId(partner, userId);
                        validatePolicyGroupId(partner, userId);
                        PolicyGroup policyGroup = validatePolicyGroup(partner);
                        policyGroupDto.setPartnerId(partner.getId());
                        policyGroupDto.setPartnerType(partner.getPartnerTypeCode());
                        policyGroupDto.setPolicyGroupId(partner.getPolicyGroupId());
                        policyGroupDto.setPolicyGroupName(policyGroup.getName());
                        policyGroupDto.setPolicyGroupDescription(policyGroup.getDesc());
                        policyGroupDtoList.add(policyGroupDto);
                    }
                }
                responseWrapper.setResponse(policyGroupDtoList);
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getApprovedPartnerIdsWithPolicyGroups method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getApprovedPartnerIdsWithPolicyGroups method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.POLICY_GROUP_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.POLICY_GROUP_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getApprovedPartnerIdsWithPolicyGroupsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAuthPartnersPolicies() {
        ResponseWrapperV2<List<ApprovedPolicyDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                List<ApprovedPolicyDto> approvedPolicyList = new ArrayList<>();
                for (Partner partner : partnerList) {
                    if (checkIfPartnerIsApprovedAuthPartner(partner)) {
                        validatePartnerId(partner, userId);
                        validatePolicyGroupId(partner, userId);
                        PolicyGroup policyGroup = validatePolicyGroup(partner);
                        ApprovedPolicyDto approvedPolicyDto = new ApprovedPolicyDto();
                        approvedPolicyDto.setPartnerId(partner.getId());
                        approvedPolicyDto.setPolicyGroupId(policyGroup.getId());
                        approvedPolicyDto.setPolicyGroupDescription(policyGroup.getDesc());
                        approvedPolicyDto.setPolicyGroupName(policyGroup.getName());
                        List<PartnerPolicyRequest> partnerPolicyRequestList = partner.getPartnerPolicyRequests();
                        List<ActivePolicyDto> activePolicyDtoList = new ArrayList<>();
                        if (!partnerPolicyRequestList.isEmpty()) {
                            for (PartnerPolicyRequest partnerPolicyRequest : partnerPolicyRequestList) {
                                if (partnerPolicyRequest.getStatusCode().equals(APPROVED)) {
                                    AuthPolicy policyDetails = authPolicyRepository.findActivePoliciesByPolicyGroupId(partner.getPolicyGroupId(), partnerPolicyRequest.getPolicyId());
                                    if (Objects.nonNull(policyDetails)) {
                                        ActivePolicyDto activePolicyDto = new ActivePolicyDto();
                                        activePolicyDto.setPolicyId(policyDetails.getId());
                                        activePolicyDto.setPolicyDescription(policyDetails.getDescr());
                                        activePolicyDto.setPolicyName(policyDetails.getName());
                                        activePolicyDtoList.add(activePolicyDto);
                                    } else {
                                        LOGGER.info("No matching policy not found for policy group ID :" + partner.getPolicyGroupId() + "and Policy ID :" + partnerPolicyRequest.getPolicyId());
                                        throw new PartnerServiceException(ErrorCode.MATCHING_POLICY_NOT_FOUND.getErrorCode(),
                                                ErrorCode.MATCHING_POLICY_NOT_FOUND.getErrorMessage());
                                    }
                                }
                            }
                            approvedPolicyDto.setActivePolicies(activePolicyDtoList);
                            approvedPolicyList.add(approvedPolicyDto);
                        } else {
                            approvedPolicyDto.setActivePolicies(activePolicyDtoList);
                            approvedPolicyList.add(approvedPolicyDto);
                        }
                    }
                }
                responseWrapper.setResponse(approvedPolicyList);
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAuthPartnersPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAuthPartnersPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getAuthPartnersPoliciesId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    public static boolean checkIfPartnerIsApprovedAuthPartner(Partner partner) {
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
        return partnerType.equals(AUTH_PARTNER) && approvalStatus.equals(APPROVED);
    }

    public static boolean checkIfPartnerIsDevicePartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        return partnerType.equals(DEVICE_PROVIDER);
    }

    public static boolean checkIfPartnerIsFtmPartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        return partnerType.equals(FTM_PROVIDER);
    }

    public static void validatePartnerId(Partner partner, String userId) {
        if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
            LOGGER.info("Partner Id is null or empty for user id : " + userId);
            throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
                    ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
        }
    }

    public static void validatePolicyGroupId(Partner partner, String userId) {
        if (Objects.isNull(partner.getPolicyGroupId()) || partner.getPolicyGroupId().equals(BLANK_STRING)) {
            LOGGER.info("Policy group Id is null or empty for user id : " + userId);
            throw new PartnerServiceException(ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorCode(),
                    ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorMessage());
        }
    }

    public static boolean skipDeviceOrFtmPartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        if (Objects.isNull(partnerType) || partnerType.equals(BLANK_STRING)) {
            LOGGER.info("Partner Type is null or empty for partner id : " + partner.getId());
            throw new PartnerServiceException(ErrorCode.PARTNER_TYPE_NOT_EXISTS.getErrorCode(),
                    ErrorCode.PARTNER_TYPE_NOT_EXISTS.getErrorMessage());
        }
        return partnerType.equals(DEVICE_PROVIDER) || partnerType.equals(FTM_PROVIDER);
    }

    private PolicyGroup validatePolicyGroup(Partner partner) throws PartnerServiceException {
        PolicyGroup policyGroup = policyGroupRepository.findPolicyGroupById(partner.getPolicyGroupId());
        if (Objects.isNull(policyGroup) || Objects.isNull(policyGroup.getName()) || policyGroup.getName().isEmpty()) {
            LOGGER.info("Policy Group is null or empty for partner id : {}", partner.getId());
            throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode(), ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
        }
        return policyGroup;
    }

    @Override
    public ResponseWrapperV2<List<ApiKeyResponseDto>> getApiKeysForAuthPartners() {
        ResponseWrapperV2<List<ApiKeyResponseDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                List<ApiKeyResponseDto> apiKeyResponseDtoList = new ArrayList<>();
                for (Partner partner : partnerList) {
                    if (checkIfPartnerIsApprovedAuthPartner(partner)) {
                        validatePartnerId(partner, userId);
                        validatePolicyGroupId(partner, userId);
                        List<PartnerPolicy> apiKeyRequestsList = partnerPolicyRepository.findAPIKeysByPartnerId(partner.getId());
                        if (!apiKeyRequestsList.isEmpty()) {
                            for (PartnerPolicy partnerPolicy : apiKeyRequestsList) {
                                Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(partnerPolicy.getPolicyId());
                                if (!authPolicy.isPresent()) {
                                    LOGGER.info("Policy does not exists.");
                                    throw new PartnerServiceException(ErrorCode.POLICY_NOT_EXIST.getErrorCode(),
                                            ErrorCode.POLICY_NOT_EXIST.getErrorMessage());
                                }
                                PolicyGroup policyGroup = authPolicy.get().getPolicyGroup();
                                if (Objects.isNull(policyGroup)) {
                                    LOGGER.info("Policy Group is null or empty");
                                    throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode(),
                                            ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
                                }
                                ApiKeyResponseDto apiKeyResponseDto = new ApiKeyResponseDto();
                                apiKeyResponseDto.setApiKeyLabel(partnerPolicy.getLabel());
                                if (partnerPolicy.getIsActive()) {
                                    apiKeyResponseDto.setStatus(ACTIVE);
                                } else {
                                    apiKeyResponseDto.setStatus(INACTIVE);
                                }
                                apiKeyResponseDto.setPartnerId(partner.getId());
                                apiKeyResponseDto.setPolicyGroupId(policyGroup.getId());
                                apiKeyResponseDto.setPolicyGroupName(policyGroup.getName());
                                apiKeyResponseDto.setPolicyGroupDescription(policyGroup.getDesc());
                                apiKeyResponseDto.setPolicyId(authPolicy.get().getId());
                                apiKeyResponseDto.setPolicyName(authPolicy.get().getName());
                                apiKeyResponseDto.setPolicyDescription(authPolicy.get().getDescr());
                                apiKeyResponseDto.setCreatedDateTime(partnerPolicy.getCrDtimes());
                                apiKeyResponseDtoList.add(apiKeyResponseDto);
                            }
                        }
                    }
                }
                responseWrapper.setResponse(apiKeyResponseDtoList);
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getApiKeysForAuthPartners method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getApiKeysForAuthPartners method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.API_KEY_REQUESTS_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.API_KEY_REQUESTS_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getApiKeysForAuthPartnersId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<SbiDetailsDto>> sbiDetails() {
        ResponseWrapperV2<List<SbiDetailsDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
            List<SbiDetailsDto> sbiDetailsDtoList = new ArrayList<>();
            for (Partner partner : partnerList) {
                validatePartnerId(partner, userId);
                if (checkIfPartnerIsDevicePartner(partner)) {
                    List<SecureBiometricInterface> secureBiometricInterfaceList = secureBiometricInterfaceRepository.findByProviderId(partner.getId());
                    if (!secureBiometricInterfaceList.isEmpty()) {
                        for (SecureBiometricInterface secureBiometricInterface : secureBiometricInterfaceList) {
                            SbiDetailsDto sbiDetailsDto = new SbiDetailsDto();
                            List<DeviceDetailSBI> deviceDetailSBIList = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiId(partner.getId(), secureBiometricInterface.getId());
                            sbiDetailsDto.setSbiId(secureBiometricInterface.getId());
                            sbiDetailsDto.setPartnerId(partner.getId());
                            sbiDetailsDto.setSbiVersion(secureBiometricInterface.getSwVersion());
                            sbiDetailsDto.setStatus(secureBiometricInterface.getApprovalStatus());
                            sbiDetailsDto.setSbiActive(secureBiometricInterface.isActive());
                            sbiDetailsDto.setSbiExpired(checkIfSbiExpired(secureBiometricInterface));
                            sbiDetailsDto.setCountOfApprovedDevices(countDevices(deviceDetailSBIList, APPROVED));
                            sbiDetailsDto.setCountOfPendingDevices(countDevices(deviceDetailSBIList, PENDING_APPROVAL));
                            sbiDetailsDto.setSbiCreatedDateTime(secureBiometricInterface.getSwCreateDateTime());
                            sbiDetailsDto.setSbiExpiryDateTime(secureBiometricInterface.getSwExpiryDateTime());
                            sbiDetailsDto.setCreatedDateTime(secureBiometricInterface.getCrDtimes());

                            sbiDetailsDtoList.add(sbiDetailsDto);
                        }
                    }
                }
            }
            responseWrapper.setResponse(sbiDetailsDtoList);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In sbiDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In sbiDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.SBI_DETAILS_LIST_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.SBI_DETAILS_LIST_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getSbiDetailsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    private String countDevices(List<DeviceDetailSBI> deviceDetailSBIList, String status) {
        int count = 0;
        if (deviceDetailSBIList.isEmpty()) {
            return String.valueOf(count);
        }
        for (DeviceDetailSBI deviceDetailSBI : deviceDetailSBIList) {
            Optional<DeviceDetail> deviceDetail = deviceDetailRepository.
                    findByIdAndDeviceProviderId(deviceDetailSBI.getId().getDeviceDetailId(), deviceDetailSBI.getProviderId());
            if (deviceDetail.isPresent()) {
                if (status.equals(APPROVED)) {
                    if (deviceDetail.get().getApprovalStatus().equals(status) && deviceDetail.get().getIsActive()) {
                        count++;
                    }
                }
                if (status.equals(PENDING_APPROVAL)) {
                    if (deviceDetail.get().getApprovalStatus().equals(status)) {
                        count++;
                    }
                }
            }
        }
        return String.valueOf(count);
    }

    private boolean checkIfSbiExpired(SecureBiometricInterface secureBiometricInterface) {
        return !secureBiometricInterface.getSwExpiryDateTime().toLocalDate().isAfter(LocalDate.now());
    }

    @Override
    public ResponseWrapperV2<List<DeviceProviderDto>> approvedDeviceProviderIds() {
        ResponseWrapperV2<List<DeviceProviderDto>> responseWrapper =  new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
            List<DeviceProviderDto> approvedDeviceProviderIds = new ArrayList<>();
            for (Partner partner : partnerList) {
                validatePartnerId(partner, userId);
                if (checkIfPartnerIsDevicePartner(partner)
                        && partner.getApprovalStatus().equalsIgnoreCase(APPROVED)) {
                    DeviceProviderDto deviceProviderDto = new DeviceProviderDto();
                    deviceProviderDto.setPartnerId(partner.getId());

                    approvedDeviceProviderIds.add(deviceProviderDto);
                }
            }
            responseWrapper.setResponse(approvedDeviceProviderIds);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In approvedDeviceProviderIds method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In approvedDeviceProviderIds method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.APPROVED_DEVICE_PROVIDER_IDS_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.APPROVED_DEVICE_PROVIDER_IDS_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getApprovedDeviceProviderIds);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<FtmChipDetailsDto>> ftmChipDetails() {
        ResponseWrapperV2<List<FtmChipDetailsDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            List<FtmChipDetailsDto> ftmChipDetailsDtoList = new ArrayList<>();
            if (!partnerList.isEmpty()) {
                for (Partner partner : partnerList) {
                    if (checkIfPartnerIsFtmPartner(partner)) {
                        validatePartnerId(partner, userId);
                        List<FTPChipDetail> ftpChipDetailList = ftpChipDetailRepository.findByProviderId(partner.getId());
                        if(!ftpChipDetailList.isEmpty()) {
                            for(FTPChipDetail ftpChipDetail: ftpChipDetailList) {
                                FtmChipDetailsDto ftmChipDetailsDto = new FtmChipDetailsDto();
                                // Get certificate data if available
                                if (ftpChipDetail.getCertificateAlias() != null) {
                                    ftmChipDetailsDto.setIsCertificateAvailable(true);
                                    FtpChipCertDownloadRequestDto requestDto = new FtpChipCertDownloadRequestDto();
                                    requestDto.setFtpChipDetailId(ftpChipDetail.getFtpChipDetailId());
                                    FtpCertDownloadResponeDto ftpCertDownloadResponeDto = ftpChipDetailServiceImpl.getCertificate(requestDto);
                                    X509Certificate cert = MultiPartnerUtil.decodeCertificateData(ftpCertDownloadResponeDto.getCertificateData());

                                    ftmChipDetailsDto.setCertificateUploadDateTime(cert.getNotBefore());
                                    ftmChipDetailsDto.setCertificateExpiryDateTime(cert.getNotAfter());

                                    // Check the certificate expiration status
                                    LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of("UTC"));
                                    LocalDateTime certExpiryDate = cert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
                                    ftmChipDetailsDto.setIsCertificateExpired(certExpiryDate.isBefore(currentDateTime));
                                } else {
                                    ftmChipDetailsDto.setIsCertificateAvailable(false);
                                    ftmChipDetailsDto.setIsCertificateExpired(false);
                                }
                                ftmChipDetailsDto.setFtmId(ftpChipDetail.getFtpChipDetailId());
                                ftmChipDetailsDto.setPartnerId(ftpChipDetail.getFtpProviderId());
                                ftmChipDetailsDto.setMake(ftpChipDetail.getMake());
                                ftmChipDetailsDto.setModel(ftpChipDetail.getModel());
                                ftmChipDetailsDto.setStatus(ftpChipDetail.getApprovalStatus());
                                ftmChipDetailsDto.setIsActive(ftpChipDetail.isActive());
                                ftmChipDetailsDto.setCreatedDateTime(ftpChipDetail.getCrDtimes());
                                ftmChipDetailsDtoList.add(ftmChipDetailsDto);
                            }
                        }
                    }
                }
            }
            responseWrapper.setResponse(ftmChipDetailsDtoList);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In ftmChipDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In ftmChipDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.FTM_CHIP_DETAILS_LIST_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.FTM_CHIP_DETAILS_LIST_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getFtmChipDetailsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<FtmProviderDto>> approvedFTMProviderIds() {
        ResponseWrapperV2<List<FtmProviderDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            List <FtmProviderDto> approvedFtmProviderIds = new ArrayList<>();
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
            for (Partner partner : partnerList) {
                validatePartnerId(partner, userId);
                if (checkIfPartnerIsFtmPartner(partner)
                        && partner.getApprovalStatus().equalsIgnoreCase(APPROVED)) {
                    FtmProviderDto ftmProviderDto = new FtmProviderDto();
                    ftmProviderDto.setPartnerId(partner.getId());

                    approvedFtmProviderIds.add(ftmProviderDto);
                }
            }
            responseWrapper.setResponse(approvedFtmProviderIds);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In approvedFTMProviderIds method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In approvedFTMProviderIds method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.APPROVED_FTM_PROVIDER_IDS_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.APPROVED_FTM_PROVIDER_IDS_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getApprovedFtmProviderIds);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserId() {
        String userId = authUserDetails().getUserId();
        return userId;
    }

    public static String getCertificateName(String subjectDN) {
        String[] parts = subjectDN.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("CN=")) {
                return part.trim().substring(3);
            }
        }
        return BLANK_STRING;
    }
}
