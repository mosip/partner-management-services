package io.mosip.pmp.policy.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pmp.policy.dto.PolicyCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyCreateResponseDto;
import io.mosip.pmp.policy.dto.PolicyGroupCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyGroupCreateResponseDto;
import io.mosip.pmp.policy.dto.PolicyGroupUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyResponseDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyWithAuthPolicyDto;
import io.mosip.pmp.policy.dto.RequestWrapper;
import io.mosip.pmp.policy.dto.ResponseWrapper;
import io.mosip.pmp.policy.errorMessages.PolicyServiceLogger;
import io.mosip.pmp.policy.service.PolicyManagementService;
import io.swagger.annotations.Api;

/** 
 * <p> This is policy controller. This controller defines all the operations required </p>
 * <p> to manage policy group.</p>
 * <p> This controller provides following operations/functions.</p>
 *     1. Create policy group.</br>
 *     2. Create auth policies for policy group.</br> 
 *     3. Update policy group.</br>
 *     4. Update policy group status.</br>
 *     5. Read/Get all policy groups.</br>
 *     6. Read/Get specific policy group.</br>
 *     7. Read/Get policy details of a partner api key.</br>
 *       
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 */

@RestController
@Api(tags = { "Partner Management : Policy Management Controller " })
public class PolicyManagementController {

