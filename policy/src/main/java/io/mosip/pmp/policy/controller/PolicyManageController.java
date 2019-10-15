package io.mosip.pmp.policy.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pmp.policy.dto.AuthPolicyCreateResponseDto;
import io.mosip.pmp.policy.dto.PoliciesDto;
import io.mosip.pmp.policy.dto.PolicyCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyCreateResponseDto;
import io.mosip.pmp.policy.dto.PolicyDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyUpdateResponseDto;
import io.mosip.pmp.policy.dto.RequestWrapper;
import io.mosip.pmp.policy.dto.ResponseWrapper;
import io.mosip.pmp.policy.service.PolicyManagementService;
import io.swagger.annotations.Api;

/**
 * @author Nagarjuna Kuchi
 *
 */

@RestController
@RequestMapping(value = "/pmp")
@Api(tags = { " Partner Management : Policy Management Controller " })

public class PolicyManageController {

	@Autowired
	private PolicyManagementService policyManagementService;

	
	/**
	 * @param createRequest
	 * @return
	 * @throws IOException
	 */
	
	@PostMapping(value = "/policies")	
	public ResponseWrapper<PolicyCreateResponseDto> definePolicy(
			@RequestBody @Valid RequestWrapper<PolicyCreateRequestDto> createRequest) throws Exception {
		
		ResponseWrapper<PolicyCreateResponseDto> response = policyManagementService.createPolicyGroup(createRequest.getRequest());
		
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());
		
		return response;
	}
	
	
	
	@PostMapping(value = "/policies/{policyID}/authPolicies")
	public ResponseWrapper<AuthPolicyCreateResponseDto> assignAuthPolicies(@RequestBody @Valid RequestWrapper<PolicyDto> policyDto,
			@PathVariable String policyID)
	throws Exception
	{
		PolicyDto policyRequestDto = policyDto.getRequest();
		policyRequestDto.setPolicyId(policyID);
	
		ResponseWrapper<AuthPolicyCreateResponseDto> response = policyManagementService.createAuthPolicies(policyRequestDto);
		
		response.setId(policyDto.getId());
		response.setVersion(policyDto.getVersion());
		
		return response;		
	}

	/**
	 * @param updateRequestDto
	 * @param policyID
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/policies/{policyID}")
	public ResponseWrapper<PolicyUpdateResponseDto> updatePolicyDetails(
			@RequestBody RequestWrapper<PolicyUpdateRequestDto> updateRequestDto, @PathVariable String policyID)
			throws Exception {
		PolicyUpdateRequestDto updateRequest = updateRequestDto.getRequest();
		updateRequest.setId(policyID);
		
		ResponseWrapper<PolicyUpdateResponseDto> response = policyManagementService.update(updateRequest);
		
		response.setId(updateRequestDto.getId());
		response.setVersion(updateRequestDto.getVersion());
		
		return response;
	}

	/**
	 * @param statusUpdateRequestDto
	 * @param policyID
	 * @return
	 * @throws Exception
	 */
	@PutMapping(value = "/policies/{policyID}")
	public ResponseWrapper<PolicyStatusUpdateResponseDto> updatePolicyStatus(
			@RequestBody RequestWrapper<PolicyStatusUpdateRequestDto> statusUpdateRequestDto,
			@PathVariable String policyID) throws Exception {
		
		PolicyStatusUpdateRequestDto statusUpdateRequest = statusUpdateRequestDto.getRequest();
		statusUpdateRequest.setId(policyID);
		
		ResponseWrapper<PolicyStatusUpdateResponseDto> response =  policyManagementService.updatePolicyStatus(statusUpdateRequest);
		
		response.setId(statusUpdateRequestDto.getId());
		response.setVersion(statusUpdateRequestDto.getVersion());

		return response;
	}

	/**
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@GetMapping(value = "/policies")
	public ResponseWrapper<PoliciesDto> getPolicyDetails() throws FileNotFoundException, IOException, ParseException {
		ResponseWrapper<PoliciesDto> response = new ResponseWrapper<>();
		
		List<PoliciesDto> policies = policyManagementService.getPolicyDetails("");
		response.setResponse(policies.get(0));
		
		//response.setId(statusUpdateRequestDto.getId());
		//response.setVersion(statusUpdateRequestDto.getVersion());
		
		return response;
	}

	/**
	 * @param policyID
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/policies/{policyID}")
	public ResponseWrapper<PoliciesDto> getPolicyDetails(@PathVariable String policyID) throws Exception {
		ResponseWrapper<PoliciesDto> response = new ResponseWrapper<>();
		
		PoliciesDto policyGroup = policyManagementService.getPolicyDetails(policyID).get(0);
		response.setResponse(policyGroup);
		
		return response;
	}

}
