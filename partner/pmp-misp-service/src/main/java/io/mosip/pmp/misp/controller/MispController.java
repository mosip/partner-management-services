package io.mosip.pmp.misp.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pmp.common.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.misp.dto.MISPCreateRequestDto;
import io.mosip.pmp.misp.dto.MISPCreateResponseDto;
import io.mosip.pmp.misp.dto.MISPDetailsDto;
import io.mosip.pmp.misp.dto.MISPLiceneseDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponse;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyRequestDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyResponseDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.RequestWrapper;
import io.mosip.pmp.misp.dto.ResponseWrapper;
import io.mosip.pmp.misp.service.MISPManagementService;
import io.mosip.pmp.misp.utils.AuditUtil;
import io.mosip.pmp.misp.utils.PartnerManageEnum;
import io.swagger.annotations.Api;

/**
 * <p>This is the MOSIP infra provider controller. This defines all the necessary operations</p>
 * <p>required for MISP</p>.
 * MISP Controller is having following operations.<br/>
 * 1. Register OR Define MISP {@link #registerMISP(RequestWrapper)} <br/>
 * 2. Updating the existing MISP {@link #updateMISP(RequestWrapper, String)} <br/>
 * 3. Validating the MISP license {@link #validateMISPlKey(RequestWrapper, String)} <br/>
 * 4. Updating the MISP status(Active OR De Active) {@link #updateMISPStatus(RequestWrapper, String)} <br/>
 * 5. Updating the MISP license key status(Active OR De Active) {@link #updateMISPlKeyStatus(RequestWrapper, String)} <br/>
 * 6. Getting the details of all the MISPS {@link #getAllMISPDetails()} <br/>
 * 7. Getting the details of specific MISP {@link #getMISPDeatils(String)} <br/>
 * 8. Getting all MISPs across the organization(domain) <br/>
 * 9. Downloading the MISP license key <br/>
 * 10.Update misp request status <br/>
 * 
  * @author Nagarjuna Kuchi
  * @version 1.0
  * @since 2019-Oct-11
 */


@RestController
@Api(tags = { "Partner Management : MOSIP Infra Provider controller" })
public class MispController {
	
	private static final Logger logger = LoggerFactory.getLogger(MispController.class);
	
	@Autowired
	private MISPManagementService mispManagementService;
	
	@Autowired
	private AuditUtil audit;
	
