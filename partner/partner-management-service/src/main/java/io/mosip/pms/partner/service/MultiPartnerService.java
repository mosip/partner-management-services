package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.response.dto.SbiDetailsResponseDto;

import java.util.List;

public interface MultiPartnerService {

    public ResponseWrapperV2<List<CertificateDto>> getAllCertificateDetails();

    public ResponseWrapperV2<List<PolicyDto>> getAllRequestedPolicies();

    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAllApprovedAuthPartnerPolicies();

    public ResponseWrapperV2<List<PolicyGroupDto>> getAllApprovedPartnerIdsWithPolicyGroups();

    public ResponseWrapperV2<List<ApiKeyResponseDto>> getAllApiKeysForAuthPartners();

    public ResponseWrapperV2<UserDetailsDto> saveUserConsentGiven();

    public ResponseWrapperV2<UserDetailsDto> isUserConsentGiven();

    public ResponseWrapperV2<List<SbiDetailsDto>> getAllSBIDetails();

    public ResponseWrapperV2<List<DeviceProviderDto>> getAllApprovedDeviceProviderIds();

    public ResponseWrapperV2<List<DeviceDetailDto>> getAllDevicesForSBI(String sbiId);

    public ResponseWrapperV2<Boolean> addInactiveDeviceMappingToSbi(SbiAndDeviceMappingRequestDto requestDto);

    public ResponseWrapperV2<DeviceDetailResponseDto> deactivateDevice(String deviceDetailId);

    public ResponseWrapperV2<SbiDetailsResponseDto> deactivateSbi(String id);

    public ResponseWrapperV2<List<FtmChipDetailsDto>> ftmChipDetails();

    public ResponseWrapperV2<List<FtmProviderDto>> approvedFTMProviderIds();
}
