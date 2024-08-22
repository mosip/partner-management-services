package io.mosip.pms.partner.controller;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.config.Config;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.mosip.pms.partner.util.RequestValidator;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.Valid;
import java.util.*;

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

    private final String getAllCertificatesDetailsId;
    private final String getAllRequestedPoliciesId;
    private final String getAllApprovedAuthPartnersPoliciesId;
    private final String getAllApprovedPartnerIdsWithPolicyGroupsId;
    private final String getConfigsId;
    private final String getAllApiKeysForAuthPartners;
    private final String postSaveUserConsentGivenId;
    private final String getUserConsentGivenId;
    private final String getAllSbiDetailsId;
    private final String getAllApprovedDeviceProviderId;
    private final String getAllDevicesForSBIId;
    private final String postAddInactiveDeviceMappingToSbiId;
    private final String putDeactivateDevice;

    public static final String VERSION = "1.0";
    public static final String ADD_INACTIVE_DEVICE_MAPPING_TO_SBI_POST = "add.inactive.device.mapping.to.sbi.id.post";

    @Autowired
    MultiPartnerService multiPartnerService;

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    public MultiPartnerServiceController(Config config) {
        Map<String, String> ids = config.getId();
        this.getAllCertificatesDetailsId = ids.get("all.certificates.details.get");
        this.getAllRequestedPoliciesId = ids.get("all.requested.policies.get");
        this.getAllApprovedAuthPartnersPoliciesId = ids.get("all.approved.auth.partners.policies.get");
        this.getAllApprovedPartnerIdsWithPolicyGroupsId = ids.get("all.approved.partner.ids.with.policy.groups.get");
        this.getConfigsId = ids.get("configs.get");
        this.getAllApiKeysForAuthPartners = ids.get("all.api.keys.for.auth.partners.get");
        this.postSaveUserConsentGivenId = ids.get("save.user.consent.given.post");
        this.getUserConsentGivenId = ids.get("user.consent.given.get");
        this.getAllSbiDetailsId = ids.get("all.sbi.details.get");
        this.getAllApprovedDeviceProviderId = ids.get("all.approved.device.provider.ids.get");
        this.getAllDevicesForSBIId = ids.get("all.devices.for.sbi.get");
        this.postAddInactiveDeviceMappingToSbiId = ids.get("add.inactive.device.mapping.to.sbi.id.post");
        this.putDeactivateDevice = ids.get("deactivate.device.put");
    }

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

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallsbidetails())")
    @GetMapping(value = "/getAllSBIDetails")
    @Operation(summary = "get all SBI details list.", description = "get all SBI details list associated with partner.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<SbiDetailsDto>> getAllSBIDetails() {
        ResponseWrapper<List<SbiDetailsDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllSbiDetailsId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllSBIDetails());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallsbidetails())")
    @GetMapping(value = "/getAllApprovedDeviceProviderIds")
    @Operation(summary = "get all approved device providers id.", description = "get all approved device providers id.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<PartnerDto>> getAllApprovedDeviceProviderIds() {
        ResponseWrapper<List<PartnerDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllApprovedDeviceProviderId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllApprovedDeviceProviderIds());
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallsbidetails())")
    @GetMapping(value = "/getAllDevicesForSBI/{sbiId}")
    @Operation(summary = "Get all device list mapped with SBI.", description = "Get all device list mapped with SBI.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<List<DeviceDetailDto>> getAllDevicesForSBI(@PathVariable String sbiId) {
        ResponseWrapper<List<DeviceDetailDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(getAllDevicesForSBIId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.getAllDevicesForSBI(sbiId));
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallsbidetails())")
    @PostMapping(value = "/addInactiveDeviceMappingToSbi")
    @Operation(summary = "Add inactive device mapping to SBI.", description = "Add inactive device mapping to SBI.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<Boolean> addInactiveDeviceMappingToSbi(@RequestBody @Valid RequestWrapper<SbiAndDeviceMappingRequestDto> requestWrapper) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        requestValidator.validateId(ADD_INACTIVE_DEVICE_MAPPING_TO_SBI_POST, requestWrapper.getId());
        requestValidator.validate(requestWrapper);
        responseWrapper.setId(postAddInactiveDeviceMappingToSbiId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.addInactiveDeviceMappingToSbi(requestWrapper.getRequest()));
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPutdeactivatedevice())")
    @PutMapping(value = "/deactivateDevice/{id}")
    @Operation(summary = "Deactivate device details", description = "Deactivate device details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<DeviceDetailResponseDto> deactivateDevice(@PathVariable String id) {
        ResponseWrapper<DeviceDetailResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(putDeactivateDevice);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerService.deactivateDevice(id));
        return responseWrapper;
    }
}