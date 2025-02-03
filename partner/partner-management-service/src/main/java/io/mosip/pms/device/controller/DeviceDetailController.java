package io.mosip.pms.device.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.DeviceDetailFilterDto;
import io.mosip.pms.device.dto.DeviceDetailSummaryDto;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.device.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.common.util.RequestValidator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.dto.DeviceFilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pms.device.authdevice.service.DeviceDetailService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeviceDetailUpdateDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.DeactivateDeviceRequestDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.response.dto.DeviceDetailSearchResponseDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.RegistrationSubTypeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Optional;

@RestController
@RequestMapping(value = "/devicedetail")
@Api(tags = { "DeviceDetail" })
public class DeviceDetailController {

	@Value("${mosip.pms.api.id.approval.mapping.device.to.sbi.post}")
	private String postApprovalMappingDeviceToSbiId;

	@Value("${mosip.pms.api.id.deactivate.device.patch}")
	private  String patchDeactivateDevice;
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired	
	DeviceDetailService deviceDetaillService;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	RequestValidator requestValidator;

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new POST /securebiometricinterface/{sbiId}/devices endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetail())")
	@ResponseFilter
	@PostMapping
	@Operation(summary = "Service to save DeviceDetail - deprecated since release-1.2.2.0.", description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the POST /securebiometricinterface/{sbiId}/devices endpoint.")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating DeviceDetail any error occured") })
	public ResponseWrapper<IdDto> createDeviceDetail(
			@Valid @RequestBody RequestWrapper<DeviceDetailDto> deviceDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.CREATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CREATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				"AUT-001", deviceDetailRequestDto.getRequest().getDeviceProviderId(), "partnerId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
		.setResponse(deviceDetaillService.createDeviceDetails(deviceDetailRequestDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_CREATE , DeviceDetailDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_CREATE , DeviceDetailDto.class.getCanonicalName()),
				"AUT-005", deviceDetailRequestDto.getRequest().getDeviceProviderId(), "partnerId");
		return responseWrapper;

	}

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 */
	@Deprecated(since = "release-1.2.2.0")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutdevicedetail())")
	@ResponseFilter
	@PutMapping
	@Operation(summary = "Service to update DeviceDetails - deprecated since release-1.2.2.0.", description = "This endpoint has been deprecated since release-1.2.2.0.")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating DeviceDetail any error occured") })
	public ResponseWrapper<IdDto> updateDeviceDetail(
			@Valid @RequestBody RequestWrapper<DeviceDetailUpdateDto> deviceDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.UPDATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.UPDATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				"AUT-006", deviceDetailRequestDto.getRequest().getId(), "deviceDetailId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
		.setResponse(deviceDetaillService.updateDeviceDetails(deviceDetailRequestDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , DeviceDetailDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , DeviceDetailDto.class.getCanonicalName()),
				"AUT-007", deviceDetailRequestDto.getRequest().getId(), "deviceDetailId");
		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new PATCH devicedetail/{id}/approval endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdevicedetail())")
	@ResponseFilter
	@PatchMapping
	@Operation(summary = "Service to approve/reject DeviceDetail - deprecated since release-1.2.2.0.",
			description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the PATCH /devicedetail/{id}/approval endpoint.")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting DeviceDetail any error occured") })
	public ResponseWrapper<String> approveDeviceDetails(
			@Valid @RequestBody RequestWrapper<UpdateDeviceDetailStatusDto> deviceDetailRequestDto){
		auditUtil.auditRequest(
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				"AUT-006", deviceDetailRequestDto.getRequest().getId(), "deviceDetailId");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper
		.setResponse(deviceDetaillService.updateDeviceDetailStatus(deviceDetailRequestDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				"AUT-007", deviceDetailRequestDto.getRequest().getId(), "deviceDetailId");

		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new GET /devicedetail/search/v2 endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetailsearch())")
	@Operation(summary = "Service to search DeviceDetails - deprecated since release-1.2.2.0.",
			description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the GET /devicedetail endpoint.")
	public ResponseWrapper<PageResponseDto<DeviceDetailSearchResponseDto>> searchDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<DeviceDetailSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(deviceDetaillService.searchDeviceDetails(DeviceDetail.class, request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PostMapping("/deviceType/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetaildevicetypesearch())")
	@Operation(summary = "Service to search DeviceTypes", description = "service to search DeviceTypes")
	public ResponseWrapper<PageResponseDto<RegistrationSubTypeDto>> searchDeviceType(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<RegistrationSubTypeDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(deviceDetaillService.searchDeviceType(RegistrationDeviceSubType.class, request.getRequest()));
		return responseWrapper;
	}

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new GET /devicedetail/search/v2 endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@ResponseFilter
	@PostMapping("/filtervalues")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetailfiltervalues())")
	@Operation(summary = "Service to filter DeviceDetails - deprecated since release-1.2.2.0.",
			description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the GET /devicedetail endpoint.")
	public ResponseWrapper<FilterResponseCodeDto> filterValues(
			@RequestBody @Valid RequestWrapper<DeviceFilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(deviceDetaillService.deviceFilterValues(request.getRequest()));
		return responseWrapper;

	}
	
	@ResponseFilter
	@PostMapping("/deviceType/filtervalues")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetaildevicetypefiltervalues())")
	@Operation(summary = "Service to filter DeviceTypes", description = "Service to filter DeviceTypes")
	public ResponseWrapper<FilterResponseCodeDto> filterDeviceType(
			@RequestBody @Valid RequestWrapper<DeviceFilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(deviceDetaillService.deviceTypeFilterValues(request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PostMapping("/deviceSubType/filtervalues")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetaildevicesubtypefiltervalues())")
	@Operation(summary = "Service to filter DeviceSubTypes", description = "Service to filter DeviceSubTypes")
	public ResponseWrapper<FilterResponseCodeDto> filterDeviceSubType(
			@RequestBody @Valid RequestWrapper<DeviceFilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(deviceDetaillService.deviceSubTypeFilterValues(request.getRequest()));
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdeactivatedevice())")
	@PatchMapping(value = "/{deviceId}")
	@Operation(summary = "This endpoint deactivates a Device based on the Device Id.",
	description = "Available since release-1.2.2.0. This endpoint is configured for the roles DEVICE_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<DeviceDetailResponseDto> deactivateDevice(@PathVariable("deviceId") @NotBlank String deviceId, @RequestBody @Valid RequestWrapperV2<DeactivateDeviceRequestDto>
			requestWrapper) {
		Optional<ResponseWrapperV2<DeviceDetailResponseDto>> validationResponse = requestValidator.validate(patchDeactivateDevice, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return deviceDetaillService.deactivateDevice(deviceId, requestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicewithsbimapping())")
	@PostMapping(value = "/{id}/approval")
	@Operation(summary = "This endpoint is for the Partner Admin user to approve or reject a Device and activate the mapping between the Device and the SBI.",
			description = "Available since release-1.2.2.0.  It is configured for the role PARTNER_ADMIN")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<Boolean> approveOrRejectMappingDeviceToSbi(@PathVariable("id") String deviceId, @RequestBody @Valid RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<Boolean>> validationResponse = requestValidator.validate(postApprovalMappingDeviceToSbiId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return deviceDetaillService.approveOrRejectMappingDeviceToSbi(deviceId, requestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetalldevicedetails())")
	@GetMapping
	@Operation(summary = "This endpoint retrieves a list of all the Devices.",
	description = "Available since release-1.2.2.0. This endpoint supports pagination, sorting, and filtering. It is configured for the role PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<DeviceDetailSummaryDto>> getAllDeviceDetails(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") Integer pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "orgName", required = false) String orgName,
			@RequestParam(value = "deviceType", required = false) String deviceType,
			@RequestParam(value = "deviceSubType", required = false) String deviceSubType,
			@Parameter(
					description = "Status of device",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"approved", "rejected", "pending_approval", "deactivated"})
			)
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "make", required = false) String make,
			@RequestParam(value = "model", required = false) String model,
			@RequestParam(value = "sbiId", required = false) String sbiId,
			@RequestParam(value = "sbiVersion", required = false) String sbiVersion,
			@RequestParam(value = "deviceId", required = false) String deviceId
	) {
		partnerHelper.validateRequestParameters(partnerHelper.deviceAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		DeviceDetailFilterDto filterDto = new DeviceDetailFilterDto();
		if (partnerId != null) {
			filterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (deviceType != null) {
			filterDto.setDeviceType(deviceType.toLowerCase());
		}
		if (orgName != null) {
			filterDto.setOrgName(orgName.toLowerCase());
		}
		if (status != null) {
			filterDto.setStatus(status);
		}
		if (deviceSubType != null) {
			filterDto.setDeviceSubType(deviceSubType.toLowerCase());
		}
		if (make != null) {
			filterDto.setMake(make.toLowerCase());
		}
		if (model != null) {
			filterDto.setModel(model.toLowerCase());
		}
		if (sbiId != null) {
			filterDto.setSbiId(sbiId.toLowerCase());
		}
		if (sbiVersion != null) {
			filterDto.setSbiVersion(sbiVersion.toLowerCase());
		}
		if (deviceId != null) {
			filterDto.setDeviceId(deviceId.toLowerCase());
		}
		return deviceDetaillService.getAllDeviceDetails(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}
}
