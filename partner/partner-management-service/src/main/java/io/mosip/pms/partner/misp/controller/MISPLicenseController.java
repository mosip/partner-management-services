package io.mosip.pms.partner.misp.controller;

import java.util.List;
import java.util.Optional;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.RequestValidator;
import io.mosip.pms.common.validator.InputValidator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseUpdateRequestDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseSummaryDto;
import io.mosip.pms.partner.misp.dto.MISPFilterDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseDetailsDto;
import io.mosip.pms.partner.misp.dto.MISPDeactivateRequestDto;
import io.mosip.pms.partner.misp.dto.MISPDeactivateResponseDto;
import io.mosip.pms.partner.misp.dto.MISPRegenerateRequestDto;
import io.mosip.pms.partner.misp.service.InfraServiceProviderService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@Api(tags = { "MISP License Detail" })
public class MISPLicenseController {

	@Autowired
	InfraServiceProviderService infraProviderService;

	@Autowired
	private InputValidator inputValidator;

	@Autowired
	RequestValidator requestValidator;

	@Value("${mosip.pms.api.id.misp.generate.license.post}")
	private String postGenerateMISPApiId;

	@Value("${mosip.pms.api.id.deactivate.misp.license.patch}")
	private String patchDeactivateMISPApiId;

	@Value("${mosip.pms.api.id.regenerate.misp.license.put}")
	private String putRegenerateMISPApiId;

