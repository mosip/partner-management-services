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

	@Value("${mosip.pms.ca.signed.partner.certificate.available}")
	private String isCaSignedPartnerCertificateAvailable;

	@Value("${mosip.pms.oidc.client.available}")
	private String isOidcClientAvailable;

	@Value("${mosip.pms.root.and.intermediate.certificates.available}")
	private String isRootIntermediateCertAvailable;

	public static final String VERSION = "1.0";

	@Autowired
	UserManagementService userManagementService;

	@PostMapping(value = "/users")
	@PreAuthorize("hasAnyRole('MISP_PARTNER','PARTNER_ADMIN','AUTH_PARTNER','CREDENTIAL_PARTNER','ONLINE_VERIFICATION_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','ABIS_PARTNER','MANUAL_ADJUDICATION','SDK_PARTNER')")
	public ResponseWrapper<MosipUserDto> registerUser(
			@RequestBody @Valid RequestWrapper<UserRegistrationRequestDto> request) {
		ResponseWrapper<MosipUserDto> response = new ResponseWrapper<MosipUserDto>();
		response.setResponse(userManagementService.registerUser(request.getRequest()));
		return response;

	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUserconsent())")
	@PostMapping(value = "users/user-consent")
	@Operation(summary = "Added in release-1.2.2.0, This endpoint saves the user's consent related to data captured by the PMS portal",
			description = "This endpoint saves the user's consent related to data captured by the PMS portal, which is requested only once after the user's first login. Once provided, the consent will not be asked again. It is configured for all Partner Type roles.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<UserDetailsDto> saveUserConsent() {
		return userManagementService.saveUserConsent();
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUserconsent())")
	@GetMapping(value = "users/user-consent")
	@Operation(summary = "Added in release-1.2.2.0, This endpoint fetches the user's consent related to the data captured by PMS",
			description = "This endpoint fetches the user's consent related to the data captured by PMS. The consent is requested only once after the user's first login, and won't be asked again if already given. It is configured for all Partner Type roles.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<UserDetailsDto> isUserConsentGiven() {
		return userManagementService.isUserConsentGiven();
	}

	@GetMapping(value = "/system-config")
	@Operation(summary = "Added in release-1.2.2.0, This endpoint fetches the configurations for PMS", description = "This endpoint fetches the configurations for PMS and sends them to the UI. No roles are required for access.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<Map<String, String>> getSystemConfig() {
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
		configMap.put("isCaSignedPartnerCertificateAvailable", isCaSignedPartnerCertificateAvailable);
		configMap.put("isOidcClientAvailable", isOidcClientAvailable);
		configMap.put("isRootIntermediateCertAvailable", isRootIntermediateCertAvailable);
		responseWrapper.setResponse(configMap);
		System.out.println(responseWrapper);
		return responseWrapper;
	}
}
