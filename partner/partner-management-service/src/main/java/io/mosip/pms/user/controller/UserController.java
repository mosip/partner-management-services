package io.mosip.pms.user.controller;

import javax.validation.Valid;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.UserDetailsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.user.service.UserManagementService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/users")
public class UserController {

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
	private String maxAllowedExpiryYear;

	@Value("${mosip.pms.created.date.max.year}")
	private String maxAllowedCreatedYear;

	@Value("${mosip.pms.pagination.items.per.page}")
	private String itemsPerPage;

	@Value("${mosip.pms.api.id.configs.get}")
	private String getConfigsId;

	public static final String VERSION = "1.0";

	@Autowired
	UserManagementService userManagementService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MISP_PARTNER','PARTNER_ADMIN','AUTH_PARTNER','CREDENTIAL_PARTNER','ONLINE_VERIFICATION_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','ABIS_PARTNER','MANUAL_ADJUDICATION','SDK_PARTNER')")
	public ResponseWrapper<MosipUserDto> registerUser(
			@RequestBody @Valid RequestWrapper<UserRegistrationRequestDto> request) {
		ResponseWrapper<MosipUserDto> response = new ResponseWrapper<MosipUserDto>();
		response.setResponse(userManagementService.registerUser(request.getRequest()));
		return response;

	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUserconsent())")
	@PostMapping(value = "/user-consent")
	@Operation(summary = "save user consent", description = "Store the user consent in the database.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<UserDetailsDto> saveUserConsent() {
		return userManagementService.saveUserConsent();
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUserconsent())")
	@GetMapping(value = "/user-consent")
	@Operation(summary = "Retrieve the user consent status.", description = "Retrieve the user consent status.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<UserDetailsDto> isUserConsentGiven() {
		return userManagementService.isUserConsentGiven();
	}

	@GetMapping(value = "/configs")
	@Operation(summary = "Get config", description = "Get configuration values")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<Map<String, String>> getConfigValues() {
		ResponseWrapperV2<Map<String, String>> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setId(getConfigsId);
		responseWrapper.setVersion(VERSION);
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("grantTypes", grantTypes);
		configMap.put("clientAuthMethods", clientAuthMethods);
		configMap.put("inActivityTimer", inActivityTimer);
		configMap.put("inActivityPromptTimer", inActivityPromptTimer);
		configMap.put("axiosTimeout", axiosTimeout);
		configMap.put("maxAllowedExpiryYear", maxAllowedExpiryYear);
		configMap.put("maxAllowedCreatedYear", maxAllowedCreatedYear);
		configMap.put("itemsPerPage", itemsPerPage);
		responseWrapper.setResponse(configMap);
		System.out.println(responseWrapper);
		return responseWrapper;
	}
}
