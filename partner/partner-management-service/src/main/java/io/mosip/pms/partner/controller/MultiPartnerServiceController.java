package io.mosip.pms.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapper;
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

    @Value("${mosip.pms.oidc.clients.grantTypes:authorization_code}")
    private String grantTypes;

    @Value("${mosip.pms.oidc.clients.clientAuthMethods:private_key_jwt}")
    private String clientAuthMethods;

    @Value("${mosip.pms.session.inactivity.timer}")
    private String inActivityTimer;

    @Value("${mosip.pms.session.inactivity.prompt.timer}")
    private String inActivityPromptTimer;

    @Value("${mosip.pms.axios.timeout}")
    private String axiosTimeout;

    @Value("${mosip.pms.api.id.all.certificates.details.get}")
    private String getAllCertificatesDetailsId;

    @Value("${mosip.pms.api.id.all.requested.policies.get}")
    private String getAllRequestedPoliciesId;

    @Value("${mosip.pms.api.id.all.approved.auth.partners.policies.get}")
    private String getAllApprovedAuthPartnersPoliciesId;

    @Value("${mosip.pms.api.id.all.approved.partner.ids.with.policy.groups.get}")
    private String getAllApprovedPartnerIdsWithPolicyGroupsId;

    @Value("${mosip.pms.api.id.configs.get}")
    private String getConfigsId;

    @Value("${mosip.pms.api.id.all.api.keys.for.auth.partners.get}")
    private String getAllApiKeysForAuthPartners;

    @Value("${mosip.pms.api.id.save.user.consent.given.post}")
    private String postSaveUserConsentGivenId;

    @Value("${mosip.pms.api.id.user.consent.given.get}")
    private String getUserConsentGivenId;

    public static final String VERSION = "1.0";

    @Autowired
    MultiPartnerService multiPartnerService;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallcertificatedetails())")
    @GetMapping(value = "/getAllCertificateDetails")
    @Operation(summary = "Get partner certificates", description = "fetch partner certificates")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<CertificateDto>> getAllCertificateDetails() {
        ResponseWrapper<List<CertificateDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllCertificatesDetailsId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllCertificateDetails());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallrequestedpolicies())")
    @GetMapping(value = "/getAllRequestedPolicies")
    @Operation(summary = "Get all policies", description = "fetch all policies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<PolicyDto>> getAllRequestedPolicies() {
        ResponseWrapper<List<PolicyDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllRequestedPoliciesId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllRequestedPolicies());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallapprovedauthpartnerpolicies())")
    @GetMapping(value = "/getAllApprovedAuthPartnerPolicies")
    @Operation(summary = "Get all approved auth partner policies", description = "fetch all approved auth partner policies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<ApprovedPolicyDto>> getAllApprovedAuthPartnerPolicies() {
        ResponseWrapper<List<ApprovedPolicyDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllApprovedAuthPartnersPoliciesId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllApprovedAuthPartnerPolicies());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallapprovedpartneridswithpolicygroups())")
    @GetMapping(value = "/getAllApprovedPartnerIdsWithPolicyGroups")
    @Operation(summary = "Get all approved partner id's with policy groups", description = "fetch all approved partner id's with policy groups")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<PolicyGroupDto>> getAllApprovedPartnerIdsWithPolicyGroups() {
        ResponseWrapper<List<PolicyGroupDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllApprovedPartnerIdsWithPolicyGroupsId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllApprovedPartnerIdsWithPolicyGroups());
        return responseWrapper;
    }

    @GetMapping(value = "/configs")
    @Operation(summary = "Get config", description = "Get configuration values")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<Map<String, String>> getConfigValues() {
        ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getConfigsId);
        responseWrapper.setVersion(VERSION);
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("grantTypes", grantTypes);
        configMap.put("clientAuthMethods", clientAuthMethods);
        configMap.put("inActivityTimer", inActivityTimer);
        configMap.put("inActivityPromptTimer", inActivityPromptTimer);
        configMap.put("axiosTimeout", axiosTimeout);
        responseWrapper.setResponse(configMap);
        System.out.println(responseWrapper);
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallapikeysforauthpartners())")
    @GetMapping(value = "/getAllApiKeysForAuthPartners")
    @Operation(summary = "Get all api keys for auth partners", description = "fetch all api keys for auth partners")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<ApiKeyResponseDto>> getAllApiKeysForAuthPartners() {
        ResponseWrapper<List<ApiKeyResponseDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllApiKeysForAuthPartners);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllApiKeysForAuthPartners());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getUserconsent())")
    @PostMapping(value = "/saveUserConsentGiven")
    @Operation(summary = "save user consent", description = "Store the user consent in the database.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<UserDetailsDto> saveUserConsentGiven() {
        ResponseWrapper<UserDetailsDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(postSaveUserConsentGivenId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.saveUserConsentGiven());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getUserconsent())")
    @GetMapping(value = "/isUserConsentGiven")
    @Operation(summary = "Retrieve the user consent status.", description = "Retrieve the user consent status.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<UserDetailsDto> isUserConsentGiven() {
        ResponseWrapper<UserDetailsDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getUserConsentGivenId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.isUserConsentGiven());
        return responseWrapper;
    }
}