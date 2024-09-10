package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.response.dto.SbiDetailsResponseDto;

import java.util.List;

public interface MultiPartnerService {

    public ResponseWrapper<List<CertificateDto>> getAllCertificateDetails();

    public ResponseWrapper<List<PolicyDto>> getAllRequestedPolicies();

    public ResponseWrapper<List<ApprovedPolicyDto>> getAllApprovedAuthPartnerPolicies();

    public ResponseWrapper<List<PolicyGroupDto>> getAllApprovedPartnerIdsWithPolicyGroups();

    public ResponseWrapper<List<ApiKeyResponseDto>> getAllApiKeysForAuthPartners();

    public ResponseWrapper<UserDetailsDto> saveUserConsentGiven();

    public ResponseWrapper<UserDetailsDto> isUserConsentGiven();

    public ResponseWrapper<List<SbiDetailsDto>> getAllSBIDetails();

    public ResponseWrapper<List<DeviceProviderDto>> getAllApprovedDeviceProviderIds();

    public ResponseWrapper<List<DeviceDetailDto>> getAllDevicesForSBI(String sbiId);

    public ResponseWrapper<Boolean> addInactiveDeviceMappingToSbi(SbiAndDeviceMappingRequestDto requestDto);

    public ResponseWrapper<DeviceDetailResponseDto> deactivateDevice(String deviceDetailId);

    public ResponseWrapper<SbiDetailsResponseDto> deactivateSbi(String id);

    public ResponseWrapper<List<FtmChipDetailsDto>> ftmChipDetails();

    public ResponseWrapper<List<FtmProviderDto>> approvedFTMProviderIds();
}
