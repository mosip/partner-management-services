package io.mosip.pms.partner.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.UserDetails;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.UserDetailsRepository;
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
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.response.dto.SbiDetailsResponseDto;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.mosip.pms.partner.util.MultiPartnerHelper;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.sql.Timestamp;
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
    public static final String REJECTED = "rejected";
    public static final String VERSION = "1.0";

    @Value("${mosip.pms.api.id.all.certificates.details.get}")
    private String getAllCertificatesDetailsId;

    @Value("${mosip.pms.api.id.all.requested.policies.get}")
    private String getAllRequestedPoliciesId;

    @Value("${mosip.pms.api.id.all.approved.partner.ids.with.policy.groups.get}")
    private String getAllApprovedPartnerIdsWithPolicyGroupsId;

    @Value("${mosip.pms.api.id.all.approved.auth.partners.policies.get}")
    private String getAllApprovedAuthPartnersPoliciesId;

    @Value("${mosip.pms.api.id.all.api.keys.for.auth.partners.get}")
    private String getAllApiKeysForAuthPartnersId;

    @Value("${mosip.pms.api.id.save.user.consent.given.post}")
    private String postSaveUserConsentGivenId;

    @Value("${mosip.pms.api.id.user.consent.given.get}")
    private String getUserConsentGivenId;

    @Value("${mosip.pms.api.id.all.sbi.details.get}")
    private  String getSbiDetailsId;

    @Value("${mosip.pms.api.id.all.approved.device.provider.ids.get}")
    private  String getApprovedDeviceProviderIds;

    @Value("${mosip.pms.api.id.all.devices.for.sbi.get}")
    private  String getSbiDevicesId;

    @Value("${mosip.pms.api.id.add.inactive.device.mapping.to.sbi.id.post}")
    private  String postInactiveMappingDeviceToSbiId;

    @Value("${mosip.pms.api.id.deactivate.device.post}")
    private  String postDeactivateDevice;

    @Value("${mosip.pms.api.id.deactivate.sbi.post}")
    private  String postDeactivateSbi;

    @Value("${mosip.pms.api.id.ftm.chip.details.get}")
    private String getFtmChipDetails;

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
    UserDetailsRepository userDetailsRepository;

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
    MultiPartnerHelper multiPartnerHelper;

    @Override
    public ResponseWrapperV2<List<CertificateDto>> getAllCertificateDetails() {
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
                        certificateDto.setCertificateName(getCertificateName(cert.getSubjectDN().getName()));
                        certificateDto.setCertificateUploadDate(cert.getNotBefore());
                        certificateDto.setCertificateExpiryDate(cert.getNotAfter());
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
            LOGGER.info("sessionId", "idType", "id", "In getAllCertificateDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllCertificateDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getAllCertificatesDetailsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<PolicyDto>> getAllRequestedPolicies() {
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
                                    policyDto.setUpdDtimes(partnerPolicyRequest.getUpdDtimes());
                                    policyDto.setCreateDate(partnerPolicyRequest.getCrDtimes());
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
            LOGGER.info("sessionId", "idType", "id", "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getAllRequestedPoliciesId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<PolicyGroupDto>> getAllApprovedPartnerIdsWithPolicyGroups() {
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
            LOGGER.info("sessionId", "idType", "id", "In getAllApprovedPolicyGroups method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllApprovedPolicyGroups method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.POLICY_GROUP_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.POLICY_GROUP_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getAllApprovedPartnerIdsWithPolicyGroupsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAllApprovedAuthPartnerPolicies() {
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
                        approvedPolicyDto.setPartnerType(partner.getPartnerTypeCode());
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
                                        activePolicyDto.setStatus(partnerPolicyRequest.getStatusCode());
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
            LOGGER.info("sessionId", "idType", "id", "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getAllApprovedAuthPartnersPoliciesId);
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
    public ResponseWrapperV2<List<ApiKeyResponseDto>> getAllApiKeysForAuthPartners() {
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
                                apiKeyResponseDto.setPolicyNameDescription(authPolicy.get().getDescr());
                                apiKeyResponseDto.setCrDtimes(partnerPolicy.getCrDtimes());
                                apiKeyResponseDto.setUpdDtimes(partnerPolicy.getUpdDtimes());
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
            LOGGER.info("sessionId", "idType", "id", "In getAllApiKeysForAuthPartners method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllApiKeysForAuthPartners method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.API_KEY_REQUESTS_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.API_KEY_REQUESTS_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getAllApiKeysForAuthPartnersId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    private String getUserBy() {
        String crBy = authUserDetails().getMail();
        return crBy;
    }

    @Override
    public ResponseWrapperV2<UserDetailsDto> saveUserConsentGiven() {
        ResponseWrapperV2<UserDetailsDto> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                UserDetails userDetails = new UserDetails();

                LocalDateTime nowDate = LocalDateTime.now();
                userDetails.setConsentGiven(YES);
                userDetails.setConsentGivenDtimes(nowDate);

                Optional<UserDetails> optionalEntity = userDetailsRepository.findByUserId(userId);
                if (optionalEntity.isPresent()) {
                    UserDetails entity = optionalEntity.get();
                    userDetails.setId(entity.getId());
                    userDetails.setUpdBy(this.getUserBy());
                    userDetails.setUpdDtimes(nowDate);
                    userDetails.setCrBy(entity.getCrBy());
                    userDetails.setCrDtimes(entity.getCrDtimes());
                    userDetails.setUserId(entity.getUserId());
                } else {
                    userDetails.setId(PartnerUtil.generateUUID("id", "", 36));
                    userDetails.setCrBy(this.getUserBy());
                    userDetails.setCrDtimes(nowDate);
                    userDetails.setUserId(userId);
                }
                UserDetails respEntity = userDetailsRepository.save(userDetails);
                LOGGER.info("sessionId", "idType", "id", "saving user consent data for user id : ", userId);

                UserDetailsDto userDetailsDto = new UserDetailsDto();
                userDetailsDto.setConsentGiven(true);
                userDetailsDto.setUserId(respEntity.getUserId());
                userDetailsDto.setConsentGivenDtimes(respEntity.getConsentGivenDtimes());

                responseWrapper.setResponse(userDetailsDto);
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In saveUserConsentGiven method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception e) {
            LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
            LOGGER.error("sessionId", "idType", "id", "In saveUserConsentGiven method of MultiPartnerServiceImpl - " + e.getMessage());
            String errorCode = ErrorCode.PMS_CONSENT_UNABLE_TO_ADD.getErrorCode();
            String errorMessage = ErrorCode.PMS_CONSENT_UNABLE_TO_ADD.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(postSaveUserConsentGivenId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<UserDetailsDto> isUserConsentGiven() {
        ResponseWrapperV2<UserDetailsDto> responseWrapper =  new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                UserDetailsDto userDetailsDto = new UserDetailsDto();
                userDetailsDto.setUserId(userId);
                LOGGER.info("sessionId", "idType", "id", "fetching consent status from db for user :", userId);
                Optional<UserDetails> optionalEntity = userDetailsRepository.findByUserId(userId);
                if (optionalEntity.isPresent()) {
                    UserDetails entity = optionalEntity.get();
                    if (entity.getConsentGiven().equals(YES)) {
                        userDetailsDto.setConsentGiven(true);
                        userDetailsDto.setConsentGivenDtimes(entity.getConsentGivenDtimes());
                    }
                }
                responseWrapper.setResponse(userDetailsDto);
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In isUserConsentGiven method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception e) {
            LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
            LOGGER.error("sessionId", "idType", "id", "In isUserConsentGiven method of MultiPartnerServiceImpl - " + e.getMessage());
            String errorCode = ErrorCode.PMS_CONSENT_ERR.getErrorCode();
            String errorMessage = ErrorCode.PMS_CONSENT_ERR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getUserConsentGivenId);
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
    public ResponseWrapperV2<List<DeviceDetailDto>> sbiDevices(String sbiId) {
        ResponseWrapperV2<List<DeviceDetailDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);

            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }

            Optional<SecureBiometricInterface> secureBiometricInterface = secureBiometricInterfaceRepository.findById(sbiId);

            if (secureBiometricInterface.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "Sbi is not associated with partner Id.");
                throw new PartnerServiceException(ErrorCode.SBI_NOT_EXISTS.getErrorCode(),
                        ErrorCode.SBI_NOT_EXISTS.getErrorMessage());
            }

            SecureBiometricInterface sbi = secureBiometricInterface.get();
            // check if partnerId is associated with user
            boolean partnerIdExists = false;
            for (Partner partner : partnerList) {
                if (partner.getId().equals(sbi.getProviderId())) {
                    validatePartnerId(partner, userId);
                    validateDevicePartnerType(partner, userId);
                    partnerIdExists = true;
                    break;
                }
            }
            if (!partnerIdExists) {
                LOGGER.info("sessionId", "idType", "id", "Partner id is not associated with user.");
                throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
                        ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
            }
            // fetch devices list
            List<DeviceDetailSBI> deviceDetailSBIList = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiId(sbi.getProviderId(), sbiId);
            if (!deviceDetailSBIList.isEmpty()) {
                List<DeviceDetailDto> deviceDetailDtoList = new ArrayList<>();
                for (DeviceDetailSBI deviceDetailSBI : deviceDetailSBIList) {
                    Optional<DeviceDetail> optionalDeviceDetail = deviceDetailRepository.
                            findByIdAndDeviceProviderId(deviceDetailSBI.getId().getDeviceDetailId(), deviceDetailSBI.getProviderId());
                    if (optionalDeviceDetail.isPresent()) {
                        DeviceDetail deviceDetail = optionalDeviceDetail.get();
                        DeviceDetailDto deviceDetailDto = new DeviceDetailDto();
                        deviceDetailDto.setId(deviceDetail.getId());
                        deviceDetailDto.setDeviceTypeCode(deviceDetail.getDeviceTypeCode());
                        deviceDetailDto.setDeviceSubTypeCode(deviceDetail.getDeviceSubTypeCode());
                        deviceDetailDto.setDeviceProviderId(deviceDetail.getDeviceProviderId());
                        deviceDetailDto.setMake(deviceDetail.getMake());
                        deviceDetailDto.setModel(deviceDetail.getModel());
                        deviceDetailDto.setStatus(deviceDetail.getApprovalStatus());
                        deviceDetailDto.setActive(deviceDetail.getIsActive());
                        deviceDetailDto.setCreatedDateTime(deviceDetail.getCrDtimes());
                        
                        deviceDetailDtoList.add(deviceDetailDto);
                    }
                }
                responseWrapper.setResponse(deviceDetailDtoList);
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In sbiDevices method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In sbiDevices method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.DEVICES_LIST_FOR_SBI_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.DEVICES_LIST_FOR_SBI_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getSbiDevicesId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<Boolean> inactiveMappingDeviceToSbi(SbiAndDeviceMappingRequestDto requestDto) {
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        try {
            String partnerId = requestDto.getPartnerId();
            String sbiId = requestDto.getSbiId();
            String deviceDetailId = requestDto.getDeviceDetailId();
            if (Objects.isNull(partnerId) || Objects.isNull(sbiId) || Objects.isNull(deviceDetailId)  ){
                LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_PARAM.getErrorMessage());
            }
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);

            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }

            // check if partnerId is associated with user
            boolean partnerIdExists = false;
            String partnerOrgname = BLANK_STRING;
            for (Partner partner : partnerList) {
                if (partner.getId().equals(partnerId)) {
                    validatePartnerId(partner, userId);
                    partnerIdExists = true;
                    partnerOrgname = partner.getName();
                    break;
                }
            }
            if (!partnerIdExists) {
                LOGGER.info("sessionId", "idType", "id", "Partner id is not associated with user.");
                throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
                        ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
            }

            // validate sbi and device mapping
            multiPartnerHelper.validateSbiDeviceMapping(partnerId, sbiId, deviceDetailId);

            DeviceDetailSBI deviceDetailSBI = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(partnerId, sbiId, deviceDetailId);
            if (Objects.nonNull(deviceDetailSBI)){
                LOGGER.info("sessionId", "idType", "id", "SBI and Device mapping already exists in DB.");
                throw new PartnerServiceException(ErrorCode.SBI_DEVICE_MAPPING_ALREADY_EXIST.getErrorCode(),
                        ErrorCode.SBI_DEVICE_MAPPING_ALREADY_EXIST.getErrorMessage());
            }

            DeviceDetailSBI entity = new DeviceDetailSBI();

            DeviceDetailSBIPK pk = new DeviceDetailSBIPK();
            pk.setSbiId(sbiId);
            pk.setDeviceDetailId(deviceDetailId);

            entity.setId(pk);
            entity.setProviderId(partnerId);
            entity.setPartnerName(partnerOrgname);
            entity.setIsActive(false);
            entity.setIsDeleted(false);
            entity.setCrBy(userId);
            entity.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));

            DeviceDetailSBI savedEntity = deviceDetailSbiRepository.save(entity);
            LOGGER.info("sessionId", "idType", "id", "saved inactive device mapping to sbi successfully in Db.");
            responseWrapper.setResponse(true);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In inactiveMappingDeviceToSbi method of MultiPartnerServiceImpl - " + ex.getMessage());
            deleteDeviceDetail(requestDto.getDeviceDetailId());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In inactiveMappingDeviceToSbi method of MultiPartnerServiceImpl - " + ex.getMessage());
            deleteDeviceDetail(requestDto.getDeviceDetailId());
            String errorCode = ErrorCode.ADD_INACTIVE_DEVICE_MAPPING_WITH_SBI_ERROR.getErrorCode();
            String errorMessage = ErrorCode.ADD_INACTIVE_DEVICE_MAPPING_WITH_SBI_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(postInactiveMappingDeviceToSbiId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    private void deleteDeviceDetail(String deviceDetailId) {
        try {
            deviceDetailRepository.deleteById(deviceDetailId);
            LOGGER.info("sessionId", "idType", "id", "Device detail with id " + deviceDetailId + " deleted successfully.");
        } catch (Exception e) {
            LOGGER.error("sessionId", "idType", "id", "Error while deleting device detail with id " + deviceDetailId + ": " + e.getMessage());
        }
    }
    
    @Override
    public ResponseWrapperV2<DeviceDetailResponseDto> deactivateDevice(String deviceDetailId) {
        ResponseWrapperV2<DeviceDetailResponseDto> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
            if (Objects.isNull(deviceDetailId)) {
                LOGGER.info("sessionId", "idType", "id", "Device id is null.");
                throw new PartnerServiceException(ErrorCode.INVALID_DEVICE_ID.getErrorCode(),
                        ErrorCode.INVALID_DEVICE_ID.getErrorMessage());
            }
            Optional<DeviceDetail> deviceDetail = deviceDetailRepository.findById(deviceDetailId);
            if (!deviceDetail.isPresent()) {
                LOGGER.error("Device not exists with id {}", deviceDetailId);
                throw new PartnerServiceException(ErrorCode.DEVICE_NOT_EXISTS.getErrorCode(),
                        ErrorCode.DEVICE_NOT_EXISTS.getErrorMessage());
            }
            DeviceDetail device = deviceDetail.get();
            // check if the device is associated with user.
            String deviceProviderId = device.getDeviceProviderId();
            boolean deviceProviderExist = false;
            Partner partnerDetails = new Partner();
            for (Partner partner : partnerList) {
                if (partner.getId().equals(deviceProviderId)) {
                    validatePartnerId(partner, userId);
                    deviceProviderExist = true;
                    partnerDetails = partner;
                    break;
                }
            }
            if (!deviceProviderExist) {
                LOGGER.info("sessionId", "idType", "id", "Device is not associated with user.");
                throw new PartnerServiceException(ErrorCode.DEVICE_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
                        ErrorCode.DEVICE_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
            }
            //check if Partner is Active or not
            if (!partnerDetails.getIsActive()) {
                LOGGER.error("Partner is not Active with id {}", deviceProviderId);
                throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
                        ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
            }
            // Deactivate only if the device is approved status and is_active true.
            if (device.getApprovalStatus().equals(APPROVED) && device.getIsActive()) {
                DeviceDetailResponseDto deviceDetailResponseDto = new DeviceDetailResponseDto();

                device.setIsActive(false);
                DeviceDetail updatedDetail = deviceDetailRepository.save(device);
                deviceDetailResponseDto.setDeviceId(updatedDetail.getId());
                deviceDetailResponseDto.setStatus(updatedDetail.getApprovalStatus());
                deviceDetailResponseDto.setActive(updatedDetail.getIsActive());

                responseWrapper.setResponse(deviceDetailResponseDto);
            } else {
                LOGGER.error("Unable to deactivate device with id {}", device.getId());
                throw new PartnerServiceException(ErrorCode.UNABLE_TO_DEACTIVATE_DEVICE.getErrorCode(),
                        ErrorCode.UNABLE_TO_DEACTIVATE_DEVICE.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In deactivateDevice method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In deactivateDevice method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.DEACTIVATE_DEVICE_ERROR.getErrorCode();
            String errorMessage = ErrorCode.DEACTIVATE_DEVICE_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(postDeactivateDevice);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<SbiDetailsResponseDto> deactivateSbi(String sbiId) {
        ResponseWrapperV2<SbiDetailsResponseDto> responseWrapper = new ResponseWrapperV2<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }

            if (Objects.isNull(sbiId)) {
                LOGGER.info("sessionId", "idType", "id", "SBI id is null.");
                throw new PartnerServiceException(ErrorCode.INVALID_SBI_ID.getErrorCode(),
                        ErrorCode.INVALID_SBI_ID.getErrorMessage());
            }
            Optional<SecureBiometricInterface> secureBiometricInterface = secureBiometricInterfaceRepository.findById(sbiId);
            if (!secureBiometricInterface.isPresent()) {
                LOGGER.error("SBI not exists with id {}", sbiId);
                throw new PartnerServiceException(ErrorCode.SBI_NOT_EXISTS.getErrorCode(),
                        ErrorCode.SBI_NOT_EXISTS.getErrorMessage());
            }
            SecureBiometricInterface sbi = secureBiometricInterface.get();
            // check if the SBI is associated with user.
            String sbiProviderId = sbi.getProviderId();
            boolean sbiProviderExist = false;
            Partner partnerDetails = new Partner();
            for (Partner partner : partnerList) {
                if (partner.getId().equals(sbiProviderId)) {
                    validatePartnerId(partner, userId);
                    sbiProviderExist = true;
                    partnerDetails = partner;
                    break;
                }
            }
            if (!sbiProviderExist) {
                LOGGER.info("sessionId", "idType", "id", "SBI is not associated with user.");
                throw new PartnerServiceException(ErrorCode.SBI_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
                        ErrorCode.SBI_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
            }
            //check if Partner is Active or not
            if (!partnerDetails.getIsActive()) {
                LOGGER.error("Partner is not Active with id {}", sbiProviderId);
                throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
                        ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
            }
            // Deactivate only if the SBI is approved and is_active true.
            if (sbi.getApprovalStatus().equals(APPROVED) && sbi.isActive()) {
                // Deactivate approved devices
                List <DeviceDetail> approvedDevices = deviceDetailRepository.findApprovedDevicesBySbiId(sbiId);
                if (!approvedDevices.isEmpty()) {
                    for (DeviceDetail deviceDetail: approvedDevices) {
                        deviceDetail.setIsActive(false);
                        deviceDetailRepository.save(deviceDetail);
                    }
                }
                // Reject pending_approval devices
                List <DeviceDetail> pendingApprovalDevices = deviceDetailRepository.findPendingApprovalDevicesBySbiId(sbiId);
                if (!pendingApprovalDevices.isEmpty()) {
                    for (DeviceDetail deviceDetail: pendingApprovalDevices) {
                        deviceDetail.setApprovalStatus(REJECTED);
                        deviceDetailRepository.save(deviceDetail);
                    }
                }
                sbi.setActive(false);
                SecureBiometricInterface updatedSbi = secureBiometricInterfaceRepository.save(sbi);
                SbiDetailsResponseDto sbiDetailsResponseDto = new SbiDetailsResponseDto();

                sbiDetailsResponseDto.setSbiId(updatedSbi.getId());
                sbiDetailsResponseDto.setSbiVersion(updatedSbi.getSwVersion());
                sbiDetailsResponseDto.setStatus(updatedSbi.getApprovalStatus());
                sbiDetailsResponseDto.setActive(updatedSbi.isActive());

                responseWrapper.setResponse(sbiDetailsResponseDto);
            } else {
                LOGGER.error("Unable to deactivate sbi with id {}", sbi.getId());
                throw new PartnerServiceException(ErrorCode.UNABLE_TO_DEACTIVATE_SBI.getErrorCode(),
                        ErrorCode.UNABLE_TO_DEACTIVATE_SBI.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In deactivateSbi method of MultiPartnerServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In deactivateSbi method of MultiPartnerServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.DEACTIVATE_SBI_ERROR.getErrorCode();
            String errorMessage = ErrorCode.DEACTIVATE_SBI_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(postDeactivateSbi);
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
        responseWrapper.setId(getFtmChipDetails);
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

    private void validateDevicePartnerType(Partner partner, String userId) {
        if (!partner.getPartnerTypeCode().equals(DEVICE_PROVIDER)) {
            LOGGER.info("Invalid Partner type for partner id : " + partner.getId());
            throw new PartnerServiceException(ErrorCode.INVALID_DEVICE_PARTNER_TYPE.getErrorCode(),
                    ErrorCode.INVALID_DEVICE_PARTNER_TYPE.getErrorMessage());
        }
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
