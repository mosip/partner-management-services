package io.mosip.pms.partner.service;

import io.mosip.pms.partner.dto.ApprovedPolicyDto;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.dto.PolicyDto;
import io.mosip.pms.partner.dto.PolicyGroupDto;
import io.mosip.pms.partner.dto.ApiKeyResponseDto;
import io.mosip.pms.partner.dto.UserDetailsDto;

import java.util.List;

public interface MultiPartnerService {

    public List<CertificateDto> getAllCertificateDetails();

    public List<PolicyDto> getAllRequestedPolicies();

    public List<ApprovedPolicyDto> getAllApprovedAuthPartnerPolicies();

    public List<PolicyGroupDto> getAllApprovedPartnerIdsWithPolicyGroups();

    public List<ApiKeyResponseDto> getAllApiKeysForAuthPartners();

    public UserDetailsDto saveUserConsentGiven();

    public UserDetailsDto isUserConsentGiven();
}
