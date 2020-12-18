package io.mosip.pmp.authdevice.controller;

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
import io.mosip.pmp.authdevice.dto.FTPChipCertDownloadRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipCertificateRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailStatusDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.service.FTPChipDetailService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.dto.PartnerCertDownloadRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertDownloadResponeDto;
import io.mosip.pmp.partner.dto.PartnerCertificateResponseDto;
import io.mosip.pmp.regdevice.service.RegFTPChipDetailService;
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
	FTPChipDetailService ftpChipDetaillService;
	
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
	@PreAuthorize("hasRole('FTM_PROVIDER')")
	@ResponseFilter
	@PostMapping
	@ApiOperation(value = "Service to save ftpChipDetail", notes = "Saves ftpChipDetail and return ftpChipDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftpChipDetail successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating ftpChipDetail any error occured") })
	public ResponseWrapper<IdDto> createDeviceDetail(
			@Valid @RequestBody RequestWrapper<FTPChipDetailDto> chipDetailRequestDto) {
		auditUtil.auditRequest(
				AuthDeviceConstant.CREATE_API_IS_CALLED + FTPChipDetailDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.CREATE_API_IS_CALLED + FTPChipDetailDto.class.getCanonicalName(),
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
				String.format(AuthDeviceConstant.SUCCESSFUL_CREATE , FTPChipDetailDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_CREATE , FTPChipDetailDto.class.getCanonicalName()),
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
	@PreAuthorize("hasRole('FTM_PROVIDER')")
	@ResponseFilter
	@PutMapping
	@ApiOperation(value = "Service to update ftp chip detail", notes = "Updates ftp chip detail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating ftp chip detail any error occured") })
	public ResponseWrapper<IdDto> updateDeviceDetail(
			@Valid @RequestBody RequestWrapper<FTPChipDetailUpdateDto> ftpChipDetailRequestDto) {
		auditUtil.auditRequest(
				AuthDeviceConstant.UPDATE_API_IS_CALLED + FTPChipDetailUpdateDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.UPDATE_API_IS_CALLED + FTPChipDetailUpdateDto.class.getCanonicalName(),
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
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailUpdateDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailUpdateDto.class.getCanonicalName()),
				"AUT-007");
		return responseWrapper;
	}
	
	/**
	 * 
	 * @param deviceDetailRequestDto
	 * @return
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@ResponseFilter
	@PatchMapping
	@ApiOperation(value = "Service to approve/reject ftp chip detail", notes = "Approve ftp chip detail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting ftp chip detail any error occured") })
	public ResponseWrapper<String> approveDeviceDetails(
			@Valid @RequestBody RequestWrapper<FTPChipDetailStatusDto> chipDetailRequestDto){		
		auditUtil.auditRequest(
				AuthDeviceConstant.STATUS_UPDATE_API_IS_CALLED + FTPChipDetailStatusDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.STATUS_UPDATE_API_IS_CALLED + FTPChipDetailStatusDto.class.getCanonicalName(),
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
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailStatusDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007");

		return responseWrapper;
	}
	
	/**
	 * To Upload FTP Chip Certificate.
	 * 
	 * @param partnerCertRequestDto {@link FTPChipCertificateRequestDto} request
	 * @return {@link PartnerCertificateResponseDto} signed certificate response
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@PreAuthorize("hasAnyRole('FTM_PROVIDER')")
	@RequestMapping(value = "/uploadcertificate", method = RequestMethod.POST)
	public ResponseWrapper<PartnerCertificateResponseDto> uploadPartnerCertificate(
			@ApiParam("Upload Partner Certificates.") @RequestBody @Valid RequestWrapper<FTPChipCertificateRequestDto> partnerCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		auditUtil.auditRequest(
				AuthDeviceConstant.CERTIFICATE_UPLOAD_API_IS_CALLED + FTPChipCertificateRequestDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.CERTIFICATE_UPLOAD_API_IS_CALLED + FTPChipCertificateRequestDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<PartnerCertificateResponseDto> response = new ResponseWrapper<>();
		if(partnerCertRequestDto.getRequest().getIsItForRegistrationDevice()) {
			response.setResponse(regFtpChipDetailService.uploadPartnerCertificate(partnerCertRequestDto.getRequest()));
		}else {
		response.setResponse(ftpChipDetaillService.uploadPartnerCertificate(partnerCertRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(AuthDeviceConstant.SUCCESSFUL_UPLOAD , FTPChipDetailStatusDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_UPLOAD , FTPChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007");
		return response;
	}
	
    /**
	 * To Download Partner Certificate.
	 * 
	 * @param certDownloadRequestDto {@link PartnerCertDownloadRequestDto} request
	 * @return {@link PartnerCertDownloadResponeDto} encrypted Data
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
	 */
	@PreAuthorize("hasAnyRole('FTM_PROVIDER')")
	@RequestMapping(value = "/getPartnerCertificate/{ftpChipDetailId}", method = RequestMethod.GET)
	public ResponseWrapper<PartnerCertDownloadResponeDto> getPartnerCertificate(
			@ApiParam("To download re-signed ftp chip certificate.")  @PathVariable("ftpChipDetailId") @NotNull String ftpChipDetailId) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {		
		ResponseWrapper<PartnerCertDownloadResponeDto> response = new ResponseWrapper<>();
		FTPChipCertDownloadRequestDto requestDto = new FTPChipCertDownloadRequestDto();
		requestDto.setFtpChipDetailId(ftpChipDetailId);
		auditUtil.auditRequest(
				AuthDeviceConstant.GET_CERTIFICATE_API_IS_CALLED + FTPChipCertificateRequestDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.GET_CERTIFICATE_API_IS_CALLED + FTPChipCertificateRequestDto.class.getCanonicalName(),
				"AUT-006");

		if(ftpChipDetaillService.getFtpChipDeatils(ftpChipDetailId) != null) {
			response.setResponse(ftpChipDetaillService.getPartnerCertificate(requestDto));
		}else {
			response.setResponse(regFtpChipDetailService.getPartnerCertificate(requestDto));
		}
		auditUtil.auditRequest(
				String.format(AuthDeviceConstant.SUCCESSFUL_DOWNLOAD , FTPChipDetailStatusDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_DOWNLOAD , FTPChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007");
		return response;
    }
}
