package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.*;

import java.util.List;

public interface MultiPartnerService {

    public ResponseWrapperV2<List<CertificateDto>> getAllCertificateDetails();

    public ResponseWrapperV2<List<PolicyDto>> getAllRequestedPolicies();

    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAllApprovedAuthPartnerPolicies();

    public ResponseWrapperV2<List<PolicyGroupDto>> getAllApprovedPartnerIdsWithPolicyGroups();

    public ResponseWrapperV2<List<ApiKeyResponseDto>> getAllApiKeysForAuthPartners();

    public ResponseWrapperV2<UserDetailsDto> saveUserConsentGiven();

    public ResponseWrapperV2<UserDetailsDto> isUserConsentGiven();

    public ResponseWrapperV2<List<SbiDetailsDto>> sbiDetails();

    public ResponseWrapperV2<List<DeviceProviderDto>> approvedDeviceProviderIds();

    public ResponseWrapperV2<List<FtmChipDetailsDto>> ftmChipDetails();

    public ResponseWrapperV2<List<FtmProviderDto>> approvedFTMProviderIds();
}
