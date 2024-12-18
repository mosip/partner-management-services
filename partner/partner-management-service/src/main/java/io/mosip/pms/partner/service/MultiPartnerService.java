package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
import io.mosip.pms.partner.dto.*;

import java.util.List;

public interface MultiPartnerService {

    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAuthPartnersPolicies();

    public ResponseWrapperV2<List<PolicyGroupDto>> getApprovedPartnerIdsWithPolicyGroups();

    public ResponseWrapperV2<List<ApiKeyResponseDto>> getAuthPartnerApiKeys();

    public ResponseWrapperV2<List<DeviceProviderDto>> approvedDeviceProviderIds();

    public ResponseWrapperV2<List<FtmProviderDto>> approvedFTMProviderIds();
}
