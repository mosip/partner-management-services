package io.mosip.pms.partner.service.impl;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.service.MultiPartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class MultiPartnerServiceImpl implements MultiPartnerService {

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerServiceImpl.class);
    public static final String BLANK_STRING = "";
    public static final String DEVICE_PROVIDER = "Device_Provider";
    public static final String FTM_PROVIDER = "FTM_Provider";
    public static final String AUTH_PARTNER = "Auth_Partner";
    public static final String APPROVED = "approved";
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
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

    @Override
    public List<CertificateDto> getAllCertificateDetails() {
        List<CertificateDto> certificateDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
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
                        String certificateData = partnerCertDownloadResponeDto.getCertificateData();
                        certificateData = certificateData.replaceAll(BEGIN_CERTIFICATE, "")
                                .replaceAll(END_CERTIFICATE, "")
                                .replaceAll("\n", "");

                        byte[] decodedCertificate = Base64.getDecoder().decode(certificateData);

                        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decodedCertificate));

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
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllCertificateDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllCertificateDetails method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorCode(),
                    ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorMessage());
        }
        return certificateDtoList;
    }

    @Override
    public List<PolicyDto> getAllRequestedPolicies() {
        List<PolicyDto> policyDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                for (Partner partner : partnerList) {
                    try {
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
                    } catch (PartnerServiceException ex) {
                        LOGGER.info("Could not fetch policies :" + ex.getMessage());
                    }
                }
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorCode(),
                    ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorMessage());
        }
        return policyDtoList;
    }

    @Override
    public List<PolicyGroupDto> getAllApprovedPartnerIdsWithPolicyGroups() {
        List<PolicyGroupDto> policyGroupDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                for (Partner partner : partnerList) {
                    String partnerType = partner.getPartnerTypeCode();
                    // Ignore, If the partner is a DEVICE or FTM partnertype
                    if (!skipDeviceOrFtmPartner(partner)
                            && partner.getApprovalStatus().equalsIgnoreCase(APPROVED)) {
                        PolicyGroupDto policyGroupDto = new PolicyGroupDto();
                        try {
                            validatePartnerId(partner, userId);
                            validatePolicyGroupId(partner, userId);
                            PolicyGroup policyGroup = validatePolicyGroup(partner);
                            policyGroupDto.setPartnerId(partner.getId());
                            policyGroupDto.setPartnerType(partner.getPartnerTypeCode());
                            policyGroupDto.setPolicyGroupId(partner.getPolicyGroupId());
                            policyGroupDto.setPolicyGroupName(policyGroup.getName());
                            policyGroupDto.setPolicyGroupDescription(policyGroup.getDesc());
                        } catch (PartnerServiceException ex) {
                            LOGGER.info("Could not fetch all approved policy groups :" + ex.getMessage());
                        }
                        policyGroupDtoList.add(policyGroupDto);
                    }
                }
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllApprovedPolicyGroups method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllApprovedPolicyGroups method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.POLICY_GROUP_FETCH_ERROR.getErrorCode(),
                    ErrorCode.POLICY_GROUP_FETCH_ERROR.getErrorMessage());
        }
        return policyGroupDtoList;
    }

    @Override
    public List<ApprovedPolicyDto> getAllApprovedAuthPartnerPolicies() {
        List<ApprovedPolicyDto> approvedPolicyList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                for (Partner partner : partnerList) {
                    try {
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
                    } catch (PartnerServiceException ex) {
                        LOGGER.info("Could not fetch policies :" + ex.getMessage());
                    }
                }
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllPolicies method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorCode(),
                    ErrorCode.PARTNER_POLICY_FETCH_ERROR.getErrorMessage());
        }
        return approvedPolicyList;
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
    public List<ApiKeyRequestDto> getAllApiKeysForAuthPartners() {
        List<ApiKeyRequestDto> apiKeyRequestDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                for (Partner partner : partnerList) {
                    try {
                        if (checkIfPartnerIsApprovedAuthPartner(partner)) {
                            validatePartnerId(partner, userId);
                            List<PartnerPolicy> apiKeyRequestsList = partnerPolicyRepository.findAPIKeysByPartnerId(partner.getId());
                            if (!apiKeyRequestsList.isEmpty()) {
                                for (PartnerPolicy partnerPolicy: apiKeyRequestsList) {
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
                                    ApiKeyRequestDto apiKeyRequestDto = new ApiKeyRequestDto();
                                    apiKeyRequestDto.setApiKeyReqID(partnerPolicy.getPolicyApiKey());
                                    apiKeyRequestDto.setApiKeyLabel(partnerPolicy.getLabel());
                                    apiKeyRequestDto.setApiKeyStatus(partnerPolicy.getIsActive());
                                    apiKeyRequestDto.setPartnerId(partner.getId());
                                    apiKeyRequestDto.setPolicyGroupId(policyGroup.getId());
                                    apiKeyRequestDto.setPolicyGroupName(policyGroup.getName());
                                    apiKeyRequestDto.setPolicyGroupDescription(policyGroup.getDesc());
                                    apiKeyRequestDto.setPolicyId(authPolicy.get().getId());
                                    apiKeyRequestDto.setPolicyName(authPolicy.get().getName());
                                    apiKeyRequestDto.setPolicyNameDescription(authPolicy.get().getDescr());
                                    apiKeyRequestDto.setCrDtimes(partnerPolicy.getCrDtimes());
                                    apiKeyRequestDto.setUpdDtimes(partnerPolicy.getUpdDtimes());
                                    apiKeyRequestDtoList.add(apiKeyRequestDto);
                                }
                            }
                        }
                    } catch (PartnerServiceException ex) {
                        LOGGER.info("Could not fetch api requests :" + ex.getMessage());
                    }
                }
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllApiKeysForAuthPartners method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllApiKeysForAuthPartners method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.API_KEY_REQUESTS_FETCH_ERROR.getErrorCode(),
                    ErrorCode.API_KEY_REQUESTS_FETCH_ERROR.getErrorMessage());
        }
        return apiKeyRequestDtoList;
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