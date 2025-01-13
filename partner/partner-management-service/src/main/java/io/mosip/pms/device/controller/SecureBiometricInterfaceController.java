package io.mosip.pms.device.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.SbiFilterDto;
import io.mosip.pms.partner.dto.DeviceDto;
import io.mosip.pms.device.response.dto.SbiDetailsResponseDto;
import io.mosip.pms.device.dto.SbiDetailsDto;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.common.util.RequestValidator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.DeactivateSbiRequestDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.MappedDeviceDetailsReponse;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;
import io.mosip.pms.device.response.dto.SbiSummaryDto;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/securebiometricinterface")
@Api(tags = { "SecureBiometricInterface" })
public class SecureBiometricInterfaceController {

	@Value("${mosip.pms.api.id.add.device.to.sbi.id.post}")
	private  String postAddDeviceToSbi;

	@Value("${mosip.pms.api.id.deactivate.sbi.patch}")
	private  String patchDeactivateSbi;

	@Autowired
	SecureBiometricInterfaceService secureBiometricInterface;

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	RequestValidator requestValidator;

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterface())")
	@ResponseFilter
	@PostMapping
	@Operation(summary = "Service to save SecureBiometricInterface details", description = "Saves SecureBiometricInterface details and return SBI id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterfaceCreateDto successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating SecureBiometricInterfaceCreateDto any error occured") })
	public ResponseWrapper<IdDto> SecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceCreateDto> secureBiometricInterfaceCreateDto) {
		auditUtil.auditRequest(
				DeviceConstant.CREATE_API_IS_CALLED + SecureBiometricInterfaceCreateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CREATE_API_IS_CALLED + SecureBiometricInterfaceCreateDto.class.getCanonicalName(),
				"AUT-011", secureBiometricInterfaceCreateDto.getRequest().getProviderId(), "partnerId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.createSecureBiometricInterface(secureBiometricInterfaceCreateDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_CREATE,
						SecureBiometricInterfaceCreateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM, String.format(DeviceConstant.SUCCESSFUL_CREATE,
						SecureBiometricInterfaceCreateDto.class.getCanonicalName()),
				"AUT-012", secureBiometricInterfaceCreateDto.getRequest().getProviderId(), "partnerId");
		return responseWrapper;

	}

	/*
	 * This endpoint has been deprecated since the release-1.3.x
	 */
	@Deprecated(since = "release-1.3.x")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutsecurebiometricinterface())")
	@ResponseFilter
	@PutMapping
	@Operation(summary = "Service to update SecureBiometricInterface - deprecated since release-1.3.x.",
			description = "This endpoint has been deprecated since release-1.3.x.")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterface successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating SecureBiometricInterface any error occured") })
	public ResponseWrapper<IdDto> updateSecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceUpdateDto> secureBiometricInterfaceUpdateDto) {
		auditUtil.auditRequest(
				DeviceConstant.UPDATE_API_IS_CALLED + SecureBiometricInterfaceUpdateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.UPDATE_API_IS_CALLED + SecureBiometricInterfaceUpdateDto.class.getCanonicalName(),
				"AUT-013", secureBiometricInterfaceUpdateDto.getRequest().getId(), "sbiId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.updateSecureBiometricInterface(secureBiometricInterfaceUpdateDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE,
						SecureBiometricInterfaceUpdateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM, String.format(DeviceConstant.SUCCESSFUL_UPDATE,
						SecureBiometricInterfaceUpdateDto.class.getCanonicalName()),
				"AUT-012", secureBiometricInterfaceUpdateDto.getRequest().getId(), "sbiId");
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchsecurebiometricinterface())")
	@ResponseFilter
	@PatchMapping
	@Operation(summary = "Service to approve/reject SecureBiometricInterface", description = "Approve SecureBiometricInterface and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterface successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting DeviceDetail any error occured") })
	public ResponseWrapper<String> approveSecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> secureBiometricInterfaceStatusUpdateDto) {
		auditUtil.auditRequest(
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				"AUT-006", secureBiometricInterfaceStatusUpdateDto.getRequest().getId(), "sbiId");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.updateSecureBiometricInterfaceStatus(secureBiometricInterfaceStatusUpdateDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE, UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE, UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				"AUT-007", secureBiometricInterfaceStatusUpdateDto.getRequest().getId(), "sbiId");

		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.3.x
	 * It has been replaced by the new GET /securebiometricinterface/search/v2 endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.3.x")
	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterfacesearch())")
	@Operation(summary = "Service to search SecureBiometricInterface details - deprecated since release-1.3.x.",
			description = "This endpoint has been deprecated since the release-1.3.x and replaced by the GET /securebiometricinterface/search/v2 endpoint.")
	public ResponseWrapper<PageResponseDto<SbiSearchResponseDto>> searchSecureBiometric(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<SbiSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.searchSecureBiometricInterface(SecureBiometricInterface.class, request.getRequest()));
		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.3.x
	 * It has been replaced by the new POST /securebiometricinterface/{sbiId}/devices endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.3.x")
	@ResponseFilter
	@PutMapping("/devicedetails/map")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutsecurebiometricinterfacedevicedetailsmap())")
	@Operation(summary = "Service to map device details with sbi - deprecated since release-1.3.x.",
			description = "This endpoint has been deprecated since the release-1.3.x and replaced by the POST /securebiometricinterface/{sbiId}/devices endpoint.")
	public ResponseWrapper<String> mapDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceDetailSBIMappingDto> request) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface.mapDeviceDetailAndSbi(request.getRequest()));
		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.3.x
	 */
	@Deprecated(since = "release-1.3.x")
	@ResponseFilter
	@PutMapping("/devicedetails/map/remove")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutsecurebiometricinterfacedevicedetailsmapremove())")
	@Operation(summary = "Service to remove mapped device details with sbi - deprecated since release-1.3.x.",
			description = "This endpoint has been deprecated since release-1.3.x.")
	public ResponseWrapper<String> removeMappedDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceDetailSBIMappingDto> request) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface.deleteDeviceDetailAndSbiMapping(request.getRequest()));
		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.3.x
	 * It has been replaced by the new GET /securebiometricinterface/{sbiId}/devices endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.3.x")
	@ResponseFilter
	@PostMapping("/devicedetails/map/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterfacedevicedetailsmapsearch())")
	@Operation(summary = "Service to search mapped device details and SecureBiometricInterface details - deprecated since release-1.3.x.",
			description = "This endpoint has been deprecated since the release-1.3.x and replaced by the GET /securebiometricinterface/{sbiId}/devices endpoint.")
	public ResponseWrapper<PageResponseDto<MappedDeviceDetailsReponse>> searchMappedDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<MappedDeviceDetailsReponse>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				secureBiometricInterface.searchMappedDeviceDetails(DeviceDetailSBI.class, request.getRequest()));
		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.3.x
	 * It has been replaced by the new GET /securebiometricinterface/search/v2 endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.3.x")
	@ResponseFilter
	@PostMapping("/filtervalues")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterfacefiltervalues())")
	@Operation(summary = "Service to filter SBI's - deprecated since release-1.3.x.",
			description = "This endpoint has been deprecated since the release-1.3.x and replaced by the GET /securebiometricinterface/search/v2 endpoint.")
	public ResponseWrapper<FilterResponseCodeDto> filterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface.filterValues(request.getRequest()));
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostadddevicetosbi())")
	@PostMapping(value = "/{sbiId}/devices")
	@Operation(summary = "This endpoint adds a new Device and creates an inactive mapping between the device and the given SBI.", 
	description = "Avaiable since release-1.3.x. This endpoint is configured for the roles DEVICE_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<IdDto> addDeviceToSbi(@PathVariable("sbiId") @NotBlank String sbiId, @RequestBody @Valid RequestWrapperV2<DeviceDetailDto> requestWrapper) {
		Optional<ResponseWrapperV2<IdDto>> validationResponse = requestValidator.validate(postAddDeviceToSbi, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return secureBiometricInterface.addDeviceToSbi(requestWrapper.getRequest(), sbiId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetsbidetails())")
	@GetMapping(value = "/{sbiId}/devices")
	@Operation(summary = "This endpoint fetches the list of Devices associated with a given SBI Id", 
	description = "Avaiable since release-1.3.x. This endpoint is configured for the roles DEVICE_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<List<DeviceDto>> getAllDevicesForSbi(@PathVariable String sbiId) {
		return secureBiometricInterface.getAllDevicesForSbi(sbiId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetsbidetails())")
	@GetMapping
	@Operation(summary = "This endpoint retrieves a list of all SBIs created by all the Device Providers associated with the logged in user."
	, description = "Available since release-1.3.x. This endpoint is configured for the roles DEVICE_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<List<SbiDetailsDto>> getSbiDetails() {
		return secureBiometricInterface.getSbiDetails();
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdeactivatesbi())")
	@PatchMapping(value = "/{sbiId}")
	@Operation(summary = "This endpoint deactivates an SBI along with associated Devices.",
			description = "Avaiable since release-1.3.x. This endpoint is configured for the roles DEVICE_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<SbiDetailsResponseDto> deactivateSbi(@PathVariable("sbiId") @NotBlank String sbiId, @RequestBody @Valid RequestWrapperV2<DeactivateSbiRequestDto>
			requestWrapper) {
		Optional<ResponseWrapperV2<SbiDetailsResponseDto>> validationResponse = requestValidator.validate(patchDeactivateSbi, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return secureBiometricInterface.deactivateSbi(sbiId, requestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetallsbidetails())")
	@GetMapping(value = "/search/v2")
	@Operation(summary = "This endpoint retrieves a list of all SBIs created by all the Device Providers.",
	description = "Avaiable since release-1.3.x. This endpoint supports pagination, sorting, and filtering. It is configured for the role PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	ResponseWrapperV2<PageResponseV2Dto<SbiSummaryDto>> getAllSbiDetails(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") int pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "orgName", required = false) String orgName,
			@RequestParam(value = "sbiId", required = false) String sbiId,
			@RequestParam(value = "sbiVersion", required = false) String sbiVersion,
			@Parameter(
					description = "Status of SBI",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"approved", "rejected", "pending_approval", "deactivated"})
			)
			@RequestParam(value = "status", required = false) String status,
			@Parameter(
					description = "Status of SBI based on expiry date time",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"expired", "valid"})
			)
			@RequestParam(value = "sbiExpiryStatus", required = false) String sbiExpiryStatus
	) {
		partnerHelper.validateRequestParameters(partnerHelper.sbiAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		SbiFilterDto filterDto = new SbiFilterDto();
		if (partnerId != null) {
			filterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (orgName != null) {
			filterDto.setOrgName(orgName.toLowerCase());
		}
		if (sbiId != null) {
			filterDto.setSbiId(sbiId.toLowerCase());
		}
		if (sbiVersion != null) {
			filterDto.setSbiVersion(sbiVersion.toLowerCase());
		}
		if (status != null) {
			filterDto.setStatus(status);
		}
		if (sbiExpiryStatus != null) {
			filterDto.setSbiExpiryStatus(sbiExpiryStatus);
		}
		return secureBiometricInterface.getAllSbiDetails(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}
}
