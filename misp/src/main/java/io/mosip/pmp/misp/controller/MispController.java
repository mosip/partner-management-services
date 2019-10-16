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
import io.mosip.pmp.misp.dto.MISPLiceneseDto;
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
import io.mosip.pmp.misp.entity.MISPLicenseReadEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.misp.service.MISPManagementService;
import io.swagger.annotations.Api;

/**
 * This is the MOSIP infra provider controller. This defines all the necessary operations
 * required for MISP. 
 * MISP Controller is having following operations.
 * 1. Register OR Define MISP {@link #registerMISP(RequestWrapper)}
 * 2. Updating the existing MISP {@link #updateMISP(RequestWrapper, String)}
 * 3. Validating the MISP license {@link #validateMISPlKey(RequestWrapper, String)}
 * 4. Updating the MISP status(Active OR De Active) {@link #updateMISPStatus(RequestWrapper, String)}
 * 5. Updating the MISP license key status(Active OR De Active) {@link #updateMISPlKeyStatus(RequestWrapper, String)}
 * 6. Getting the details of all the MISPS {@link #getAllMISPDetails()}
 * 7. Getting the details of specific MISP {@link #getMISPDeatils(String)}
 * 8. Getting all MISPs across the organization(domain)
 * 9. Downloading the MISP licnese key
 * 
  * @author Nagarjuna Kuchi
  * @version 1.0
  * @since 2019-Oct-11
 */

@RestController
@RequestMapping(value = "/pmp")
@Api(tags = { "Partner Management : MOSIP Infra Provider controller" })
public class MispController {

	@Autowired
	private MISPManagementService mispManagementService;
	
	/**
	 * MOSIP Admin would be able to create MISP using this API. At the time of creation of MISP, MISP ID and 
	 * MISP License Key are generated,mapped and shared back in response. Post successful MISP creation, 
	 * by default MISP is set to active status, MISP License key is to active status. MISP License key is configurable
	 * and set to expire in 3 months, 6 months OR any configurable period. Kernel ID generator API would be used to generate unique id.
	 * 
	 * @param mispCreateRequestDto  this class contains all the parameters that input should contain
	 * @return mispCreateResponseDto this class conatins all the parameters that response should have
	 * @throws Exception unhandle exceptions 
	 */
	
	@PostMapping(value = "/misps")
	public ResponseWrapper<MISPCreateResponseDto> registerMISP(@RequestBody @Valid RequestWrapper<MISPCreateRequestDto> mispCreateRequestDto)
			throws Exception{		

		ResponseWrapper<MISPCreateResponseDto> response = mispManagementService.createMISP(mispCreateRequestDto.getRequest());
		
		response.setId(mispCreateRequestDto.getId());
		response.setVersion(mispCreateRequestDto.getVersion());
		
		return response;
	}
	
	/**
	 * This API would be used to update MISP for given mispID.
	 * 
	 * @param mispUpdateRequestDto this is request body
	 * @param mispId this is mispid. This value coming as path variable.
	 * @return mispUpdateResponseDto this is the response 
	 * @throws Exception if any exceptions
	 */
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
		
	/**
	 * This API would be used for validating MISPs license key-
		1. Validate license key pattern.
		2. Validate license key is associated with the requested MISP id.
		3. Validate license key is Active or not.
	 * @param misplKeyStatusUpdateRequestDto this is the request body
	 * @param mispId This value coming as path variable.
	 * @return mispValidatelKeyResponseDto this class contains all the reponse fields.
	 * @throws Exception if any exception
	 * 	 
	 */	
	@PostMapping(value = "/misps/{mispId}/licenseKey")
	public ResponseWrapper<MISPValidatelKeyResponseDto> validateMISPlKey(@RequestBody RequestWrapper<MISPValidatelKeyRequestDto> misplKeyStatusUpdateRequestDto, 
			@PathVariable String mispId) throws Exception
	{
		
		ResponseWrapper<MISPValidatelKeyResponseDto> response = new ResponseWrapper<>();
		
		MISPValidatelKeyResponseDto message = new MISPValidatelKeyResponseDto();
		
		MISPLicenseReadEntity mispLicense = mispManagementService.getMispLicense(new MISPlKeyUniqueKeyEntity(mispId,misplKeyStatusUpdateRequestDto.getRequest().getMispLicenseKey()));
		
		message.setMessage("MISP " + mispLicense.getMispUniqueEntity().getMisp_id() + " license key " + mispLicense.getMispUniqueEntity().getLicense_key() + "  is " 
		+ (mispLicense.isActive == true ? "valid" : "not valid"));
		
		response.setResponse(message);
		response.setId(misplKeyStatusUpdateRequestDto.getId());
		response.setVersion(misplKeyStatusUpdateRequestDto.getVersion());
		
		return response;
	}
		
	/**
	 *  This API would be used to update MISP status for given MISP ID.
	 *  
	 * @param mispStatusUpdateRequestDto this class contains all the fields required for input request. 
	 * @param mispId this value coming as path variable.
	 * @return mispStatusUpdateResponseDto this class contains all the reponse fields.
	 * @throws Exception if any exceptions
	 */
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
	
	/**
	 * This API would be used to activate/deactivate MISPs License Key for the MSIP ID.
	 * 
	 * @param misplKeyStatusUpdateRequestDto this class contains all the fields required for input request.
	 * @param mispId this value coming as path variable.
	 * @return misplKeyStatusUpdateResponseDto this class contains all the reponse fields.
	 */
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
	
	
	/**
	 * This API would be used to retrieve all MISPs details.
	 * 
	 * @return MISPGroupResponseDto  list of misps
	 */
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
	
	/**
	 * This API would be used to retrieve the MISPs details based on given misp id.
	 * 
	 * @param mispId this value coming as path variable.
	 * @return mispEntity specific misp details.
	 */
	@GetMapping(value = "/misps/{mispId}")
	public ResponseWrapper<MISPEntity> getMISPDeatils(@PathVariable String mispId)
	{
		ResponseWrapper<MISPEntity> response = new ResponseWrapper<>();
		MISPEntity mispDetails =  new MISPEntity();
		
		mispDetails = mispManagementService.getMISPDeatils(mispId);
		
		response.setResponse(mispDetails);
		
		return response;
	}
	
	@GetMapping(value ="/misps/misp/{orgName}")
	public List<MISPEntity> getOrgWiseMISPDeatils(@PathVariable String orgName )
	{
		return mispManagementService.getMispsByOrganization(orgName);
	}
	
	@GetMapping(value = "/misps/{mispId}/licenseKey")
	public ResponseWrapper<MISPLiceneseDto> downloadMispLicenseKey(@RequestBody @Valid RequestWrapper<MISPlKeyStatusUpdateRequestDto> misplKeyStatusUpdateRequestDto,
			@PathVariable String mispId)
	{
		MISPlKeyStatusUpdateRequestDto request = misplKeyStatusUpdateRequestDto.getRequest();
		
		request.setMispId(mispId);
		
		ResponseWrapper<MISPLiceneseDto> response = mispManagementService.retriveMISPLicense(mispId);
		
		response.setId(misplKeyStatusUpdateRequestDto.getId());
		response.setVersion(misplKeyStatusUpdateRequestDto.getVersion());

		return response;
	}

}
