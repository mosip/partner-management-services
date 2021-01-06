package io.mosip.pmp.policy.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.json.simple.parser.ParseException;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pmp.common.dto.PageResponseDto;
import io.mosip.pmp.common.dto.PolicySearchDto;
import io.mosip.pmp.common.dto.SearchAuthPolicy;
import io.mosip.pmp.common.dto.SearchDto;
import io.mosip.pmp.common.entity.PolicyGroup;
import io.mosip.pmp.policy.dto.KeyValuePair;
import io.mosip.pmp.policy.dto.PolicyCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyCreateResponseDto;
import io.mosip.pmp.policy.dto.PolicyGroupCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyGroupCreateResponseDto;
import io.mosip.pmp.policy.dto.PolicyGroupUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyManageEnum;
import io.mosip.pmp.policy.dto.PolicyResponseDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyWithAuthPolicyDto;
import io.mosip.pmp.policy.dto.RequestWrapper;
import io.mosip.pmp.policy.dto.ResponseWrapper;
import io.mosip.pmp.policy.service.PolicyManagementService;
import io.mosip.pmp.policy.util.AuditUtil;
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

	private static final Logger logger = LoggerFactory.getLogger(PolicyManagementController.class);
	
	@Autowired
	private PolicyManagementService policyManagementService;
	
	@Autowired
	AuditUtil auditUtil;

	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PostMapping(value = "/policies/policyGroup")
	public ResponseWrapper<PolicyGroupCreateResponseDto> definePolicyGroup(
			@RequestBody @Valid RequestWrapper<PolicyGroupCreateRequestDto> createRequest){	
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_GROUP);
		PolicyGroupCreateResponseDto responseDto = policyManagementService.createPolicyGroup(createRequest.getRequest());
		ResponseWrapper<PolicyGroupCreateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(responseDto);
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_GROUP_SUCCESS);
		return response;		
	}
	
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@PutMapping(value = "/policies/policyGroup/{policyGroupId}")
	public ResponseWrapper<PolicyGroupCreateResponseDto> updatePolicyGroup(
			@PathVariable String policyGroupId,@RequestBody @Valid RequestWrapper<PolicyGroupUpdateRequestDto> createRequest){	
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_GROUP);
		PolicyGroupCreateResponseDto responseDto = policyManagementService.updatePolicyGroup(createRequest.getRequest(), policyGroupId);
		ResponseWrapper<PolicyGroupCreateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(responseDto);
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());		
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_GROUP_SUCCESS);
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
		
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<PolicyCreateResponseDto>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_GROUP);
		PolicyCreateResponseDto responseDto = policyManagementService.
				createPolicies(createRequest.getRequest());		
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());
		response.setResponse(responseDto);		
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_GROUP_SUCCESS);
		logger.info("Returning response from MispController.");
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
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY);
		PolicyResponseDto responseDto = policyManagementService.publishPolicy(policyGroupId, policyId);
		response.setResponse(responseDto);
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_SUCCESS);
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
			@RequestBody @Valid RequestWrapper<PolicyUpdateRequestDto> updateRequestDto, @PathVariable String policyID)
			throws Exception {
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY);
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<PolicyCreateResponseDto>();
		PolicyCreateResponseDto responseDto = policyManagementService.updatePolicies(updateRequestDto.getRequest(),policyID);
		response.setResponse(responseDto);
		response.setId(updateRequestDto.getId());
		response.setVersion(updateRequestDto.getVersion());
		logger.info("Returning response from MispController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_SUCCESS);
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
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY);
		ResponseWrapper<PolicyStatusUpdateResponseDto> response =  policyManagementService.
				updatePolicyStatus(statusUpdateRequest,policyGroupId,policyID);		
		response.setId(requestDto.getId());
		response.setVersion(requestDto.getVersion());
		logger.info("Returning response from MispController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_SUCCESS);
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
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY);
		response.setResponse(policyManagementService.findAllPolicies());
		logger.info("Returning response from MispController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_SUCCESS);
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
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY);
		PolicyResponseDto responseDto = policyManagementService.findPolicy(policyID);
		response.setResponse(responseDto);
		logger.info("Returning response from MispController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_SUCCESS);
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
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY);
		PolicyResponseDto policyGroup = policyManagementService.getAuthPolicyWithApiKey(partnerApiKey);
		response.setResponse(policyGroup);
		logger.info("Returning response from MispController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_SUCCESS);
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
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager','CREDENTIAL_ISSUANCE','CREATE_SHARE')")
	@GetMapping(value="/policies/partnerId/{partnerId}/policyId/{policyId}")
	public ResponseWrapper<PolicyResponseDto> getPartnersPolicy(@PathVariable String partnerId, @PathVariable String policyId) throws JsonParseException, JsonMappingException, IOException{
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<>();
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY);
		PolicyResponseDto policyGroup = policyManagementService.getPartnerMappedPolicy(partnerId, policyId);
		response.setResponse(policyGroup);
		logger.info("Returning response from MispController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_SUCCESS);
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
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP);
		response.setResponse(policyManagementService.getPolicyGroupPolicy(policyGroupId));
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP_SUCCESS);
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
		logger.info("Calling PolicyManagementService from PolicyManageController.");		
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP);
		response.setResponse(policyManagementService.getPolicyGroup());
		logger.info("Returning response from MispController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP_SUCCESS);
		return response;
	}
	
	@ResponseFilter
	@PostMapping("/policyGroup/search")
	@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','ID_AUTHENTICATION')")
	public ResponseWrapper<PageResponseDto<PolicyGroup>> searchPolicyGroup(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<PolicyGroup>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP);
		responseWrapper.setResponse(policyManagementService.searchPolicyGroup(request.getRequest()));
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP_SUCCESS);
		return responseWrapper;
	}
	
	@ResponseFilter
	@PostMapping("/policy/search")
	@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','ID_AUTHENTICATION')")
	public ResponseWrapper<PageResponseDto<SearchAuthPolicy>> searchPolicy(
			@RequestBody @Valid RequestWrapper<PolicySearchDto> request) {
		ResponseWrapper<PageResponseDto<SearchAuthPolicy>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY);
		responseWrapper.setResponse(policyManagementService.searchPolicy(request.getRequest()));
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_SUCCESS);
		return responseWrapper;
	}
	
	@PreAuthorize("hasAnyRole('POLICYMANAGER','policymanager')")
	@GetMapping(value = "/policies/key/{key}")
    public ResponseWrapper<KeyValuePair<String,Object>> getValueForKey(@PathVariable String key){
		ResponseWrapper<KeyValuePair<String,Object>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(policyManagementService.getValueForKey(key));
    	return responseWrapper;
    }
}
