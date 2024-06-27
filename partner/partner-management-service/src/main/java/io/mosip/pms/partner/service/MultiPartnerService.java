package io.mosip.pms.partner.service;

import io.mosip.pms.partner.dto.*;

import java.util.List;

public interface MultiPartnerService {

    public List<CertificateDto> getAllCertificateDetails();

    public List<PolicyDto> getAllRequestedPolicies();

    public List<ApprovedPolicyDto> getAllApprovedAuthPartnerPolicies();

    public List<PolicyGroupDto> getAllApprovedPartnerIdsWithPolicyGroups();

    public List<ApiKeyResponseDto> getAllApiKeysForAuthPartners();
}