	@Autowired
	private PolicyManagementService policyManagementService;

	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PostMapping(value = "/policies/policyGroup")
	public ResponseWrapper<PolicyGroupCreateResponseDto> definePolicyGroup(
			@RequestBody @Valid RequestWrapper<PolicyGroupCreateRequestDto> createRequest){	
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		PolicyGroupCreateResponseDto responseDto = policyManagementService.createPolicyGroup(createRequest.getRequest());
		ResponseWrapper<PolicyGroupCreateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(responseDto);
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());		
		return response;		
	}
	
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PutMapping(value = "/policies/policyGroup/{policyGroupId}")
	public ResponseWrapper<PolicyGroupCreateResponseDto> updatePolicyGroup(
			@PathVariable String policyGroupId,@RequestBody @Valid RequestWrapper<PolicyGroupUpdateRequestDto> createRequest){	
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		PolicyGroupCreateResponseDto responseDto = policyManagementService.updatePolicyGroup(createRequest.getRequest(), policyGroupId);
		ResponseWrapper<PolicyGroupCreateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(responseDto);
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());		
		return response;		
	}
	
	
	/**
	 * <p> This API would be used to create new Policy for policy group.</p>
	 * 
	 * @param createRequest {@link PolicyCreateRequestDto} this contains all the required parameters for creating the policy.
	 * @return response {@link PolicyCreateResponseDto} this contains all the response parameters for created policy.
	 * @throws Exception  
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PostMapping(value = "/policies")	
	public ResponseWrapper<PolicyCreateResponseDto> definePolicy(
			@RequestBody @Valid RequestWrapper<PolicyCreateRequestDto> createRequest) throws Exception {
		
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<PolicyCreateResponseDto>();
		PolicyCreateResponseDto responseDto = policyManagementService.
				createPolicies(createRequest.getRequest());		
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());
		response.setResponse(responseDto);		
		
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}

	/**
	 * 
	 * @param policyGroupId
	 * @param policyId
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PostMapping(value = "/policies/publishPolicy/policyGroupId/{policyGroupId}/policyId/{policyId}")
	public ResponseWrapper<PolicyResponseDto> publishPolicy(@PathVariable @Valid String policyGroupId, @PathVariable @Valid String policyId) throws JsonParseException, JsonMappingException, IOException {
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<PolicyResponseDto>();
		PolicyResponseDto responseDto = policyManagementService.publishPolicy(policyGroupId, policyId);
		response.setResponse(responseDto);
		return response;
	}
	
	/**
	 * <p> This API would be used to update existing policy for a policy group.</p>
	 *  
	 * @param updateRequestDto {@link PolicyUpdateRequestDto } Encapsulated all the required parameters required for policy update.
	 * @param policyID policy id.7
	 * @return response {@link PolicyUpdateResponseDto} contains all response details.
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PutMapping(value = "/policies/{policyID}")
	public ResponseWrapper<PolicyCreateResponseDto> updatePolicyDetails(
			@RequestBody RequestWrapper<PolicyUpdateRequestDto> updateRequestDto, @PathVariable String policyID)
			throws Exception {
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<PolicyCreateResponseDto>();
		PolicyCreateResponseDto responseDto = policyManagementService.updatePolicies(updateRequestDto.getRequest(),policyID);
		response.setResponse(responseDto);
		response.setId(updateRequestDto.getId());
		response.setVersion(updateRequestDto.getVersion());
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}

	/**
	 * <p> This API would be used to update the status (activate/deactivate) for the given policy id.</p>
	 * 
	 * @param requestDto {@link PolicyStatusUpdateRequestDto } Defines all the required parameters for policy status update.	 *  
	 * @param policyID policy id.
	 * @return response {@link PolicyStatusUpdateResponseDto} contains all response details.
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PatchMapping(value = "/policies/policyGroupId/{policyGroupId}/policyId/{policyID}")
	public ResponseWrapper<PolicyStatusUpdateResponseDto> updatePolicyStatus(@RequestBody RequestWrapper<PolicyStatusUpdateRequestDto> requestDto,
			@PathVariable String policyGroupId, @PathVariable String policyID) throws Exception {		
		PolicyStatusUpdateRequestDto statusUpdateRequest = requestDto.getRequest();	
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		
		ResponseWrapper<PolicyStatusUpdateResponseDto> response =  policyManagementService.
				updatePolicyStatus(statusUpdateRequest,policyGroupId,policyID);		
		response.setId(requestDto.getId());
		response.setVersion(requestDto.getVersion());
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}

	/**
	 * <p> This API would be used to get details for the policies in the policy group he belongs to.</p>
	 * 
	 * @return response {@link PolicyWithAuthPolicyDto}  policy group associated with his auth policies.
	 * @throws ParseException  
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@GetMapping(value = "/policies")
	public ResponseWrapper<List<PolicyResponseDto>> getPolicies() throws FileNotFoundException, IOException, ParseException{
		ResponseWrapper<List<PolicyResponseDto>> response = new ResponseWrapper<>();
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");		
		response.setResponse(policyManagementService.findAllPolicies());
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}

	/**
	 * <p> This API would be used to retrieve existing policy for a policy group based on the policy id.</p>
	 * 
	 * @param policyID policy id.
	 * @return response  {@link PolicyWithAuthPolicyDto}  policy group associated with his auth policies.
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@GetMapping(value = "/policies/policyId/{policyID}")
	public ResponseWrapper<PolicyResponseDto> getPolicy(@PathVariable String policyID) throws Exception {
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<>();
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		PolicyResponseDto responseDto = policyManagementService.findPolicy(policyID);
		response.setResponse(responseDto);
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}
	
	/**
	 * <p>This API would be used to retrieve the partner policy details for given PartnerAPIKey.</p>
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@GetMapping(value = "/policies/partnerApiKey/{partnerApiKey}")
	public ResponseWrapper<PolicyResponseDto> getPolicyAgainstApiKey(@PathVariable String partnerApiKey) throws FileNotFoundException, IOException, ParseException{
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<>();
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		PolicyResponseDto policyGroup = policyManagementService.getAuthPolicyWithApiKey(partnerApiKey);
		response.setResponse(policyGroup);
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;		
	}
	
	/**
	 * 
	 * @param partnerId
	 * @param policyId
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager','CREDENTIAL_ISSUANCE')")
	@GetMapping(value="/policies/partnerId/{partnerId}/policyId/{policyId}")
	public ResponseWrapper<PolicyResponseDto> getPartnersPolicy(@PathVariable String partnerId, @PathVariable String policyId) throws JsonParseException, JsonMappingException, IOException{
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<>();
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		PolicyResponseDto policyGroup = policyManagementService.getPartnerMappedPolicy(partnerId, policyId);
		response.setResponse(policyGroup);
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}
	
	/**
	 * 
	 * @param policyGroupId
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@GetMapping(value = "/policies/policyGroupId/{policyGroupId}")
	public ResponseWrapper<PolicyWithAuthPolicyDto> getPolicyGroup(@PathVariable String policyGroupId) throws JsonParseException, JsonMappingException, IOException{
		ResponseWrapper<PolicyWithAuthPolicyDto> response = new ResponseWrapper<>();
		response.setResponse(policyManagementService.getPolicyGroupPolicy(policyGroupId));
		return response;
	}
	
	/**
	 * 
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@GetMapping(value ="/policies/policyGroups")
	public ResponseWrapper<List<PolicyWithAuthPolicyDto>> getPolicyGroup() throws JsonParseException, JsonMappingException, IOException {
		ResponseWrapper<List<PolicyWithAuthPolicyDto>> response = new ResponseWrapper<>();
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");		
		response.setResponse(policyManagementService.getPolicyGroup());
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}
}
