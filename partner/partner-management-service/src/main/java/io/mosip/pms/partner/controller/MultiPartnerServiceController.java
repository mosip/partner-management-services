package io.mosip.pms.partner.controller;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.request.dto.DeactivateDeviceRequestDto;
import io.mosip.pms.partner.request.dto.DeactivateSbiRequestDto;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.response.dto.SbiDetailsResponseDto;
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

    @Value("${mosip.pms.expiry.date.max.year}")
    private String maxAllowedYear;

    @Value("${mosip.pms.api.id.configs.get}")
    private String getConfigsId;

    @Value("${mosip.pms.api.id.add.inactive.device.mapping.to.sbi.id.post}")
    private  String postAddInactiveDeviceMappingToSbiId;

    @Value("${mosip.pms.api.id.deactivate.device.put}")
    private  String putDeactivateDevice;

    @Value("${mosip.pms.api.id.deactivate.sbi.put}")
    private  String putDeactivateSbi;

    public static final String VERSION = "1.0";

    @Autowired
    MultiPartnerService multiPartnerService;

    @Autowired
    RequestValidator requestValidator;

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
        return  multiPartnerService.getAllCertificateDetails();
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
        return multiPartnerService.getAllRequestedPolicies();
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
        return multiPartnerService.getAllApprovedAuthPartnerPolicies();
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
        return multiPartnerService.getAllApprovedPartnerIdsWithPolicyGroups();
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
        configMap.put("maxAllowedyear", maxAllowedYear);
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
        return multiPartnerService.getAllApiKeysForAuthPartners();
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
        return multiPartnerService.saveUserConsentGiven();
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
        return multiPartnerService.isUserConsentGiven();
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
        return multiPartnerService.getAllSBIDetails();
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetallsbidetails())")
    @GetMapping(value = "/getAllApprovedDeviceProviderIds")
    @Operation(summary = "get all approved device providers id.", description = "get all approved device providers id.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<DeviceProviderDto>> getAllApprovedDeviceProviderIds() {
        return multiPartnerService.getAllApprovedDeviceProviderIds();
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
        return multiPartnerService.getAllDevicesForSBI(sbiId);
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
        Optional<ResponseWrapper<Boolean>> validationResponse = requestValidator.validate(postAddInactiveDeviceMappingToSbiId, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        return multiPartnerService.addInactiveDeviceMappingToSbi(requestWrapper.getRequest());
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPutdeactivatedevice())")
    @PutMapping(value = "/deactivateDevice")
    @Operation(summary = "Deactivate device details", description = "Deactivate device details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<DeviceDetailResponseDto> deactivateDevice(@RequestBody @Valid RequestWrapper<DeactivateDeviceRequestDto> requestWrapper) {
        Optional<ResponseWrapper<DeviceDetailResponseDto>> validationResponse = requestValidator.validate(putDeactivateDevice, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        return multiPartnerService.deactivateDevice(requestWrapper.getRequest().getDeviceId());
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPutdeactivatesbi())")
    @PutMapping(value = "/deactivateSbi")
    @Operation(summary = "Deactivate SBI along with associated devices", description = "Deactivate SBI along with associated devices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<SbiDetailsResponseDto> deactivateSbi(@RequestBody @Valid RequestWrapper<DeactivateSbiRequestDto> requestWrapper) {
        Optional<ResponseWrapper<SbiDetailsResponseDto>> validationResponse = requestValidator.validate(putDeactivateSbi, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        return multiPartnerService.deactivateSbi(requestWrapper.getRequest().getSbiId());
    }
}