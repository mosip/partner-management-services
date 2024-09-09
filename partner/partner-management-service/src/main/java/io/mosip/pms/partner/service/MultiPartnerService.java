package io.mosip.pms.partner.service;

import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.response.dto.SbiDetailsResponseDto;

import java.util.List;

public interface MultiPartnerService {

    public List<CertificateDto> getAllCertificateDetails();

    public List<PolicyDto> getAllRequestedPolicies();

    public List<ApprovedPolicyDto> getAllApprovedAuthPartnerPolicies();

    public List<PolicyGroupDto> getAllApprovedPartnerIdsWithPolicyGroups();

    public List<ApiKeyResponseDto> getAllApiKeysForAuthPartners();

    public UserDetailsDto saveUserConsentGiven();

    public UserDetailsDto isUserConsentGiven();

    public List<SbiDetailsDto> getAllSBIDetails();

    public List<DeviceProviderDto> getAllApprovedDeviceProviderIds();

    public List<DeviceDetailDto> getAllDevicesForSBI(String sbiId);

    public Boolean addInactiveDeviceMappingToSbi(SbiAndDeviceMappingRequestDto requestDto);

    public DeviceDetailResponseDto deactivateDevice(String deviceDetailId);

    public SbiDetailsResponseDto deactivateSbi(String id);

    public List<FtmChipDetailsDto> ftmChipDetails();

    public List<FtmProviderDto> approvedFTMProviderIds();
}