	/**
	 * <p>MOSIP Admin would be able to create MISP using this API. At the time of creation of MISP, MISP ID and</p> 
	 * <p>MISP License Key are generated,mapped and shared back in response. Post successful MISP creation,</p> 
	 * <p>by default MISP is set to active status, MISP License key is to active status. MISP License key is configurable</p>
	 * <p>and set to expire in 3 months, 6 months OR any configurable period. Kernel ID generator API would be used <p>
	 * <p>to generate unique id.</p>
	 * 
	 * @param mispCreateRequestDto  this class contains all the parameters that input should contain
	 * @return mispCreateResponseDto this class conatins all the parameters that response should have
	 * @throws Exception unhandle exceptions 
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@PostMapping(value = "/misps")
	public ResponseWrapper<MISPCreateResponseDto> registerMISP(@RequestBody @Valid RequestWrapper<MISPCreateRequestDto> mispCreateRequestDto)
			throws Exception{		

        logger.info("Calling MISPManagementService from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.CREATE_MISP);
		ResponseWrapper<MISPCreateResponseDto> response = mispManagementService.createMISP(mispCreateRequestDto.getRequest());		
		response.setId(mispCreateRequestDto.getId());
		response.setVersion(mispCreateRequestDto.getVersion());		
		logger.info("Returning misp registration response from MispController.");		
		audit.setAuditRequestDto(PartnerManageEnum.CREATE_MISP_SUCCESS);
		return response;
	}
	
	/**
	 * 
	 * @return
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@PatchMapping(value="/misps/{mispId}/status")
	public ResponseWrapper<MISPStatusUpdateResponse> processRequest(@RequestBody @Valid RequestWrapper<MISPStatusUpdateRequestDto> mispUpdateRequestDto,
			@PathVariable String mispId){
		logger.info("Calling MISPManagementService from MispController.");		
		MISPStatusUpdateRequestDto updateRequest = mispUpdateRequestDto.getRequest(); 
		updateRequest.setMispId(mispId);
		audit.setAuditRequestDto(PartnerManageEnum.getPartnerManageEnumWithValue(PartnerManageEnum.PROCESS_MISP,mispId));
		ResponseWrapper<MISPStatusUpdateResponse>  response = mispManagementService.processRequest(updateRequest);
		response.setId(mispUpdateRequestDto.getId());
		response.setVersion(mispUpdateRequestDto.getVersion());
		logger.info("Returning misp update response from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.getPartnerManageEnumWithValue(PartnerManageEnum.PROCESS_MISP_SUCCESS,mispId));
		return response;		
	}
	
	
	/**
	 * This API would be used to update MISP for given mispID.
	 * </br>
	 * @param mispUpdateRequestDto this is request body
	 * @param mispId this is mispid. This value coming as path variable.
	 * @return mispUpdateResponseDto this is the response 
	 * @throws Exception if any exceptions
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@PutMapping(value = "/misps/{mispId}")
	public ResponseWrapper<MISPUpdateResponseDto> updateMISP(@RequestBody @Valid RequestWrapper<MISPUpdateRequestDto> mispUpdateRequestDto, 
			@PathVariable String mispId) throws Exception {
		logger.info("Calling MISPManagementService from MispController.");		
		MISPUpdateRequestDto updateRequest = mispUpdateRequestDto.getRequest(); 
		updateRequest.setMispID(mispId);		
		audit.setAuditRequestDto(PartnerManageEnum.UPDATE_MISP);
		ResponseWrapper<MISPUpdateResponseDto> response = mispManagementService.update(updateRequest);
		response.setId(mispUpdateRequestDto.getId());
		response.setVersion(mispUpdateRequestDto.getVersion());
        logger.info("Returning misp update response from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.UPDATE_MISP_SUCCESS);
		return response;
	}
		
	/**
	 * This API would be used for validating MISPs license key.</br>
		1. Validate license key pattern.</br>
		2. Validate license key is associated with the requested MISP id.</br>
		3. Validate license key is Active or not.</br>
	 * @param misplKeyStatusUpdateRequestDto this is the request body
	 * @param mispId This value coming as path variable.
	 * @return mispValidatelKeyResponseDto this class contains all the response fields.
	 * @throws Exception if any exception
	 * 	 
	 */	
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@PatchMapping(value = "/misps/{mispId}/licenseKey")
	public ResponseWrapper<MISPValidatelKeyResponseDto> validateLicenseKey(@RequestBody RequestWrapper<MISPValidatelKeyRequestDto> misplKeyStatusUpdateRequestDto, 
			@PathVariable String mispId) throws Exception{		
		logger.info("Calling MISPManagementService from MispController.");
		ResponseWrapper<MISPValidatelKeyResponseDto> response = new ResponseWrapper<>();	
		audit.setAuditRequestDto(PartnerManageEnum.VALIDATE_LICENSE_KEY);
		MISPValidatelKeyResponseDto responseDto = mispManagementService.validateLicenseKey(new MISPlKeyUniqueKeyEntity(mispId,misplKeyStatusUpdateRequestDto.getRequest().
				getMispLicenseKey()));		
		response.setResponse(responseDto);
		response.setId(misplKeyStatusUpdateRequestDto.getId());		
		response.setVersion(misplKeyStatusUpdateRequestDto.getVersion());
		logger.info("Returning misp validate licensekey response from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.VALIDATE_LICENSE_KEY_SUCCESS);
		return response;
	}
		
	/**
	 *  This API would be used to update MISP status for given MISP ID.
	 *  </br>
	 * @param mispStatusUpdateRequestDto this class contains all the fields required for input request. 
	 * @param mispId this value coming as path variable.
	 * @return mispStatusUpdateResponseDto this class contains all the response fields.
	 * @throws Exception if any exceptions
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@PatchMapping(value="/misps/{mispId}")
	public ResponseWrapper<MISPStatusUpdateResponseDto> updateMISPStatus(@RequestBody RequestWrapper<MISPStatusUpdateRequestDto> mispStatusUpdateRequestDto,
			@PathVariable String mispId) throws Exception {
		logger.info("Calling MISPManagementService from MispController.");
		MISPStatusUpdateRequestDto requestDto = mispStatusUpdateRequestDto.getRequest();		
		requestDto.setMispId(mispId);
		audit.setAuditRequestDto(PartnerManageEnum.UPDATE_MISP_STATUS);
		ResponseWrapper<MISPStatusUpdateResponseDto> response = mispManagementService.updateMISPStatus(requestDto);		
		response.setId(mispStatusUpdateRequestDto.getId());
		response.setVersion(mispStatusUpdateRequestDto.getVersion());
		logger.info("Returning update misp status response from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.UPDATE_MISP_STATUS_SUCCESS);
		return response;
	}
	
	/**
	 * This API would be used to activate/deactivate MISPs License Key for the MSIP ID.
	 * </br>
	 * @param misplKeyStatusUpdateRequestDto this class contains all the fields required for input request.
	 * @param mispId this value coming as path variable.
	 * @return misplKeyStatusUpdateResponseDto this class contains all the response fields.
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@PutMapping(value = "/misps/{mispId}/licenseKey")
	public ResponseWrapper<MISPlKeyStatusUpdateResponseDto> updateMISPlKeyStatus(@RequestBody @Valid RequestWrapper<MISPlKeyStatusUpdateRequestDto> misplKeyStatusUpdateRequestDto,
			@PathVariable String mispId) {
		logger.info("Calling MISPManagementService from MispController.");
		MISPlKeyStatusUpdateRequestDto request = misplKeyStatusUpdateRequestDto.getRequest();	
		audit.setAuditRequestDto(PartnerManageEnum.UPDATE_MISP_LICENSE_KEY_STATUS);
		ResponseWrapper<MISPlKeyStatusUpdateResponseDto> response = mispManagementService.updateMisplkeyStatus(request,mispId);		
		response.setId(misplKeyStatusUpdateRequestDto.getId());
		response.setVersion(misplKeyStatusUpdateRequestDto.getVersion());
		logger.info("Returning update misp license key status update response from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.UPDATE_MISP_LICENSE_KEY_STATUS_SUCCESS);
		return response;
	}
	
	
	/**
	 * This API would be used to retrieve all MISPs details.
	 * </br>
	 * @return MISPGroupResponseDto  list of misps
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@GetMapping(value = "/misps")
	public ResponseWrapper<List<MISPDetailsDto>> getMisps(){
		ResponseWrapper<List<MISPDetailsDto>> response = new ResponseWrapper<>();
		logger.info("Calling MISPManagementService from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.GET_ALL_MISPS);
		response.setResponse(mispManagementService.getMisps());
		audit.setAuditRequestDto(PartnerManageEnum.GET_ALL_MISPS_SUCCESS);
		return response;
	}
	
	/**
	 * This API would be used to retrieve the MISPs details based on given misp id.
	 * </br>
	 * @param mispId this value coming as path variable.
	 * @return mispEntity specific misp details.
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@GetMapping(value = "/misps/mispId/{mispId}")
	public ResponseWrapper<MISPDetailsDto> getMisp(@PathVariable String mispId){
		logger.info("Calling MISPManagementService from MispController.");
		ResponseWrapper<MISPDetailsDto> response = new ResponseWrapper<>();
		MISPDetailsDto mispDetails =  new MISPDetailsDto();		
		audit.setAuditRequestDto(PartnerManageEnum.GET_MISP_BY_ID);
		mispDetails = mispManagementService.getMisp(mispId);		
		response.setResponse(mispDetails);		
		audit.setAuditRequestDto(PartnerManageEnum.GET_MISP_BY_ID_SUCCESS);
		return response;
	}
	
	/**
	 * This api would bring misp details with misp name
	 * </br>
	 * @param orgName
	 * @return list of misp's which are starts with given name.
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@GetMapping(value ="/misps/name/{orgName}")
	public ResponseWrapper<List<MISPDetailsDto>> getMispsByOrg(@PathVariable String orgName ){
		ResponseWrapper<List<MISPDetailsDto>> responseWrapper = new ResponseWrapper<>();
		logger.info("Calling MISPManagementService from MispController.");
		audit.setAuditRequestDto(PartnerManageEnum.GET_MISP_BY_ORGANIZATION_NAME);
		responseWrapper.setResponse(mispManagementService.getMispsByOrg(orgName));
		audit.setAuditRequestDto(PartnerManageEnum.GET_MISP_BY_ORGANIZATION_NAME_SUCCESS);
		return responseWrapper;
	}
	
	/**
	 * <p> This api would be used to download misp's license key.</p>
	 * <p> In case where license key got expired then user would be able to get a new license key. </p>
	 * <p> New license key generated would be mapped with given MISP ID . Older license keys would be </p>
	 * <p> updated with inactive status.</p>
	 * 
	 * @param misplKeyStatusUpdateRequestDto
	 * @param mispId
	 * @return misp license details.
	 */
	@Deprecated
	@PreAuthorize("hasAnyRole('MISP')")
	@GetMapping(value = "/misps/{mispId}/licenseKey")
	public ResponseWrapper<MISPLiceneseDto> downloadLicenseKey(@PathVariable @Valid String mispId){

		logger.info("Calling MISPManagementService from MispController.");	
		audit.setAuditRequestDto(PartnerManageEnum.DOWNLOAD_LICENSE_KEY);
		ResponseWrapper<MISPLiceneseDto> response = mispManagementService.retriveLicense(mispId);	
		audit.setAuditRequestDto(PartnerManageEnum.DOWNLOAD_LICENSE_KEY_SUCCESS);
		return response;
	}	
}