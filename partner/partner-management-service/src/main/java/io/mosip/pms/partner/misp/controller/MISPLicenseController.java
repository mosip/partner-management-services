package io.mosip.pms.partner.misp.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import io.mosip.pms.partner.misp.service.InfraServiceProviderService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/misps")
@Api(tags = { "MISP License Detail" })
public class MISPLicenseController {

	@Autowired
	InfraServiceProviderService infraProviderService;
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostmisplicense())")
	@PostMapping
	@Operation(summary = "Service to generate license for misp", description = "Service to generate license for misp")
	public ResponseWrapper<MISPLicenseResponseDto> generateLicense(@RequestBody @Valid RequestWrapper<MISPLicenseRequestDto> request){	
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<MISPLicenseResponseDto>();
		response.setResponse(infraProviderService.approveInfraProvider(request.getRequest().getProviderId()));
		return response;
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutmisplicense())")
	@PutMapping
	@Operation(summary = "Service to update license details of misp", description = "Service to update license details of misp")
	public ResponseWrapper<MISPLicenseResponseDto> updateLicenseDetails(@RequestBody @Valid RequestWrapper<MISPLicenseUpdateRequestDto> request){
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<MISPLicenseResponseDto>();
		response.setResponse(infraProviderService.updateInfraProvider(request.getRequest().getProviderId(), request.getRequest().getLicenseKey(),
				request.getRequest().getLicenseKeyStatus()));
		return response;
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetmisplicense())")
	@GetMapping
	@Operation(summary = "Service to get license details of misp", description = "Service to get license details of misp")
	public ResponseWrapper<List<MISPLicenseEntity>> getLicenseDetails(){
		ResponseWrapper<List<MISPLicenseEntity>> response = new ResponseWrapper<>();
		response.setResponse(infraProviderService.getInfraProvider());
		return response;
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetmisplicensekey())")
	@GetMapping(value = "/{mispId}/licenseKey")
	@Operation(summary = "Service to get/regenarate license details of misp", description = "Service to get/regenarate license details of misp")
	public ResponseWrapper<MISPLicenseResponseDto> regenarteLicenseKey(@PathVariable @Valid String mispId){
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<>();
		response.setResponse(infraProviderService.regenerateKey(mispId));
		return response;
	}	
	
	@PostMapping("/filtervalues")	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostmispfiltervalues())")
	@Operation(summary = "Service to filter misp details", description = "Service to filter misp details")
	public ResponseWrapper<FilterResponseCodeDto> filterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(infraProviderService.filterValues(request.getRequest()));
		return responseWrapper;
	}	
	
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostmispsearch())")
	@Operation(summary = "Service to search misp details", description = "Service to search misp details")
	public ResponseWrapper<PageResponseDto<MISPLicenseEntity>> search(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<MISPLicenseEntity>> responseWrapper = new ResponseWrapper<>();		
		responseWrapper.setResponse(infraProviderService.search(request.getRequest()));
		return responseWrapper;
	}
}
