package io.mosip.pmp.policy.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pmp.policy.dto.PoliciesDto;
import io.mosip.pmp.policy.dto.PolicyCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyCreateResponseDto;
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

	
	/**
	 * <p> This API would be used to create new Policy for policy group.</p>
	 * 
	 * @param createRequest {@link PolicyCreateRequestDto} this contains all the required parameters for creating the policy.
	 * @return response {@link PolicyCreateResponseDto} this contains all the response parameters for created policy.
	 * @throws Exception  
	 */
	
	@PostMapping(value = "/policies")	
	public ResponseWrapper<PolicyCreateResponseDto> definePolicy(
			@RequestBody @Valid RequestWrapper<PolicyCreateRequestDto> createRequest) throws Exception {
		
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		ResponseWrapper<PolicyCreateResponseDto> response = policyManagementService.
				createPolicyGroup(createRequest.getRequest());		
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());
		
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}

	/**
	 * <p> This API would be used to update existing policy for a policy group.</p>
	 *  
	 * @param updateRequestDto {@link PolicyUpdateRequestDto } Encapsulated all the required parameters required for policy update.
	 * @param policyID policy id.
	 * @return response {@link PolicyUpdateResponseDto} contains all response details.
	 * @throws Exception
	 */
	@PutMapping(value = "/policies/{policyID}")
	public ResponseWrapper<PolicyUpdateResponseDto> updatePolicyDetails(
			@RequestBody RequestWrapper<PolicyUpdateRequestDto> updateRequestDto, @PathVariable String policyID)
			throws Exception {
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		PolicyUpdateRequestDto updateRequest = updateRequestDto.getRequest();
		updateRequest.setId(policyID);
		
		ResponseWrapper<PolicyUpdateResponseDto> response = policyManagementService.update(updateRequest);
		
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
	@PatchMapping(value = "/policies/{policyID}")
	public ResponseWrapper<PolicyStatusUpdateResponseDto> updatePolicyStatus(@RequestBody RequestWrapper<PolicyStatusUpdateRequestDto> requestDto,
			@PathVariable String policyID) throws Exception {
		
		PolicyStatusUpdateRequestDto statusUpdateRequest = requestDto.getRequest();
		
		statusUpdateRequest.setId(policyID);		
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		
		ResponseWrapper<PolicyStatusUpdateResponseDto> response =  policyManagementService.
				updatePolicyStatus(statusUpdateRequest);		
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
	@GetMapping(value = "/policies")
	public ResponseWrapper<PoliciesDto> getPolicies() throws FileNotFoundException, IOException, ParseException{
		ResponseWrapper<PoliciesDto> response = new ResponseWrapper<>();
		PoliciesDto dto = new PoliciesDto();
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		List<PolicyWithAuthPolicyDto> policies = policyManagementService.findAllPolicies();
		dto.setPolicies(policies);
		response.setResponse(dto);
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
	@GetMapping(value = "/policies/policyId/{policyID}")
	public ResponseWrapper<PolicyWithAuthPolicyDto> getPolicy(@PathVariable String policyID) throws Exception {
		ResponseWrapper<PolicyWithAuthPolicyDto> response = new ResponseWrapper<>();
		PolicyServiceLogger.info("Calling PolicyManagementService from PolicyManageController.");
		PolicyWithAuthPolicyDto policyGroup = policyManagementService.findPolicy(policyID);
		response.setResponse(policyGroup);
		PolicyServiceLogger.info("Returning response from MispController.");
		return response;
	}
	
	/**
	 * <p>This API would be used to retrieve the partner policy details for given PartnerAPIKey.</p>
	 */

}
