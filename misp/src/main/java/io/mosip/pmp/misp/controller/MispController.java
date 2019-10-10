package io.mosip.pmp.misp.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pmp.misp.dto.MISPCreateRequestDto;
import io.mosip.pmp.misp.dto.MISPCreateResponseDto;
import io.mosip.pmp.misp.dto.MISPGroupResponseDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyRequestDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyResponseDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.RequestWrapper;
import io.mosip.pmp.misp.dto.ResponseWrapper;
import io.mosip.pmp.misp.entity.MISPEntity;
import io.mosip.pmp.misp.entity.MISPLicenseEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.misp.service.MISPManagementService;
import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/pmp")
@Api(tags = { "Partner Management : MOSIP Infra Provider controller" })
public class MispController {

	@Autowired
	private MISPManagementService mispManagementService;
	
	@PostMapping(value = "/misps")
	public ResponseWrapper<MISPCreateResponseDto> registerMISP(@RequestBody @Valid RequestWrapper<MISPCreateRequestDto> mispCreateRequestDto)
			throws Exception{		

		ResponseWrapper<MISPCreateResponseDto> response = mispManagementService.createMISP(mispCreateRequestDto.getRequest());
		
		response.setId(mispCreateRequestDto.getId());
		response.setVersion(mispCreateRequestDto.getVersion());
		
		return response;
	}
	
	@PostMapping(value = "/misps/{mispId}")
	public ResponseWrapper<MISPUpdateResponseDto> updateMISP(@RequestBody @Valid RequestWrapper<MISPUpdateRequestDto> mispUpdateRequestDto, 
			@PathVariable String mispId) throws Exception
	{
		MISPUpdateRequestDto updateRequest = mispUpdateRequestDto.getRequest(); 
		updateRequest.setMispID(mispId);
		
		ResponseWrapper<MISPUpdateResponseDto>  response = mispManagementService.update(updateRequest);

		response.setId(mispUpdateRequestDto.getId());
		response.setVersion(mispUpdateRequestDto.getVersion());
		
		return response;
	}
		
	@PostMapping(value = "/misps/{mispId}/licenseKey")
	public ResponseWrapper<MISPValidatelKeyResponseDto> validateMISPlKey(@RequestBody RequestWrapper<MISPValidatelKeyRequestDto> misplKeyStatusUpdateRequestDto, 
			@PathVariable String mispId) throws Exception
	{
		ResponseWrapper<MISPValidatelKeyResponseDto> response = new ResponseWrapper<>();
		
		MISPValidatelKeyResponseDto message = new MISPValidatelKeyResponseDto();
		
		MISPLicenseEntity mispLicense = mispManagementService.getMispLiecense(new MISPlKeyUniqueKeyEntity(mispId,misplKeyStatusUpdateRequestDto.getRequest().getMispLicenseKey()));
		
		message.setMessage("MISP " + mispLicense.getMisp_id() + " license key " + mispLicense.getLicense_key() + "  is " 
		+ (mispLicense.getIsActive() == true ? "valid" : "not valid"));
		
		response.setResponse(message);
		response.setId(misplKeyStatusUpdateRequestDto.getId());
		response.setVersion(misplKeyStatusUpdateRequestDto.getVersion());
		
		return response;
	}
		
	@PutMapping(value="/misps/{mispId}")
	public ResponseWrapper<MISPStatusUpdateResponseDto> updateMISPStatus(@RequestBody RequestWrapper<MISPStatusUpdateRequestDto> mispStatusUpdateRequestDto,
			@PathVariable String mispId) throws Exception
	{
		MISPStatusUpdateRequestDto requestDto = mispStatusUpdateRequestDto.getRequest();
		
		requestDto.setMispId(mispId);
		ResponseWrapper<MISPStatusUpdateResponseDto> response = mispManagementService.updateMISPStatus(requestDto);
		
		response.setId(mispStatusUpdateRequestDto.getId());
		response.setVersion(mispStatusUpdateRequestDto.getVersion());
		
		return response;
	}
	
	@PutMapping(value = "/misps/{mispId}/licenseKey")
	public ResponseWrapper<MISPlKeyStatusUpdateResponseDto> updateMISPlKeyStatus(@RequestBody @Valid RequestWrapper<MISPlKeyStatusUpdateRequestDto> misplKeyStatusUpdateRequestDto,
			@PathVariable String mispId)
	{
		MISPlKeyStatusUpdateRequestDto request = misplKeyStatusUpdateRequestDto.getRequest();
		
		request.setMispId(mispId);
		
		ResponseWrapper<MISPlKeyStatusUpdateResponseDto> response = mispManagementService.updateMisplkeyStatus(request);
		
		response.setId(misplKeyStatusUpdateRequestDto.getId());
		response.setVersion(misplKeyStatusUpdateRequestDto.getVersion());

		return response;
	}
	
	@GetMapping(value = "/misps")
	public ResponseWrapper<MISPGroupResponseDto> getAllMISPDetails()
	{
		ResponseWrapper<MISPGroupResponseDto> response = new ResponseWrapper<>();
		MISPGroupResponseDto responseDto = new MISPGroupResponseDto();		
		List<MISPEntity> mispDetails =  new ArrayList<MISPEntity>();
		
		mispDetails = mispManagementService.getAllMISPS();
		
		responseDto.setMispList(mispDetails);		
		response.setResponse(responseDto);
		
		return response;
	}
	
	@GetMapping(value = "/misps/{mispId}")
	public ResponseWrapper<MISPEntity> getMISPDeatils(@PathVariable String mispId)
	{
		ResponseWrapper<MISPEntity> response = new ResponseWrapper<>();
		MISPEntity mispDetails =  new MISPEntity();
		
		mispDetails = mispManagementService.getMISPDeatils(mispId);
		
		response.setResponse(mispDetails);
		
		return response;
	}
	
//	@GetMapping
//	public List<MISPEntity> getOrgWiseMISPDeatils(@PathVariable String orgName )
//	{
//		return Collections.emptyList();
//	}
//	
//	@GetMapping
//	public List<MISPEntity> downloadMISPlKey(@PathVariable String id )
//	{
//		return Collections.emptyList();
//	}

}
