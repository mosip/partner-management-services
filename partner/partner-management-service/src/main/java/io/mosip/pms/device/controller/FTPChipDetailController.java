package io.mosip.pms.device.controller;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
import io.mosip.pms.device.dto.FtmChipFilterDto;
import io.mosip.pms.device.request.dto.*;
import io.mosip.pms.device.response.dto.*;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
import io.mosip.pms.partner.util.FeatureAvailabilityUtil;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.service.FtpChipDetailService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/ftpchipdetail")
@Api(tags = { "FTPChipDetail" })
public class FTPChipDetailController {
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired	
	FtpChipDetailService ftpChipDetaillService;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	RequestValidator requestValidator;

	@Autowired
	FeatureAvailabilityUtil featureAvailabilityUtil;

	@Value("${mosip.pms.api.id.deactivate.ftm.patch}")
	private  String patchDeactivateFtm;

	/**
	 * Post API to insert a new row of ftpChipDetail data
	 * 
	 * @param chipDetailRequestDto input parameter chipDetailRequestDto
	 * 
	 * @return ResponseEntity DeviceDetail which is inserted successfully
	 *         {@link ResponseEntity}
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostftpchipdetail())")
	@ResponseFilter
	@PostMapping
	@Operation(summary = "Service to save ftpChipDetail", description = "Saves ftpChipDetail and return ftpChipDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftpChipDetail successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating ftpChipDetail any error occured") })
	public ResponseWrapper<IdDto> createDeviceDetail(
			@Valid @RequestBody RequestWrapper<FtpChipDetailDto> chipDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.CREATE_API_IS_CALLED + FtpChipDetailDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CREATE_API_IS_CALLED + FtpChipDetailDto.class.getCanonicalName(),
				"AUT-001", chipDetailRequestDto.getRequest().getFtpProviderId(), "partnerId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
		.setResponse(ftpChipDetaillService.createFtpChipDetails(chipDetailRequestDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_CREATE , FtpChipDetailDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_CREATE , FtpChipDetailDto.class.getCanonicalName()),
				"AUT-005", chipDetailRequestDto.getRequest().getFtpProviderId(), "partnerId");
		return responseWrapper;

	}

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 */
	@Deprecated(since = "release-1.2.2.0")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutftpchipdetail())")
	@ResponseFilter
	@PutMapping
	@Operation(summary = "Service to update ftp chip detail - deprecated since release-1.2.2.0.", description =  "This endpoint has been deprecated since release-1.2.2.0.")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating ftp chip detail any error occured") })
	public ResponseWrapper<IdDto> updateDeviceDetail(
			@Valid @RequestBody RequestWrapper<FtpChipDetailUpdateDto> ftpChipDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.UPDATE_API_IS_CALLED + FtpChipDetailUpdateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.UPDATE_API_IS_CALLED + FtpChipDetailUpdateDto.class.getCanonicalName(),
				"AUT-006", ftpChipDetailRequestDto.getRequest().getFtpChipDetailId(), "ftpChipDetailId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
		.setResponse(ftpChipDetaillService.updateFtpChipDetails(ftpChipDetailRequestDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailUpdateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailUpdateDto.class.getCanonicalName()),
				"AUT-007", ftpChipDetailRequestDto.getRequest().getFtpChipDetailId(), "ftpChipDetailId");
		return responseWrapper;
	}
	
	/**
	 * 
	 * @param deviceDetailRequestDto
	 * @return
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchftpchipdetail())")
	@ResponseFilter
	@PatchMapping
	@Operation(summary = "Service to approve/reject ftp chip detail", description = "Approve ftp chip detail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting ftp chip detail any error occured") })
	public ResponseWrapper<String> approveDeviceDetails(
			@Valid @RequestBody RequestWrapper<FtpChipDetailStatusDto> chipDetailRequestDto){		
		auditUtil.auditRequest(
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + FtpChipDetailStatusDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + FtpChipDetailStatusDto.class.getCanonicalName(),
				"AUT-006", chipDetailRequestDto.getRequest().getFtpChipDetailId(), "ftpChipDetailId");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper
		.setResponse(ftpChipDetaillService.updateFtpChipDetailStatus(chipDetailRequestDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007", chipDetailRequestDto.getRequest().getFtpChipDetailId(), "ftpChipDetailId");

		return responseWrapper;
	}
	
	/**
	 * To Upload FTP Chip Certificate.
	 * 
	 * @param partnerCertRequestDto {@link FtpChipCertificateRequestDto} request
	 * @return {@link PartnerCertificateResponseDto} signed certificate response
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostftpchipdetailuploadcertificate())")
	@RequestMapping(value = "/uploadcertificate", method = RequestMethod.POST)
	@Operation(summary = "Service to upload certificate of ftp chip", description = "uploads the ftp chip certificate and returns success message")
	public ResponseWrapper<FtpCertificateResponseDto> uploadPartnerCertificate(
			@ApiParam("Upload Partner Certificates.") @RequestBody @Valid RequestWrapper<FtpChipCertificateRequestDto> partnerCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		auditUtil.auditRequest(
				DeviceConstant.CERTIFICATE_UPLOAD_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CERTIFICATE_UPLOAD_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				"AUT-006", partnerCertRequestDto.getRequest().getFtpChipDeatilId(), "ftpChipDetailId");
		ResponseWrapper<FtpCertificateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(ftpChipDetaillService.uploadCertificate(partnerCertRequestDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007", partnerCertRequestDto.getRequest().getFtpChipDeatilId(), "ftpChipDetailId");
		return response;
	}
	
    /**
	 * To Download Partner Certificate.
	 * 
	 * @param certDownloadRequestDto {@link FtpCertDownloadRequestDto} request
	 * @return {@link PartnerCertDownloadResponeDto} encrypted Data
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetftpchipdetailgetpartnercertificate())")
	@RequestMapping(value = "/getPartnerCertificate/{ftpChipDetailId}", method = RequestMethod.GET)
	@Operation(summary = "Service to get certificate of ftp chip", description = "Returns the ftp chip certificate")
	public ResponseWrapper<FtpCertDownloadResponeDto> getPartnerCertificate(
			@ApiParam("To download re-signed ftp chip certificate.")  @PathVariable("ftpChipDetailId") @NotNull String ftpChipDetailId) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {		
		ResponseWrapper<FtpCertDownloadResponeDto> response = new ResponseWrapper<>();
		FtpChipCertDownloadRequestDto requestDto = new FtpChipCertDownloadRequestDto();
		requestDto.setFtpChipDetailId(ftpChipDetailId);
		auditUtil.auditRequest(
				DeviceConstant.GET_CERTIFICATE_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.GET_CERTIFICATE_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				"AUT-006", ftpChipDetailId, "ftpChipDetailId");

		if(ftpChipDetaillService.getFtpChipDeatils(ftpChipDetailId) != null) {
			response.setResponse(ftpChipDetaillService.getCertificate(requestDto));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_DOWNLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_DOWNLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007", ftpChipDetailId, "ftpChipDetailId");
		return response;
    }

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new GET /ftpchipdetail/v2 endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostftpchipdetailsearch())")
	@Operation(summary = "Service to search ftp chip details - deprecated since release-1.2.2.0.",
			description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the GET /ftpchipdetail/v2 endpoint.")
	public ResponseWrapper<PageResponseDto<FTPSearchResponseDto>> searchFtpChipDetails(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<FTPSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(ftpChipDetaillService.searchFTPChipDetails(FTPChipDetail.class, request.getRequest()));
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdeactivateftm())")
	@PatchMapping(value = "/{ftmId}")
	@Operation(summary = "This endpoint deactivates the FTM chip details based on the FTM chip details Id.",
	description = "Available since release-1.2.2.0. This endpoint is configured for the roles FTM_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<FtmDetailResponseDto> deactivateFtm(@PathVariable("ftmId") @NotBlank String ftmId, @RequestBody @Valid RequestWrapperV2<DeactivateFtmRequestDto>
			requestWrapper) {
		Optional<ResponseWrapperV2<FtmDetailResponseDto>> validationResponse = requestValidator.validate(patchDeactivateFtm, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return ftpChipDetaillService.deactivateFtm(ftmId, requestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetoriginalftmcertificate())")
	@RequestMapping(value = "/{ftmId}/certificate-data", method = RequestMethod.GET)
	@Operation(summary = "This endpoint fetches both the CA signed certificate uploaded by the FTM Chip Provider and the MOSIP signed certificate generated by PMS",
			description = "Available since release-1.2.2.0. The endpoint is configured for the roles FTM_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<FtmCertificateDownloadResponseDto> getFtmCertificateData(
			@ApiParam("To download original FTM certificate.")  @PathVariable("ftmId") @NotNull String ftmId) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, CertificateException {
		featureAvailabilityUtil.validateCaSignedPartnerCertificateFeatureEnabled();
		return ftpChipDetaillService.getFtmCertificateData(ftmId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersftmchipdetails())")
	@GetMapping(value = "/v2")
	@Operation(summary = "This endpoint retrieves a list of all FTM Chip details created by all the FTM Providers.",
	description = "Available since release-1.2.2.0. This endpoint supports pagination, sorting, and filtering. It is configured for the role PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	ResponseWrapperV2<PageResponseV2Dto<FtmDetailSummaryDto>> getPartnersFtmChipDetails(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") Integer pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "orgName", required = false) String orgName,
			@RequestParam(value = "ftmId", required = false) String ftmId,
			@RequestParam(value = "make", required = false) String make,
			@RequestParam(value = "model", required = false) String model,
			@Parameter(
					description = "Status of FTM",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"approved", "rejected", "pending_cert_upload", "pending_approval", "deactivated"})
			)
			@RequestParam(value = "status", required = false) String status
	) {
		partnerHelper.validateRequestParameters(partnerHelper.ftmAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		FtmChipFilterDto filterDto = new FtmChipFilterDto();
		if (partnerId != null) {
			filterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (orgName != null) {
			filterDto.setOrgName(orgName.toLowerCase());
		}
		if (ftmId != null) {
			filterDto.setFtmId(ftmId.toLowerCase());
		}
		if (make != null) {
			filterDto.setMake(make.toLowerCase());
		}
		if (model != null) {
			filterDto.setModel(model.toLowerCase());
		}
		if (status != null) {
			filterDto.setStatus(status);
		}
		return ftpChipDetaillService.getPartnersFtmChipDetails(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetftmchipdetails())")
	@GetMapping
	@Operation(summary = "This endpoint retrieves a list of all FTM Chip details created by all the FTM Providers associated with the logged in user."
	, description = "Available since release-1.2.2.0. This endpoint is configured for the roles FTM_PROVIDER or PARTNER_ADMIN.")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<List<FtmChipDetailsDto>> ftmChipDetail() {
		return ftpChipDetaillService.ftmChipDetail();
	}
}