	@Deprecated(since = "release-1.3.0-beta.3")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostmisplicense())")
	@PostMapping(value = "/misps")
	@Operation(summary = "Service to generate license for misp - deprecated since release-1.3.0-beta.3", description = "This endpoint has been deprecated since the release-1.3.0-beta.3 and replaced by the POST /misp-licenses endpoint.")
	public ResponseWrapper<MISPLicenseResponseDto> generateLicense(@RequestBody @Valid RequestWrapper<MISPLicenseRequestDto> request){	
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<MISPLicenseResponseDto>();
		response.setResponse(infraProviderService.approveInfraProvider(request.getRequest().getProviderId()));
		return response;
	}

	@Deprecated(since = "release-1.3.0-beta.3")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutmisplicense())")
	@PutMapping(value = "/misps")
	@Operation(summary = "Service to update license details of misp - deprecated since release-1.3.0-beta.3", description = "This endpoint has been deprecated since the release-1.3.0-beta.3 and replaced by the PATCH /misp-licenses/{partnerId} endpoint.")
	public ResponseWrapper<MISPLicenseResponseDto> updateLicenseDetails(@RequestBody @Valid RequestWrapper<MISPLicenseUpdateRequestDto> request){
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<MISPLicenseResponseDto>();
		response.setResponse(infraProviderService.updateInfraProvider(request.getRequest().getProviderId(), request.getRequest().getLicenseKey(),
				request.getRequest().getLicenseKeyStatus()));
		return response;
	}

	@Deprecated(since = "release-1.3.0-beta.3")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetmisplicense())")
	@GetMapping(value = "/misps")
	@Operation(summary = "Service to get license details of misp - deprecated since release-1.3.0-beta.3", description = "This endpoint has been deprecated since the release-1.3.0-beta.3 and replaced by the GET /misp-licenses endpoint.")
	public ResponseWrapper<List<MISPLicenseEntity>> getLicenseDetails(){
		ResponseWrapper<List<MISPLicenseEntity>> response = new ResponseWrapper<>();
		response.setResponse(infraProviderService.getInfraProvider());
		return response;
	}

	@Deprecated(since = "release-1.3.0-beta.3")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetmisplicensekey())")
	@GetMapping(value = "/misps/{mispId}/licenseKey")
	@Operation(summary = "Service to get/regenarate license details of misp - deprecated since release-1.3.0-beta.3", description = "This endpoint has been deprecated since the release-1.3.0-beta.3 and replaced by the PUT /misp-licenses/{partnerId} endpoint.")
	public ResponseWrapper<MISPLicenseResponseDto> regenarteLicenseKey(@PathVariable @Valid String mispId){
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<>();
		response.setResponse(infraProviderService.regenerateKey(mispId));
		return response;
	}

	@Deprecated(since = "release-1.3.0-beta.3")
	@PostMapping("/misps/filtervalues")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostmispfiltervalues())")
	@Operation(summary = "Service to filter misp details - deprecated since release-1.3.0-beta.3", description = "This endpoint has been deprecated since the release-1.3.0-beta.3 and replaced by the GET /misp-licenses endpoint.")
	public ResponseWrapper<FilterResponseCodeDto> filterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(infraProviderService.filterValues(request.getRequest()));
		return responseWrapper;
	}

	@Deprecated(since = "release-1.3.0-beta.3")
	@PostMapping("/misps/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostmispsearch())")
	@Operation(summary = "Service to search misp details  - deprecated since release-1.3.0-beta.3", description = "This endpoint has been deprecated since the release-1.3.0-beta.3 and replaced by the GET /misp-licenses endpoint.")
	public ResponseWrapper<PageResponseDto<MISPLicenseEntity>> search(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<MISPLicenseEntity>> responseWrapper = new ResponseWrapper<>();		
		responseWrapper.setResponse(infraProviderService.search(request.getRequest()));
		return responseWrapper;
	}

	@GetMapping("/misp-licenses")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetallmisplicenses())")
	@Operation(summary = "This endpoint retrieves a list of all MISP Licence Key.",
			description = "Available since release-1.3.0-beta.3. This endpoint supports pagination, sorting, and and filtering based on optional query parameters. It is configured for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> getAllMISPLicenses(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") Integer pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "orgName", required = false) String orgName,
			@RequestParam(value = "policyGroupName", required = false) String policyGroupName,
			@RequestParam(value = "policyName", required = false) String policyName,
			@RequestParam(value = "mispLicenseKeyName", required = false) String mispLicenseKeyName,
			@Parameter(
					description = "Status of MISP License Key.",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"activated", "deactivated"})
			)
			@RequestParam(value = "status", required = false) String status
	) {
		inputValidator.validateRequestInput(sortFieldName);
		inputValidator.validateRequestInput(sortType);
		inputValidator.validateRequestInput(partnerId);
		inputValidator.validateRequestInput(orgName);
		inputValidator.validateRequestInput(policyGroupName);
		inputValidator.validateRequestInput(policyName);
		inputValidator.validateRequestInput(mispLicenseKeyName);
		inputValidator.validateRequestInput(status);
		MISPFilterDto filterDto = new MISPFilterDto();
		if (partnerId != null) {
			filterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (orgName != null) {
			filterDto.setOrgName(orgName.toLowerCase());
		}
		if (policyGroupName != null) {
			filterDto.setPolicyGroupName(policyGroupName.toLowerCase());
		}
		if (policyName != null) {
			filterDto.setPolicyName(policyName.toLowerCase());
		}
		if (mispLicenseKeyName != null) {
			filterDto.setMispLicenseKeyName(mispLicenseKeyName.toLowerCase());
		}
		if (status != null) {
			filterDto.setStatus(status);
		}
		return infraProviderService.getAllMISPLicenses(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@PostMapping("/misp-licenses")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostgeneratemisplicense())")
	@Operation(summary = "This endpoint generates MISP Licence Key.",
			description = "Available since release-1.3.0-beta.3. This endpoint generates MISP Licence Key for given MISP partner. It is configured for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<MISPLicenseResponseDtoV2> generateMISPLicense( @RequestBody @Valid RequestWrapperV2<MISPLicenseRequestDtoV2> requestWrapper) {
		Optional<ResponseWrapperV2<MISPLicenseResponseDtoV2>> validationResponse = requestValidator.validate(postGenerateMISPApiId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		inputValidator.validateRequestInput(requestWrapper.getRequest().getPartnerId());
		inputValidator.validateRequestInput(requestWrapper.getRequest().getPolicyId());
		inputValidator.validateRequestInput(requestWrapper.getRequest().getLicenseKeyName());
		return infraProviderService.generateMISPLicense(requestWrapper.getRequest());
	}

	@GetMapping("/misp-licenses/{partnerId}")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetmisplicensedetails())")
	@Operation(summary = "This endpoint retrieves the details of MISP Licence Key.",
			description = "Available since release-1.3.0-beta.3. This endpoint retrieves the details of MISP Licence Key based on Partner Id and optional query parameters. It is configured for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<MISPLicenseDetailsDto> getMISPLicenseDetails( @PathVariable @Valid String partnerId,
			@RequestParam(value = "policyId", required = false) String policyId,
			@RequestParam(value = "mispLicenseKeyName", required = false) String mispLicenseKeyName
	) {
		inputValidator.validateRequestInput(partnerId);
		inputValidator.validateRequestInput(policyId);
		inputValidator.validateRequestInput(mispLicenseKeyName);
		return infraProviderService.getMISPLicenseDetails(partnerId, policyId, mispLicenseKeyName);
	}

	@PatchMapping("/misp-licenses/{partnerId}")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdeactivatemisplicensekey())")
	@Operation(summary = "This endpoint deactivates the MISP Licence Key.",
			description = "Available since release-1.3.0-beta.3. This endpoint deactivates the MISP Licence Key based on Partner Id and request parameters. It is configured for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<MISPDeactivateResponseDto> deactivateMISPLicense( @PathVariable @Valid String partnerId,
			@RequestBody @Valid RequestWrapperV2<MISPDeactivateRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<MISPDeactivateResponseDto>> validationResponse = requestValidator.validate(patchDeactivateMISPApiId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		inputValidator.validateRequestInput(partnerId);
		inputValidator.validateRequestInput(requestWrapper.getRequest().getPolicyId());
		inputValidator.validateRequestInput(requestWrapper.getRequest().getLicenseKeyName());
		inputValidator.validateRequestInput(requestWrapper.getRequest().getStatus());
		return infraProviderService.deactivateMISPLicense(partnerId, requestWrapper.getRequest());
	}

	@PutMapping("/misp-licenses/{partnerId}")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutregeneratemisplicensekey())")
	@Operation(summary = "This endpoint regenerates the MISP Licence Key.",
			description = "Available since release-1.3.0-beta.3. This endpoint regenerates the MISP Licence Key based on Partner Id and request parameters. It is configured for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<MISPLicenseResponseDtoV2> regenerateMISPLicense( @PathVariable @Valid String partnerId,
			@RequestBody @Valid RequestWrapperV2<MISPRegenerateRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<MISPLicenseResponseDtoV2>> validationResponse = requestValidator.validate(putRegenerateMISPApiId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		inputValidator.validateRequestInput(partnerId);
		inputValidator.validateRequestInput(requestWrapper.getRequest().getPolicyId());
		inputValidator.validateRequestInput(requestWrapper.getRequest().getLicenseKeyName());
		return infraProviderService.regenerateMISPLicense(partnerId, requestWrapper.getRequest());
	}
}
