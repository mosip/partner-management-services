package io.mosip.pms.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/partners")
@Api(tags = { "Multi Partner Service Controller" })
public class MultiPartnerServiceController {

    @Value("${mosip.pms.api.id.all.certificates.details.get}")
    private String getPartnerCertificatesId;

    @Value("${mosip.pms.api.id.all.requested.policies.get}")
    private String getAllRequestedPoliciesId;

    @Value("${mosip.pms.api.id.all.approved.auth.partners.policies.get}")
    private String getAuthPartnersPoliciesId;

    @Value("${mosip.pms.api.id.all.approved.partner.ids.with.policy.groups.get}")
    private String getApprovedPartnerIdsWithPolicyGroupsId;

    @Value("${mosip.pms.api.id.all.api.keys.for.auth.partners.get}")
    private String getApiKeysForAuthPartners;

    public static final String VERSION = "1.0";

    @Autowired
    MultiPartnerService multiPartnerService;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallcertificatedetails())")
    @GetMapping(value = "/partner-certificates")
    @Operation(summary = "Get partner certificates", description = "fetch partner certificates")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<CertificateDto>> getPartnerCertificates() {
        ResponseWrapperV2<List<CertificateDto>> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setId(getPartnerCertificatesId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getPartnerCertificates());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallrequestedpolicies())")
    @GetMapping(value = "/policy-requests")
    @Operation(summary = "Get all policies", description = "fetch all policies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<PolicyDto>> getPolicyRequests() {
        ResponseWrapperV2<List<PolicyDto>> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setId(getAllRequestedPoliciesId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getPolicyRequests());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallapprovedauthpartnerpolicies())")
    @GetMapping(value = "/auth-partners-policies")
    @Operation(summary = "Get all approved auth partner policies", description = "fetch all approved auth partner policies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAuthPartnerPolicies() {
        ResponseWrapperV2<List<ApprovedPolicyDto>> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setId(getAuthPartnersPoliciesId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAuthPartnerPolicies());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallapprovedpartneridswithpolicygroups())")
    @GetMapping(value = "/approved-partner-ids-with-policy-groups")
    @Operation(summary = "Get all approved partner id's with policy groups", description = "fetch all approved partner id's with policy groups")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<PolicyGroupDto>> getApprovedPartnerIdsWithPolicyGroups() {
        ResponseWrapperV2<List<PolicyGroupDto>> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setId(getApprovedPartnerIdsWithPolicyGroupsId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getApprovedPartnerIdsWithPolicyGroups());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallapikeysforauthpartners())")
    @GetMapping(value = "/api-keys-for-auth-partners")
    @Operation(summary = "Get all api keys for auth partners", description = "fetch all api keys for auth partners")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<ApiKeyResponseDto>> getApiKeysForAuthPartners() {
        ResponseWrapperV2<List<ApiKeyResponseDto>> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setId(getApiKeysForAuthPartners);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getApiKeysForAuthPartners());
        return responseWrapper;
    }
}