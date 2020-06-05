package io.mosip.pmp.policy.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.adapter.model.AuthUserDetails;
import io.mosip.pmp.policy.dto.AllowedKycDto;
import io.mosip.pmp.policy.dto.AuthPolicyCreateResponseDto;
import io.mosip.pmp.policy.dto.AuthPolicyDto;
import io.mosip.pmp.policy.dto.PolicyCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyCreateResponseDto;
import io.mosip.pmp.policy.dto.PolicyDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyWithAuthPolicyDto;
import io.mosip.pmp.policy.dto.ResponseWrapper;
import io.mosip.pmp.policy.entity.AuthPolicy;
import io.mosip.pmp.policy.entity.AuthPolicyH;
import io.mosip.pmp.policy.entity.PartnerPolicy;
import io.mosip.pmp.policy.entity.PolicyGroup;
import io.mosip.pmp.policy.errorMessages.ErrorMessages;
import io.mosip.pmp.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pmp.policy.errorMessages.PolicyServiceLogger;
import io.mosip.pmp.policy.repository.AuthPolicyHRepository;
import io.mosip.pmp.policy.repository.AuthPolicyRepository;
import io.mosip.pmp.policy.repository.PartnerPolicyRepository;
import io.mosip.pmp.policy.repository.PolicyGroupRepository;
import io.mosip.pmp.policy.util.PolicyUtil;

/**
 * <p>This class manages business logic before or after performing database operations.</p>
 * This class is performing following operations.</br>
 * 1. Creating the policy group {@link #createPolicyGroup(PolicyCreateRequestDto)} </br>
 * 2. Creating the auth policies {@link #createAuthPoliciesRequest(PolicyDto)} </br>
 * 3. Updating the policy group {@link #update(PolicyUpdateRequestDto)} </br>
 * 4. Updating the policy status {@link #updatePolicyStatus(PolicyStatusUpdateRequestDto)} </br>
 * 5. Reading/Getting the policy details {@link #getPolicyDetails(String)} </br>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */

@Service
public class PolicyManagementService {

	@Autowired
	private AuthPolicyRepository authPolicyRepository;

	@Autowired
	private PolicyGroupRepository policyGroupRepository;
	
	@Autowired
	private AuthPolicyHRepository authPolicyHRepository;
	
	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;
	

	/**
	 * <p> This function inserts the policy group data into policy group table.</p>
	 * <p> Checks the database for uniqueness of policy group name.</p>
	 * <p> If policy group name exists throws the exception saying policy group name exists.</p>
	 * <p> else will insert the data into policy group table.</p>
	 * <p> and returns the response;
	 * @param request {@link PolicyCreateRequestDto } Contains input request details.
	 * @return response {@link PolicyCreateResponseDto } Contains response details.
	 * @throws Exception
	 */	
	public ResponseWrapper<PolicyCreateResponseDto> createPolicyGroup(PolicyCreateRequestDto request)
			throws PolicyManagementServiceException,Exception {			
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<>();		
		PolicyCreateResponseDto responseDto = new PolicyCreateResponseDto();		
		PolicyGroup policyGroupInput = new PolicyGroup();
		AuthPolicy authPolicy = new AuthPolicy();
		if(request.getPolicies() == null) {
			PolicyServiceLogger.error("No details found with policy name " + request.getName());
			throw new PolicyManagementServiceException(ErrorMessages.AUTH_POLICIES_NOT_DEFINED.getErrorCode(),
					ErrorMessages.AUTH_POLICIES_NOT_DEFINED.getErrorMessage());
		}
		
		PolicyServiceLogger.info("Validating the policy name " + request.getName());
		PolicyGroup policyGroupName = policyGroupRepository.findByName(request.getName());	
		if(policyGroupName != null){
			PolicyServiceLogger.warn("No details found with policy name " + request.getName());
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_NAME_DUPLICATE_EXCEPTION.getErrorCode(),
					ErrorMessages.POLICY_NAME_DUPLICATE_EXCEPTION.getErrorMessage() +" " + request.getName());			
		}
		policyGroupInput.setName(request.getName());
		policyGroupInput.setDescr(request.getDesc());
		policyGroupInput.setId(PolicyUtil.generateId());
		policyGroupInput.setCrBy(getUser());
		policyGroupInput.setUserId(getUser());
		policyGroupInput.setCrDtimes(LocalDateTime.now());
		policyGroupInput.setIsActive(true);
		
