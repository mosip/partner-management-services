package io.mosip.pms.partner.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.service.impl.FTPChipDetailServiceImpl;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MultiPartnerServiceImpl implements MultiPartnerService {

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerServiceImpl.class);
    public static final String BLANK_STRING = "";
    public static final String DEVICE_PROVIDER = "Device_Provider";
    public static final String APPROVED = "approved";
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String VERSION = "1.0";

    @Value("${mosip.pms.api.id.approved.partner.ids.with.policy.groups.get}")
    private String getApprovedPartnerIdsWithPolicyGroupsId;

    @Value("${mosip.pms.api.id.auth.partners.policies.get}")
    private String getAuthPartnersPoliciesId;

    @Value("${mosip.pms.api.id.api.keys.for.auth.partners.get}")
    private String getApiKeysForAuthPartnersId;

    @Value("${mosip.pms.api.id.approved.device.provider.ids.get}")
    private  String getApprovedDeviceProviderIds;

    @Value("${mosip.pms.api.id.approved.ftm.provider.ids.get}")
    private String getApprovedFtmProviderIds;

    @Autowired
    PartnerServiceRepository partnerRepository;

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
                    if (!partnerHelper.skipDeviceOrFtmPartner(partner)
                            && partner.getApprovalStatus().equalsIgnoreCase(APPROVED)) {
                        PolicyGroupDto policyGroupDto = new PolicyGroupDto();
                        partnerHelper.validatePartnerId(partner, userId);
                        partnerHelper.validatePolicyGroupId(partner, userId);
                        PolicyGroup policyGroup = partnerHelper.validatePolicyGroup(partner);
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
                    if (partnerHelper.checkIfPartnerIsApprovedAuthPartner(partner)) {
                        partnerHelper.validatePartnerId(partner, userId);
                        partnerHelper.validatePolicyGroupId(partner, userId);
                        PolicyGroup policyGroup = partnerHelper.validatePolicyGroup(partner);
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
                partnerHelper.validatePartnerId(partner, userId);
                if (partnerHelper.checkIfPartnerIsDevicePartner(partner)
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
                partnerHelper.validatePartnerId(partner, userId);
                if (partnerHelper.checkIfPartnerIsFtmPartner(partner)
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

}
