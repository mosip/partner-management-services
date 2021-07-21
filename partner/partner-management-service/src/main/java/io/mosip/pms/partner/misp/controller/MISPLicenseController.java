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

import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseUpdateRequestDto;
import io.mosip.pms.partner.misp.service.InfraServiceProviderService;
import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/misps")
@Api(tags = { "MISP License Detail" })
public class MISPLicenseController {

	@Autowired
	InfraServiceProviderService infraProviderService;
	
	//@PreAuthorize("hasAnyRole('MISP_PARTNER','MISP','PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostmisps())")
	@PostMapping
	public ResponseWrapper<MISPLicenseResponseDto> generateLicense(@RequestBody @Valid RequestWrapper<MISPLicenseRequestDto> request){	
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<MISPLicenseResponseDto>();
		response.setResponse(infraProviderService.approveInfraProvider(request.getRequest().getProviderId()));
		return response;
	}
	
	//@PreAuthorize("hasAnyRole('MISP_PARTNER','MISP','PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutmisps())")
	@PutMapping
	public ResponseWrapper<MISPLicenseResponseDto> updateLicenseDetails(@RequestBody @Valid RequestWrapper<MISPLicenseUpdateRequestDto> request){
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<MISPLicenseResponseDto>();
		response.setResponse(infraProviderService.updateInfraProvider(request.getRequest().getProviderId(), request.getRequest().getLicenseKey(),
				request.getRequest().getLicenseKeyStatus()));
		return response;
	}
	
	//@PreAuthorize("hasAnyRole('MISP_PARTNER','MISP','PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetmisps())")
	@GetMapping
	public ResponseWrapper<List<MISPLicenseEntity>> getLicenseDetails(){
		ResponseWrapper<List<MISPLicenseEntity>> response = new ResponseWrapper<>();
		response.setResponse(infraProviderService.getInfraProvider());
		return response;
	}
	
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetmispsmispidlicensekey())")
	@GetMapping(value = "/{mispId}/licenseKey")
	public ResponseWrapper<MISPLicenseResponseDto> regenarteLicenseKey(@PathVariable @Valid String mispId){
		ResponseWrapper<MISPLicenseResponseDto> response = new ResponseWrapper<>();
		response.setResponse(infraProviderService.regenerateKey(mispId));
		return response;
	}
}