		PolicyDto dto = new PolicyDto();
		dto.setAllowedKycAttributes(request.getPolicies().getAllowedKycAttributes());
		dto.setAuthPolicies(request.getPolicies().getAuthPolicies());
		authPolicy = createAuthPoliciesRequest(dto,request.getName(),request.getName(),request.getDesc(),policyGroupInput.getId());
		
		PolicyServiceLogger.info("Inserting data into policy group table");	
		try {
			policyGroupRepository.save(policyGroupInput);
			authPolicyRepository.save(authPolicy);
			InsertIntoAuthPolicyH(authPolicy);
		}			
		catch (Exception e) {
			PolicyServiceLogger.error("Error occurred while inserting data into policy group table");
			PolicyServiceLogger.error(e.getMessage());
			PolicyServiceLogger.logStackTrace(e);
			throw new PolicyManagementServiceException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
					ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage(), e);
		}
		
		responseDto.setIs_Active(policyGroupInput.getIsActive());
		responseDto.setId(policyGroupInput.getId());
		responseDto.setName(policyGroupInput.getName());
		responseDto.setDesc(policyGroupInput.getDescr());
		responseDto.setCr_by(policyGroupInput.getCrBy());
		responseDto.setCr_dtimes(policyGroupInput.getCrDtimes());
		responseDto.setUp_by(policyGroupInput.getUpdBy());
		responseDto.setUpd_dtimes(policyGroupInput.getUpdDtimes());
		response.setResponse(responseDto);
		
		return response;
	}
	
	/**
	 * <p> This function creates auth policies for policy group.</p>
	 * <p> Validates the policy group id.</p>
	 * <p> If policy details not found for policy id, then throws exception.</p>
	 * <p> Validates the auth policy name.</p>
	 * <p> If name exists then throws exception saying duplicate name.</p>
	 * <p> With input data will create policy document and stores in configured path.</p>
	 * <p> Saves the data into auth policy table.</p>
	 * <p> And returns the response</p>
	 * 
	 * @param request {@link PolicyDto} Contains input information regarding auth policies. 
	 * @return response {@link AuthPolicyCreateResponseDto} Contains auth policies information.
	 * @throws PolicyManagementServiceException Compile time exceptions
	 * @throws Exception runtime exceptions.
	 */
	private AuthPolicy createAuthPoliciesRequest(PolicyDto request, String oldPolicyName, String newPolicyName, String policyDesc, String policyId) 
			throws PolicyManagementServiceException, Exception {
		
		AuthPolicy authPolicy = authPolicyRepository.findByPolicyGroupAndName(policyId, oldPolicyName);
		if(authPolicy != null) {
			authPolicy.setCrBy(getUser());		
			authPolicy.setId(authPolicy.getId());
			authPolicy.setCrDtimes(LocalDateTime.now());
			authPolicy.setDescr(policyDesc);
			authPolicy.setName(newPolicyName);
			authPolicy.setIsActive(true);
			authPolicy.setIsDeleted(false);	
			authPolicy.setPolicy_group_id(policyId);
			authPolicy.setPolicyFileId(generatePolicyJson(request,newPolicyName).toJSONString());
			return authPolicy;
		}else {		
			authPolicy = new AuthPolicy();		
			authPolicy.setCrBy(getUser());		
			authPolicy.setId(PolicyUtil.generateId());
			authPolicy.setCrDtimes(LocalDateTime.now());
			authPolicy.setDescr(policyDesc);
			authPolicy.setName(newPolicyName);
			authPolicy.setIsActive(true);
			authPolicy.setIsDeleted(false);	
			authPolicy.setPolicy_group_id(policyId);
			authPolicy.setPolicyFileId(generatePolicyJson(request,newPolicyName).toJSONString());
			return authPolicy;
		}
	}
	
	/**
	 * This function inserts the records into auth history table
	 * @param authPolicy
	 */
	private void InsertIntoAuthPolicyH(AuthPolicy authPolicy) {
		AuthPolicyH authPolicyH = new AuthPolicyH();		
		authPolicyH.setEffDtimes(new Date());
		authPolicyH.setCrBy(getUser());
		authPolicyH.setCrDtimes(LocalDateTime.now());
		authPolicyH.setDescr(authPolicy.getDescr());
		authPolicyH.setId(PolicyUtil.generateId());
		authPolicyH.setIsActive(authPolicy.getIsActive());
		authPolicyH.setName(authPolicy.getName());
		authPolicyH.setPolicyFileId(authPolicy.getPolicyFileId());
		authPolicyH.setUpdBy(authPolicy.getUpdBy());
		authPolicyH.setUpdDtimes(LocalDateTime.now());
		authPolicyH.setPolicy_group_id(authPolicy.getPolicy_group_id());
		authPolicyHRepository.save(authPolicyH);
	}

	/**
	 * <p>This function updates the policy group details along with auth policies.</p>
	 * <p> </p>
	 * @param updateRequestDto
	 * @return
	 * @throws Exception
	 */
	
	public ResponseWrapper<PolicyUpdateResponseDto> update(PolicyUpdateRequestDto updateRequestDto) throws Exception {		
		PolicyServiceLogger.info("Validating the policy group name " + updateRequestDto.getName());
		PolicyGroup policyGroup = policyGroupRepository.findByName(updateRequestDto.getName());		
		
		if(policyGroup!=null && !policyGroup.getId().equals(updateRequestDto.getId())){
			PolicyServiceLogger.error("Policy name exists with name : " + updateRequestDto.getName());
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_NAME_DUPLICATE_EXCEPTION.getErrorCode(),
					ErrorMessages.POLICY_NAME_DUPLICATE_EXCEPTION.getErrorMessage() +" " + updateRequestDto.getName());
		}
		
		PolicyServiceLogger.info("Validating the policy group ID " + updateRequestDto.getId());
		Optional<PolicyGroup> policyGroupDetails = policyGroupRepository.findById(updateRequestDto.getId());
		if(!policyGroupDetails.isPresent()){
		   PolicyServiceLogger.error("Policy details not exists for policy id :" + updateRequestDto.getId());	
           throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
        		   ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());
		}		
		
		ResponseWrapper<PolicyUpdateResponseDto> response = new ResponseWrapper<>();	
		PolicyUpdateResponseDto responseDto = new PolicyUpdateResponseDto();
		AuthPolicy authPolicy = new AuthPolicy();
		PolicyGroup policyGroupFromDb = null;
		String existingPolicyName = policyGroupDetails.get().getName();
		
		if (policyGroupDetails.get() != null){
			policyGroupFromDb = policyGroupDetails.get();
			policyGroupFromDb.setName(updateRequestDto.getName());
			policyGroupFromDb.setDescr(updateRequestDto.getDesc());
			policyGroupFromDb.setId(policyGroupDetails.get().getId());
			policyGroupFromDb.setCrDtimes(LocalDateTime.now());
			policyGroupFromDb.setIsActive(true);
			policyGroupFromDb.setUpdDtimes(LocalDateTime.now());
			policyGroupFromDb.setUpdBy(getUser());
			
			PolicyServiceLogger.info("Creating auth policies for policy group.");
			authPolicy = createAuthPoliciesRequest(updateRequestDto.getPolicies(),existingPolicyName,policyGroupFromDb.getName(),
					policyGroupFromDb.getDescr(),policyGroupFromDb.getId());
			try{
				policyGroupRepository.save(policyGroupFromDb);
				authPolicyRepository.save(authPolicy);
				InsertIntoAuthPolicyH(authPolicy);
				}catch(Exception e){
					PolicyServiceLogger.error("Error occurred while updating the policy group details");
					PolicyServiceLogger.error(e.getMessage());
			        throw new PolicyManagementServiceException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
			        		   ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
				}
		}
		if(policyGroupFromDb!=null) {			
			responseDto.set_Active(policyGroupFromDb.getIsActive());
			responseDto.setId(policyGroupFromDb.getId());
			responseDto.setName(policyGroupFromDb.getName());
			responseDto.setDesc(policyGroupFromDb.getDescr());
			responseDto.setCr_by(policyGroupFromDb.getCrBy());
			responseDto.setCr_dtimes(policyGroupFromDb.getCrDtimes());
			responseDto.setUp_by(policyGroupFromDb.getUpdBy());
			responseDto.setUpd_dtimes(policyGroupFromDb.getUpdDtimes());
		}
		
		response.setResponse(responseDto);
		
		return response;
	}

	/**
	 * @param statusUpdateRequest
	 * @return
	 */
	public ResponseWrapper<PolicyStatusUpdateResponseDto> updatePolicyStatus(PolicyStatusUpdateRequestDto statusUpdateRequest) {
		Boolean status = statusUpdateRequest.getStatus().contains("De-Active") ? false : true;
		
		ResponseWrapper<PolicyStatusUpdateResponseDto> response = new ResponseWrapper<>();
		PolicyStatusUpdateResponseDto responseDto = new PolicyStatusUpdateResponseDto();
		
		PolicyServiceLogger.info("Validating the policy group id " + statusUpdateRequest.getId());
		Optional<PolicyGroup> policyGroupDetails = policyGroupRepository.findById(statusUpdateRequest.getId());
        if(!policyGroupDetails.isPresent()){
        	PolicyServiceLogger.error("No details found for policy group id " + statusUpdateRequest.getId());
        	throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
        			ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());
        }
		PolicyGroup policyGroupFromDb = null;
		if (policyGroupDetails.get() != null) {
			policyGroupFromDb = policyGroupDetails.get();
			policyGroupFromDb.setIsActive(status);
			policyGroupFromDb.setUpdBy(getUser());
			policyGroupFromDb.setUpdDtimes(LocalDateTime.now());
			List<AuthPolicy> authPolicies = authPolicyRepository.findByPolicyId(statusUpdateRequest.getId());
			for(AuthPolicy authPolicy:authPolicies) {
				authPolicy.setIsActive(status);
				authPolicy.setUpdBy(getUser());
				authPolicy.setUpdDtimes(LocalDateTime.now());
				authPolicyRepository.save(authPolicy);
			}
			try{
			policyGroupRepository.save(policyGroupFromDb);
			}catch(Exception e){
				PolicyServiceLogger.error("Error occurred while saving the updated policy group details to table.");
				PolicyServiceLogger.error(e.getMessage());
				PolicyServiceLogger.logStackTrace(e);
	        	throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
	        			ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());				
			}
		}

		responseDto.setMessage("status updated successfully");
		response.setResponse(responseDto);
	 return response;	
	}

	/**
	 * @param policyId
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public PolicyWithAuthPolicyDto findPolicy(String policyId) throws FileNotFoundException, 
	IOException, ParseException {		
		Optional<PolicyGroup> policyFromDb =policyGroupRepository.findById(policyId);			
		if(!policyFromDb.isPresent()){
			PolicyServiceLogger.error("No details exists for policy group id " + policyId);
        	throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
        			ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());	
		}
		PolicyWithAuthPolicyDto authPolicy = new PolicyWithAuthPolicyDto();
		List<PolicyDto> authPolicies = getAuthPolicies(policyFromDb.get().getId());
		if(!authPolicies.isEmpty()) {
		authPolicy.setPolicies(authPolicies.get(0));
		authPolicy.setPolicy(policyFromDb.get());
		}
		return authPolicy;
	}
	
	/**
	 * This method is used to fetch all policies available in system.
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public List<PolicyWithAuthPolicyDto> findAllPolicies() throws JsonParseException, JsonMappingException, IOException{
		List<PolicyGroup> policies = policyGroupRepository.findAll();
		if(policies.isEmpty()){
			PolicyServiceLogger.error("No details exists");
        	throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
        			ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());	
		}
		List<PolicyWithAuthPolicyDto> allPolicies = new ArrayList<PolicyWithAuthPolicyDto>();
		for(PolicyGroup policy : policies) {
			PolicyWithAuthPolicyDto authPolicy = new PolicyWithAuthPolicyDto();
			List<PolicyDto> authPolicies = getAuthPolicies(policy.getId());
			if(!authPolicies.isEmpty()) {
			authPolicy.setPolicies(authPolicies.get(0));
			authPolicy.setPolicy(policy);
			allPolicies.add(authPolicy);
			}		
		}
		
		return allPolicies;
	}
	
	@SuppressWarnings("unchecked")
	private List<PolicyDto> getAuthPolicies(String policyGroupId) throws JsonParseException, JsonMappingException, IOException{
		List<AuthPolicy> authPolicies= authPolicyRepository.findByPolicyId(policyGroupId);
		List<PolicyDto> policies = new ArrayList<PolicyDto>();
		for(AuthPolicy authPolicy:authPolicies) {	
			PolicyDto dto = new PolicyDto();
			Map<?, ?> readValue = new ObjectMapper().readValue(authPolicy.getPolicyFileId(), Map.class);
			dto.setAuthPolicies((List<AuthPolicyDto>) readValue.get("authPolicies"));
			dto.setAllowedKycAttributes((List<AllowedKycDto>) readValue.get("allowedKycAttributes"));
			policies.add(dto);
		}
		return policies;
	}

	/**
	 * This method is used to find policy mapped to partner api key.
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	public PolicyWithAuthPolicyDto getAuthPolicyWithApiKey(String partnerApiKey) throws FileNotFoundException, IOException, ParseException {
		PartnerPolicy partnerPolicy = partnerPolicyRepository.findByApiKey(partnerApiKey);
		if(partnerPolicy == null) {
			PolicyServiceLogger.error("Partner api key not found");
        	throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
        			ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());
		}
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(partnerPolicy.getPolicyId());
		if(!authPolicy.isPresent()) {
			throw new PolicyManagementServiceException(
					ErrorMessages.NO_POLICY_AGAINST_APIKEY.getErrorCode(),
					ErrorMessages.NO_POLICY_AGAINST_APIKEY.getErrorMessage());
		}
		
		return findPolicy(authPolicy.get().getPolicy_group_id());		
	}

	/**
	 * @param policy
	 * @param name
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked" })
	private JSONObject generatePolicyJson(PolicyDto policy, String name) throws PolicyManagementServiceException,
	Exception {
		PolicyServiceLogger.info("Creating policy document");
		JSONObject obj = new JSONObject();
		JSONArray authPolicies = new JSONArray();
		JSONArray allowedKycAttributes = new JSONArray();
		for (AuthPolicyDto authPolicyDto : policy.getAuthPolicies()) {
			JSONObject authObj = new JSONObject();
			authObj.put("authType", authPolicyDto.getAuthType());
			authObj.put("authSubType", authPolicyDto.getAuthSubType());
			authObj.put("mandatory", authPolicyDto.isMandatory());
			authPolicies.add(authObj);
			}
		for (AllowedKycDto allowedKycDto : policy.getAllowedKycAttributes()) {
			JSONObject allowedKycObj = new JSONObject();
			allowedKycObj.put("attributeName", allowedKycDto.getAttributeName());
			allowedKycObj.put("required", allowedKycDto.isRequired());
			allowedKycAttributes.add(allowedKycObj);
		}
		obj.put("authPolicies", authPolicies);
		obj.put("allowedKycAttributes", allowedKycAttributes);		
		
		return obj;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUser() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.getUserId();
		} else {
			return null;
		}
	}
}