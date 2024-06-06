package io.mosip.pms.partner.service.impl;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.dto.PolicyGroupDto;
import io.mosip.pms.partner.dto.PolicyDto;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class MultiPartnerServiceImpl implements MultiPartnerService {

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerServiceImpl.class);
    public static final String BLANK_STRING = "";
    public static final String DEVICE_PROVIDER = "Device_Provider";
    public static final String FTM_PROVIDER = "FTM_Provider";
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
    public List<PolicyDto> getAllRequestedPolicies(Boolean filterByOnlyApprovedPolicies) {
        List<PolicyDto> policyDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                for (Partner partner : partnerList) {
                    try {
                        if (!partner.getPartnerTypeCode().equals(DEVICE_PROVIDER) && !partner.getPartnerTypeCode().equals(FTM_PROVIDER)) {
                            if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
                                LOGGER.info("Partner Id is null or empty for user id : " + userId);
                                throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
                                        ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
                            }
                            if (Objects.isNull(partner.getPolicyGroupId()) || partner.getPolicyGroupId().equals(BLANK_STRING)) {
                                LOGGER.info("Policy group Id is null or empty for user id : " + userId);
                                throw new PartnerServiceException(ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorCode(),
                                        ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorMessage());
                            }
                            PolicyGroup policyGroup = policyGroupRepository.findPolicyGroupById(partner.getPolicyGroupId());
                            if (Objects.isNull(policyGroup) || Objects.isNull(policyGroup.getName()) || policyGroup.getName().equals(BLANK_STRING)) {
                                LOGGER.info("Policy Group is null or empty for partner id : " + partner.getId());
                                throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode(),
                                        ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
                            }
                            List<PartnerPolicyRequest> partnerPolicyRequestList = partner.getPartnerPolicyRequests();
                            if (!partnerPolicyRequestList.isEmpty()) {
                                for (PartnerPolicyRequest partnerPolicyRequest : partnerPolicyRequestList) {
                                    if (!filterByOnlyApprovedPolicies || partnerPolicyRequest.getStatusCode().equals("approved")){
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
                    if (!partnerType.equalsIgnoreCase(DEVICE_PROVIDER) && !partnerType.equalsIgnoreCase(FTM_PROVIDER)
                            && partner.getApprovalStatus().equalsIgnoreCase(APPROVED)) {
                        PolicyGroupDto policyGroupDto = new PolicyGroupDto();
                        try {
                            if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
                                LOGGER.info("Partner Id is null or empty for user id : " + userId);
                                throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
                                        ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
                            }
                            PolicyGroup policyGroup = policyGroupRepository.findPolicyGroupById(partner.getPolicyGroupId());
                            if (Objects.isNull(policyGroup)) {
                                LOGGER.info("Policy Group is null for partner id : " + partner.getId());
                                throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode(),
                                        ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
                            }
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
