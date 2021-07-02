package io.mosip.pms.device.controller;

import java.io.IOException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.service.FtpChipDetailService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.regdevice.entity.RegFTPChipDetail;
import io.mosip.pms.device.regdevice.service.RegFTPChipDetailService;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.FtpChipCertDownloadRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipDetailDto;
import io.mosip.pms.device.request.dto.FtpChipDetailStatusDto;
import io.mosip.pms.device.request.dto.FtpChipDetailUpdateDto;
import io.mosip.pms.device.response.dto.FTPSearchResponseDto;
import io.mosip.pms.device.response.dto.FtpCertDownloadResponeDto;
import io.mosip.pms.device.response.dto.FtpCertificateResponseDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/ftpchipdetail")
@Api(tags = { "FTPChipDetail" })
public class FTPChipDetailController {
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired	
	FtpChipDetailService ftpChipDetaillService;
	
	@Autowired	
	RegFTPChipDetailService regFtpChipDetailService;
	
	/**
	 * Post API to insert a new row of ftpChipDetail data
	 * 
	 * @param chipDetailRequestDto input parameter chipDetailRequestDto
	 * 
	 * @return ResponseEntity DeviceDetail which is inserted successfully
	 *         {@link ResponseEntity}
	 */
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','FTM_PROVIDER')")

	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostftpchipdetail())")
	@PostMapping
	@ApiOperation(value = "Service to save ftpChipDetail", notes = "Saves ftpChipDetail and return ftpChipDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftpChipDetail successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating ftpChipDetail any error occured") })
	public ResponseWrapper<IdDto> createDeviceDetail(
			@Valid @RequestBody RequestWrapper<FtpChipDetailDto> chipDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.CREATE_API_IS_CALLED + FtpChipDetailDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CREATE_API_IS_CALLED + FtpChipDetailDto.class.getCanonicalName(),
				"AUT-001");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(chipDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regFtpChipDetailService.createFtpChipDetails(chipDetailRequestDto.getRequest()));
			
		}else {
		responseWrapper
				.setResponse(ftpChipDetaillService.createFtpChipDetails(chipDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_CREATE , FtpChipDetailDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_CREATE , FtpChipDetailDto.class.getCanonicalName()),
				"AUT-005");
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
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','FTM_PROVIDER')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutftpchipdetail())")
	@PutMapping
	@ApiOperation(value = "Service to update ftp chip detail", notes = "Updates ftp chip detail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating ftp chip detail any error occured") })
	public ResponseWrapper<IdDto> updateDeviceDetail(
			@Valid @RequestBody RequestWrapper<FtpChipDetailUpdateDto> ftpChipDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.UPDATE_API_IS_CALLED + FtpChipDetailUpdateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.UPDATE_API_IS_CALLED + FtpChipDetailUpdateDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(ftpChipDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regFtpChipDetailService.updateFtpChipDetails(ftpChipDetailRequestDto.getRequest()));
			
		}else {
		responseWrapper
				.setResponse(ftpChipDetaillService.updateFtpChipDetails(ftpChipDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailUpdateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailUpdateDto.class.getCanonicalName()),
				"AUT-007");
		return responseWrapper;
	}
	
	/**
	 * 
	 * @param deviceDetailRequestDto
	 * @return
	 */
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchftpchipdetail())")
	@PatchMapping
	@ApiOperation(value = "Service to approve/reject ftp chip detail", notes = "Approve ftp chip detail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting ftp chip detail any error occured") })
	public ResponseWrapper<String> approveDeviceDetails(
			@Valid @RequestBody RequestWrapper<FtpChipDetailStatusDto> chipDetailRequestDto){		
		auditUtil.auditRequest(
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + FtpChipDetailStatusDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + FtpChipDetailStatusDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		if(chipDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regFtpChipDetailService.updateFtpChipDetailStatus(chipDetailRequestDto.getRequest()));
			
		}else {
			responseWrapper
			.setResponse(ftpChipDetaillService.updateFtpChipDetailStatus(chipDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , FtpChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007");

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
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','FTM_PROVIDER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostftpchipdetailuploadcertificate())")
	@RequestMapping(value = "/uploadcertificate", method = RequestMethod.POST)
	public ResponseWrapper<FtpCertificateResponseDto> uploadPartnerCertificate(
			@ApiParam("Upload Partner Certificates.") @RequestBody @Valid RequestWrapper<FtpChipCertificateRequestDto> partnerCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		auditUtil.auditRequest(
				DeviceConstant.CERTIFICATE_UPLOAD_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CERTIFICATE_UPLOAD_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<FtpCertificateResponseDto> response = new ResponseWrapper<>();
		if(partnerCertRequestDto.getRequest().getIsItForRegistrationDevice()) {
			response.setResponse(regFtpChipDetailService.uploadPartnerCertificate(partnerCertRequestDto.getRequest()));
		}else {
		response.setResponse(ftpChipDetaillService.uploadPartnerCertificate(partnerCertRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007");
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
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','FTM_PROVIDER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetftpchipdetailgetpartnercertificate())")
	@RequestMapping(value = "/getPartnerCertificate/{ftpChipDetailId}", method = RequestMethod.GET)
	public ResponseWrapper<FtpCertDownloadResponeDto> getPartnerCertificate(
			@ApiParam("To download re-signed ftp chip certificate.")  @PathVariable("ftpChipDetailId") @NotNull String ftpChipDetailId) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {		
		ResponseWrapper<FtpCertDownloadResponeDto> response = new ResponseWrapper<>();
		FtpChipCertDownloadRequestDto requestDto = new FtpChipCertDownloadRequestDto();
		requestDto.setFtpChipDetailId(ftpChipDetailId);
		auditUtil.auditRequest(
				DeviceConstant.GET_CERTIFICATE_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.GET_CERTIFICATE_API_IS_CALLED + FtpChipCertificateRequestDto.class.getCanonicalName(),
				"AUT-006");

		if(ftpChipDetaillService.getFtpChipDeatils(ftpChipDetailId) != null) {
			response.setResponse(ftpChipDetaillService.getPartnerCertificate(requestDto));
		}else {
			response.setResponse(regFtpChipDetailService.getPartnerCertificate(requestDto));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_DOWNLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_DOWNLOAD , FtpChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007");
		return response;
    }
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostftpchipdetailsearch())")
	@PostMapping("/search")
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','FTM_PROVIDER')")
	public ResponseWrapper<PageResponseDto<FTPSearchResponseDto>> searchFtpChipDetails(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<FTPSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())) {
			responseWrapper.setResponse(regFtpChipDetailService.searchFTPChipDetails(RegFTPChipDetail.class, request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(ftpChipDetaillService.searchFTPChipDetails(FTPChipDetail.class, request.getRequest()));
		return responseWrapper;
	}
}
