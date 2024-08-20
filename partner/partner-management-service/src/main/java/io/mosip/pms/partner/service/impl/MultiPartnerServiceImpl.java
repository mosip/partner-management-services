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
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.mosip.pms.partner.util.MultiPartnerHelper;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    public static final String YES = "YES";
    public static final String PENDING_APPROVAL = "pending_approval";

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
    UserDetailsRepository userDetailsRepository;

    @Autowired
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @Autowired
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @Autowired
    DeviceDetailRepository deviceDetailRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MultiPartnerHelper multiPartnerHelper;

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

    public static boolean checkIfPartnerIsDevicePartner(Partner partner) {
        String partnerType = partner.getPartnerTypeCode();
        return partnerType.equals(DEVICE_PROVIDER);
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
    public List<ApiKeyResponseDto> getAllApiKeysForAuthPartners() {
        List<ApiKeyResponseDto> apiKeyResponseDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
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
        return apiKeyResponseDtoList;
    }

    private String getUserBy() {
        String crBy = authUserDetails().getMail();
        return crBy;
    }

    @Override
    public UserDetailsDto saveUserConsentGiven() {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
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

                userDetailsDto.setConsentGiven(true);
                userDetailsDto.setUserId(respEntity.getUserId());
                userDetailsDto.setConsentGivenDtimes(respEntity.getConsentGivenDtimes());

            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In saveUserConsentGiven method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception e) {
            LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
            LOGGER.error("sessionId", "idType", "id", "In saveUserConsentGiven method of MultiPartnerServiceImpl - " + e.getMessage());
            throw new PartnerServiceException(ErrorCode.PMS_CONSENT_UNABLE_TO_ADD.getErrorCode(),
                    ErrorCode.PMS_CONSENT_UNABLE_TO_ADD.getErrorMessage());
        }
        return userDetailsDto;
    }

    @Override
    public UserDetailsDto isUserConsentGiven() {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
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
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In isUserConsentGiven method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception e) {
            LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
            LOGGER.error("sessionId", "idType", "id", "In isUserConsentGiven method of MultiPartnerServiceImpl - " + e.getMessage());
            throw new PartnerServiceException(ErrorCode.PMS_CONSENT_ERR.getErrorCode(),
                    ErrorCode.PMS_CONSENT_ERR.getErrorMessage());
        }
        return userDetailsDto;
    }

    @Override
    public List<SbiDetailsDto> getAllSBIDetails() {
        List<SbiDetailsDto> sbiDetailsDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
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
                            sbiDetailsDto.setPartnerType(partner.getPartnerTypeCode());
                            sbiDetailsDto.setSbiVersion(secureBiometricInterface.getSwVersion());
                            sbiDetailsDto.setStatus(secureBiometricInterface.getApprovalStatus());
                            sbiDetailsDto.setExpired(checkIfSbiExpired(secureBiometricInterface));
                            sbiDetailsDto.setCountOfApprovedDevices(countDevices(deviceDetailSBIList, APPROVED));
                            sbiDetailsDto.setCountOfPendingDevices(countDevices(deviceDetailSBIList, PENDING_APPROVAL));
                            sbiDetailsDto.setSbiSoftwareCreatedDtimes(secureBiometricInterface.getSwCreateDateTime());
                            sbiDetailsDto.setSbiSoftwareExpiryDtimes(secureBiometricInterface.getSwExpiryDateTime());
                            sbiDetailsDto.setCrDtimes(secureBiometricInterface.getCrDtimes());

                            sbiDetailsDtoList.add(sbiDetailsDto);
                        }
                    }
                }
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getSbiDetailsList method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getSbiDetailsList method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.SBI_DETAILS_LIST_FETCH_ERROR.getErrorCode(),
                    ErrorCode.SBI_DETAILS_LIST_FETCH_ERROR.getErrorMessage());
        }
        return sbiDetailsDtoList;
    }

    private String countDevices(List<DeviceDetailSBI> deviceDetailSBIList, String status) {
        int count = 0;
        if (deviceDetailSBIList.isEmpty()) {
            return String.valueOf(count);
        }
        for (DeviceDetailSBI deviceDetailSBI : deviceDetailSBIList) {
            Optional<DeviceDetail> deviceDetail = deviceDetailRepository.
                    findByIdAndDeviceProviderId(deviceDetailSBI.getId().getDeviceDetailId(), deviceDetailSBI.getProviderId());
            if (deviceDetail.isPresent() && deviceDetail.get().getApprovalStatus().equals(status)) {
                count++;
            }
        }
        return String.valueOf(count);
    }

    private boolean checkIfSbiExpired(SecureBiometricInterface secureBiometricInterface) {
        return !secureBiometricInterface.getSwExpiryDateTime().toLocalDate().isAfter(LocalDate.now());
    }

    @Override
    public List<PartnerDto> getAllApprovedDeviceProviderIds() {
        List<PartnerDto> approvedDeviceProviderIds = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
            for (Partner partner : partnerList) {
                validatePartnerId(partner, userId);
                if (checkIfPartnerIsDevicePartner(partner)
                        && partner.getApprovalStatus().equalsIgnoreCase(APPROVED)) {
                    PartnerDto partnerDto = new PartnerDto();
                    partnerDto.setPartnerId(partner.getId());
                    partnerDto.setPartnerType(partner.getPartnerTypeCode());

                    approvedDeviceProviderIds.add(partnerDto);
                }
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllApprovedPolicyGroups method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllApprovedDeviceProviderIds method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.APPROVED_DEVICE_PROVIDER_IDS_FETCH_ERROR.getErrorCode(),
                    ErrorCode.APPROVED_DEVICE_PROVIDER_IDS_FETCH_ERROR.getErrorMessage());
        }
        return approvedDeviceProviderIds;
    }

    @Override
    public List<DeviceDetailDto> getAllDevicesForSBI(String sbiId) {
        List<DeviceDetailDto> deviceDetailDtoList = new ArrayList<>();
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
                for (DeviceDetailSBI deviceDetailSBI : deviceDetailSBIList) {
                    Optional<DeviceDetail> deviceDetail = deviceDetailRepository.
                            findByIdAndDeviceProviderId(deviceDetailSBI.getId().getDeviceDetailId(), deviceDetailSBI.getProviderId());
                    if (deviceDetail.isPresent()) {
                        DeviceDetailDto deviceDetailDto = objectMapper
                                .convertValue(deviceDetail, DeviceDetailDto.class);
                        deviceDetailDtoList.add(deviceDetailDto);
                    }
                }
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllDevicesForSBI method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllDevicesForSBI method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.DEVICES_LIST_FOR_SBI_FETCH_ERROR.getErrorCode(),
                    ErrorCode.DEVICES_LIST_FOR_SBI_FETCH_ERROR.getErrorMessage());
        }
        return deviceDetailDtoList;
    }

    @Override
    public Boolean addInactiveDeviceMappingToSbi(SbiAndDeviceMappingRequestDto requestDto) {
        Boolean inactiveDeviceMappingToSbiFlag = false;
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
            inactiveDeviceMappingToSbiFlag = true;
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In addInactiveDeviceMappingToSbi method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In addInactiveDeviceMappingToSbi method of MultiPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.ADD_INACTIVE_DEVICE_MAPPING_WITH_SBI_ERROR.getErrorCode(),
                    ErrorCode.ADD_INACTIVE_DEVICE_MAPPING_WITH_SBI_ERROR.getErrorMessage());
        }
        return inactiveDeviceMappingToSbiFlag;
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
