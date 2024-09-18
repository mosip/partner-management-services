package io.mosip.pms.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.mosip.pms.partner.util.RequestValidator;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping(value = "/partners")
@Api(tags = {"Multi Partner Service Controller"})
public class MultiPartnerServiceController {

    @Autowired
    MultiPartnerService multiPartnerService;

    @Autowired
    RequestValidator requestValidator;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPartnercertificates())")
    @GetMapping(value = "/partner-certificates")
    @Operation(summary = "Get partner certificates", description = "fetch partner certificates")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<CertificateDto>> getPartnerCertificates() {
        return  multiPartnerService.getPartnerCertificates();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPolicyrequests())")
    @GetMapping(value = "/policy-requests")
    @Operation(summary = "Get all policies", description = "fetch all policies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<PolicyDto>> getPolicyRequests() {
        return multiPartnerService.getPolicyRequests();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getAuthpartnerspolicies())")
    @GetMapping(value = "/auth-partners-policies")
    @Operation(summary = "Get all approved auth partner policies", description = "fetch all approved auth partner policies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAuthPartnersPolicies() {
        return multiPartnerService.getAuthPartnersPolicies();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getApprovedpartneridswithpolicygroups())")
    @GetMapping(value = "/approved-partner-ids-with-policy-groups")
    @Operation(summary = "Get all approved partner id's with policy groups", description = "fetch all approved partner id's with policy groups")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<PolicyGroupDto>> getApprovedPartnerIdsWithPolicyGroups() {
        return multiPartnerService.getApprovedPartnerIdsWithPolicyGroups();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getApikeysforauthpartners())")
    @GetMapping(value = "/api-keys-for-auth-partners")
    @Operation(summary = "Get all api keys for auth partners", description = "fetch all api keys for auth partners")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<ApiKeyResponseDto>> getApiKeysForAuthPartners() {
        return multiPartnerService.getApiKeysForAuthPartners();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getDeviceproviderservice())")
    @GetMapping(value = "/sbi-details")
    @Operation(summary = "get all SBI details list.", description = "get all SBI details list associated with partner.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<SbiDetailsDto>> sbiDetails() {
        return multiPartnerService.sbiDetails();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getDeviceproviderservice())")
    @GetMapping(value = "/approved-device-provider-ids")
    @Operation(summary = "get all approved device providers id.", description = "get all approved device providers id.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<DeviceProviderDto>> approvedDeviceProviderIds() {
        return multiPartnerService.approvedDeviceProviderIds();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getFtmchipservice())")
    @GetMapping(value = "/ftm-chip-details")
    @Operation(summary = "Get list of all the FTM Chip details", description = "This endpoint will fetch the list of all the FTM Chip details created by all the partner Id's associated with the logged in user")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<FtmChipDetailsDto>> ftmChipDetails() {
        return multiPartnerService.ftmChipDetails();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getFtmchipservice())")
    @GetMapping(value = "/approved-ftm-provider-ids")
    @Operation(summary = "Get all approved FTM providers ids.", description = "This endpoint will fetch list of all the approved FTM provider ID's mapped to the logged in user.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<FtmProviderDto>> approvedFTMProviderIds() {
        return multiPartnerService.approvedFTMProviderIds();
    }
}