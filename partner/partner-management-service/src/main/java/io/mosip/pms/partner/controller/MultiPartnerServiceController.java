package io.mosip.pms.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/partners")
@Api(tags = {"Multi Partner Service Controller"})
public class MultiPartnerServiceController {

    @Autowired
    MultiPartnerService multiPartnerService;

    @Autowired
    RequestValidator requestValidator;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetauthpartnerspolicies())")
    @GetMapping(value = "/auth-partners-policies")
    @Operation(summary = "Get all approved auth partner policies", description = "fetch all approved auth partner policies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<ApprovedPolicyDto>> getAuthPartnersPolicies() {
        return multiPartnerService.getAuthPartnersPolicies();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetapprovedpartneridswithpolicygroups())")
    @GetMapping(value = "/approved-partner-ids-with-policy-groups")
    @Operation(summary = "Get all approved partner id's with policy groups", description = "fetch all approved partner id's with policy groups")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<PolicyGroupDto>> getApprovedPartnerIdsWithPolicyGroups() {
        return multiPartnerService.getApprovedPartnerIdsWithPolicyGroups();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetapikeysforauthpartners())")
    @GetMapping(value = "/auth-partner-api-keys")
    @Operation(summary = "Get all api keys for auth partners", description = "fetch all api keys for auth partners")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<ApiKeyResponseDto>> getAuthPartnerApiKeys() {
        return multiPartnerService.getAuthPartnerApiKeys();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetapproveddeviceproviderids())")
    @GetMapping(value = "/approved-device-provider-ids")
    @Operation(summary = "get all approved device providers id.", description = "get all approved device providers id.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<DeviceProviderDto>> approvedDeviceProviderIds() {
        return multiPartnerService.approvedDeviceProviderIds();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetapprovedftmproviderids())")
    @GetMapping(value = "/approved-ftm-provider-ids")
    @Operation(summary = "Get all approved FTM providers ids.", description = "This endpoint will fetch list of all the approved FTM provider ID's mapped to the logged in user.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<List<FtmProviderDto>> approvedFTMProviderIds() {
        return multiPartnerService.approvedFTMProviderIds();
    }
}