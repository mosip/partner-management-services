package io.mosip.pms.partner.service;

import io.mosip.pms.partner.dto.ApprovedPolicyDto;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.dto.PolicyDto;
import io.mosip.pms.partner.dto.PolicyGroupDto;
import io.mosip.pms.partner.dto.ApiKeyResponseDto;
import io.mosip.pms.partner.dto.UserDetailsDto;
import io.mosip.pms.partner.dto.SbiDetailsDto;
import io.mosip.pms.partner.dto.DeviceProviderDto;
import io.mosip.pms.partner.dto.DeviceDetailDto;
import io.mosip.pms.partner.dto.FtmProviderDto;
import io.mosip.pms.partner.dto.FtmProviderDetailsDto;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.response.dto.FtmDetailsResponseDto;
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

    public List<FtmProviderDto> getAllApprovedFtmProviderIds();

    public List<FtmProviderDetailsDto> getAllFtmProviderDetails();

    public FtmDetailsResponseDto deactivateFtm(String id);
}
