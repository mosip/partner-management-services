package io.mosip.pms.device.controller;

import javax.validation.Valid;

import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.request.dto.DeactivateDeviceRequestDto;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.device.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.util.RequestValidator;
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

	@Value("${mosip.pms.api.id.add.inactive.mapping.device.to.sbi.id.post}")
	private  String postInactiveMappingDeviceToSbiId;

	@Value("${mosip.pms.api.id.deactivate.device.post}")
	private  String postDeactivateDeviceId;
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired	
	DeviceDetailService deviceDetaillService;

	@Autowired
	RequestValidator requestValidator;
	
	/**
	 * Post API to insert a new row of DeviceDetail data
	 * 
	 * @param deviceDetailRequestDto input parameter deviceRequestDto
	 * 
	 * @return ResponseEntity DeviceDetail which is inserted successfully
	 *         {@link ResponseEntity}
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetail())")
	@ResponseFilter
	@PostMapping
	@Operation(summary = "Service to save DeviceDetail", description = "Saves DeviceDetail and return DeviceDetail id")
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

	/**
	 * Put API to update a row of DeviceDetail data
	 * 
	 * @param deviceDetailRequestDto input parameter deviceRequestDto
	 * 
	 * @return ResponseEntity DeviceDetail which is updated successfully
	 *         {@link ResponseEntity}
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutdevicedetail())")
	@ResponseFilter
	@PutMapping
	@Operation(summary = "Service to update DeviceDetails", description = "This API has been deprecated since 1.3.x release.")
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
	
	/**
	 * 
	 * @param deviceDetailRequestDto
	 * @return
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdevicedetail())")
	@ResponseFilter
	@PatchMapping
	@Operation(summary = "Service to approve/reject DeviceDetail", description = "Approve DeviceDetail and returns success message")
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
	
	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetailsearch())")
	@Operation(summary = "Service to search DeviceDetails", description = "ervice to search DeviceDetails")
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
	
	@ResponseFilter
	@PostMapping("/filtervalues")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetailfiltervalues())")
	@Operation(summary = "Service to filter DeviceDetails", description = "Service to filter DeviceDetails")
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

	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeviceproviderservice())")
	@PostMapping(value = "/inactive-mapping-device-to-sbi")
	@Operation(summary = "Add inactive device mapping to SBI.", description = "Add inactive device mapping to SBI.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<Boolean> inactiveMappingDeviceToSbi(@RequestBody @Valid RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<Boolean>> validationResponse = requestValidator.validate(postInactiveMappingDeviceToSbiId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return deviceDetaillService.inactiveMappingDeviceToSbi(requestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getDeviceproviderservice())")
	@PostMapping(value = "/deactivate-device")
	@Operation(summary = "Deactivate device details", description = "Deactivate device details")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<DeviceDetailResponseDto> deactivateDevice(@RequestBody @Valid RequestWrapperV2<DeactivateDeviceRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<DeviceDetailResponseDto>> validationResponse = requestValidator.validate(postDeactivateDeviceId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return deviceDetaillService.deactivateDevice(requestWrapper.getRequest().getDeviceId());
	}

}
