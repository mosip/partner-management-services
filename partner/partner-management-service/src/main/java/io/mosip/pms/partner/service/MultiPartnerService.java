package io.mosip.pms.partner.service;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.ApprovedPolicyDto;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.dto.PolicyDto;
import io.mosip.pms.partner.dto.PolicyGroupDto;
import io.mosip.pms.partner.dto.ApiKeyResponseDto;
import io.mosip.pms.partner.dto.UserDetailsDto;

import java.util.List;

public interface MultiPartnerService {

    public ResponseWrapperV2<List<CertificateDto>> getPartnerCertificates();

    public ResponseWrapperV2<List<PolicyDto>> getPolicyRequests();

    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAuthPartnersPolicies();

    public ResponseWrapperV2<List<PolicyGroupDto>> getApprovedPartnerIdsWithPolicyGroups();

    public ResponseWrapperV2<List<ApiKeyResponseDto>> getApiKeysForAuthPartners();
}
