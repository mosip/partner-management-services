package io.mosip.pms.partner.service;

import io.mosip.pms.partner.dto.ApprovedPolicyDto;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.dto.PolicyDto;
import io.mosip.pms.partner.dto.PolicyGroupDto;
import io.mosip.pms.partner.dto.ApiKeyResponseDto;
import io.mosip.pms.partner.dto.UserDetailsDto;

import java.util.List;

public interface MultiPartnerService {

    public List<CertificateDto> getPartnerCertificates();

    public List<PolicyDto> getPolicyRequests();

    public List<ApprovedPolicyDto> getAuthPartnerPolicies();

    public List<PolicyGroupDto> getApprovedPartnerIdsWithPolicyGroups();

    public List<ApiKeyResponseDto> getApiKeysForAuthPartners();
}
