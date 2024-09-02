package io.mosip.pms.policy.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.PolicyFilterValueDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.PolicySearchDto;
import io.mosip.pms.common.dto.SearchAuthPolicy;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.AuthPolicyH;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyHRepository;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.policy.dto.ColumnCodeValue;
import io.mosip.pms.policy.dto.FilterResponseCodeDto;
import io.mosip.pms.policy.dto.KeyValuePair;
import io.mosip.pms.policy.dto.PartnerPolicySearchDto;
import io.mosip.pms.policy.dto.PolicyCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyCreateResponseDto;
import io.mosip.pms.policy.dto.PolicyDetailsDto;
import io.mosip.pms.policy.dto.PolicyDto;
import io.mosip.pms.policy.dto.PolicyGroupCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyGroupCreateResponseDto;
import io.mosip.pms.policy.dto.PolicyGroupUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyManageEnum;
import io.mosip.pms.policy.dto.PolicyResponseDto;
import io.mosip.pms.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyStatusUpdateResponseDto;
import io.mosip.pms.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyWithAuthPolicyDto;
import io.mosip.pms.policy.dto.ResponseWrapper;
import io.mosip.pms.policy.errorMessages.ErrorMessages;
import io.mosip.pms.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pms.policy.util.AuditUtil;
import io.mosip.pms.policy.util.PolicyUtil;
import io.mosip.pms.policy.validator.exception.InvalidPolicySchemaException;
import io.mosip.pms.policy.validator.exception.PolicyIOException;
import io.mosip.pms.policy.validator.exception.PolicyObjectValidationFailedException;
import io.mosip.pms.policy.validator.spi.PolicyValidator;

/**
 * <p>
 * This class manages business logic before or after performing database
 * operations.
 * </p>
 * This class is performing following operations.</br>
 * 1. Creating the policy group </br>
 * 2. Creating the auth policies </br>
 * 3. Updating the policy group </br>
 * 4. Updating the policy status </br>
 * 5. Reading/Getting the policy details</br>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */

@Service
public class PolicyManagementService {

	private static final Logger logger = PMSLogger.getLogger(PolicyManagementService.class);

	@Autowired
	private AuthPolicyRepository authPolicyRepository;

	@Autowired
	private PolicyGroupRepository policyGroupRepository;

	@Autowired
	private AuthPolicyHRepository authPolicyHRepository;

	@Autowired
	PolicyValidator policyValidator;

	@Autowired
	private WebSubPublisher webSubPublisher;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;
	
	@Value("${pmp.policy.schema.url}")
	private String policySchemaUrl;

	@Value("${pmp.allowed.policy.types}")
	private String supportedPolicyTypes;

	@Autowired
	SearchHelper searchHelper;

	@Autowired
	private FilterHelper filterHelper;
	
	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private FilterColumnValidator filterColumnValidator;

	@Autowired
	AuditUtil auditUtil;

	public static final String ACTIVE_STATUS = "active";
	public static final String NOTACTIVE_STATUS = "de-active";
	public static final String ALL = "all";	

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	Environment environment;

	/**
	 * 
	 * @param requestDto
	 * @return
	 */
	public PolicyGroupCreateResponseDto createPolicyGroup(PolicyGroupCreateRequestDto requestDto) {
		validatePolicyGroupName(requestDto.getName(), true,PolicyManageEnum.CREATE_POLICY_GROUP_FAILURE);
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setCrBy(getUser());
		policyGroup.setCrDtimes(LocalDateTime.now());
		policyGroup.setIsActive(true);
		policyGroup.setName(requestDto.getName());
		policyGroup.setDesc(requestDto.getDesc());
		policyGroup.setUserId(getUser());
		policyGroup.setId(PolicyUtil.generateId());
		policyGroup.setIsDeleted(false);
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_GROUP_SUCCESS, requestDto.getName(), "policyGroupName");
		return savePolicyGroup(policyGroup);
	}

	/**
	 * 
	 * @param Id
	 * @param name
	 * @param descr
	 * @param is_active
	 * @return
	 */
	public PolicyGroupCreateResponseDto updatePolicyGroup(PolicyGroupUpdateRequestDto requestDto,
			String policyGroupId) {
		Optional<PolicyGroup> policyGroupFromDb = policyGroupRepository.findById(policyGroupId);
		if (policyGroupFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_GROUP_FAILURE, requestDto.getName(), "policyGroupName");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());
		}
		PolicyGroup policyGroup = policyGroupFromDb.get();
		if (!policyGroupFromDb.get().getName().equals(requestDto.getName())) {
			validatePolicyGroupName(requestDto.getName(), true,PolicyManageEnum.UPDATE_POLICY_GROUP_FAILURE);
			policyGroup.setName(requestDto.getName());
		}
		if (!policyGroup.getIsActive().equals(requestDto.getIsActive())) {
			if(isActivePolicesExists(policyGroupId)) {
				auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_GROUP_FAILURE, requestDto.getName(), "policyGroupName");
				throw new PolicyManagementServiceException(ErrorMessages.ACTIVE_POLICY_EXISTS_UNDER_POLICY_GROUP.getErrorCode(),
						ErrorMessages.ACTIVE_POLICY_EXISTS_UNDER_POLICY_GROUP.getErrorMessage());				
			}			
			policyGroup.setIsActive(requestDto.getIsActive());
		}
		policyGroup.setDesc(requestDto.getDesc());
		policyGroup.setUpdBy(getUser());
		policyGroup.setUpdDtimes(LocalDateTime.now());
		policyGroup.setUserId(getUser());
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_GROUP_SUCCESS, requestDto.getName(), "policyGroupName");
		return savePolicyGroup(policyGroup);
	}
	
	/**
	 * 
	 * @param policyId
	 * @return
	 */
	private boolean isActivePolicesExists(String policyGroupId) {
		if(!authPolicyRepository.findActivePoliciesByPolicyGroupId(policyGroupId).isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param requestDto
	 * @throws PolicyManagementServiceException
	 * @throws Exception
	 */
	public PolicyCreateResponseDto createPolicies(PolicyCreateRequestDto requestDto)
			throws PolicyManagementServiceException, Exception {
		validatePolicyTypes(requestDto.getPolicyType());
		PolicyGroup policyGroup = validatePolicyGroupName(requestDto.getPolicyGroupName(), false,PolicyManageEnum.CREATE_POLICY_FAILURE);
		if(!policyGroup.getIsActive()) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_FAILURE, requestDto.getName(), "policyName");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorMessages.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		validateAuthPolicyName(policyGroup.getId(), requestDto.getName());
		validatePolicy(requestDto.getPolicyType(), requestDto.getPolicies(),PolicyManageEnum.CREATE_POLICY_FAILURE);
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_SUCCESS, requestDto.getName(), "policyName");
		return savePolicy(requestDto.getPolicies(), requestDto.getName(), requestDto.getName(), requestDto.getDesc(),
				policyGroup.getId(), requestDto.getPolicyType(), requestDto.getPolicyGroupName(),
				requestDto.getVersion(), requestDto.getPolicyId() == null ? "" : requestDto.getPolicyId());
	}

	/**
	 * 
	 * @param requestDto
	 * @return
	 * @throws Exception
	 */
	private void validatePolicy(String policyType, JSONObject policies,PolicyManageEnum auditEnum) throws Exception {
		try {
			policyValidator.validatePolicies(getPolicySchema(policyType),
					IOUtils.toString(policies.toString().getBytes(), "UTF-8"));
		}catch(PolicyObjectValidationFailedException e) {
			auditUtil.setAuditRequestDto(auditEnum);
			logger.error("Error occured while validating the policy {} ", e.getLocalizedMessage(), e);
			throw new PolicyManagementServiceException(e.getErrorCode(), e.getErrorText());
		} catch (InvalidPolicySchemaException | PolicyIOException e) {
			auditUtil.setAuditRequestDto(auditEnum);
			logger.error("Error occured while validating the policy {} ", e.getLocalizedMessage(), e);
			throw new PolicyManagementServiceException(e.getErrorCode(), e.getErrorText());
		} catch (IOException e) {
			auditUtil.setAuditRequestDto(auditEnum);
			logger.error("Error occured while validating the policy {} ", e.getLocalizedMessage(), e);
			throw new PolicyManagementServiceException(ErrorMessages.SCHEMA_POLICY_NOT_MATCHING.getErrorCode(),
					ErrorMessages.SCHEMA_POLICY_NOT_MATCHING.getErrorMessage() + "/" + e.getMessage());
		}
	}

	/**
	 * 
	 * @param requestDto
	 * @throws Exception
	 * @throws PolicyManagementServiceException
	 */
	public PolicyCreateResponseDto updatePolicies(PolicyUpdateRequestDto requestDto, String policyId)
			throws PolicyManagementServiceException, Exception {
		PolicyGroup policyGroup = validatePolicyGroupName(requestDto.getPolicyGroupName(), false,PolicyManageEnum.UPDATE_POLICY_FAILURE);
		AuthPolicy authPolicy = checkMappingExists(policyGroup.getId(), policyId, false,PolicyManageEnum.UPDATE_POLICY_FAILURE);
		// Published policy cannot be updated.
		if(authPolicy.getPolicySchema() != null) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_FAILURE, requestDto.getName(), "policyName");
			throw new PolicyManagementServiceException(ErrorMessages.PUBLISHED_POLICY_NOT_UPDATED.getErrorCode(),
					ErrorMessages.PUBLISHED_POLICY_NOT_UPDATED.getErrorMessage());
		}
		AuthPolicy mappedPolicy = authPolicyRepository.findByPolicyGroupIdAndName(policyGroup.getId(),
				requestDto.getName());
		if (mappedPolicy != null && !mappedPolicy.getName().equals(authPolicy.getName())) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_FAILURE, requestDto.getName(), "policyName");
			throw new PolicyManagementServiceException(
					ErrorMessages.AUTH_POLICY_NAME_DUPLICATE_EXCEPTION.getErrorCode(),
					ErrorMessages.AUTH_POLICY_NAME_DUPLICATE_EXCEPTION.getErrorMessage() + requestDto.getName());
		}
		validatePolicy(authPolicy.getPolicy_type(), requestDto.getPolicies(),PolicyManageEnum.UPDATE_POLICY_FAILURE);
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_SUCCESS, requestDto.getName(), "policyName");
		return savePolicy(requestDto.getPolicies(), authPolicy.getName(), requestDto.getName(), requestDto.getDesc(),
				policyGroup.getId(), authPolicy.getPolicy_type(), requestDto.getPolicyGroupName(),
				requestDto.getVersion(), authPolicy.getId());
	}

	/**
	 * 
	 * @param policy_group_name
	 */
	private PolicyGroup validatePolicyGroupName(String policy_group_name, boolean isExists,PolicyManageEnum auditEnum) {
		PolicyGroup policy_group_by_name = policyGroupRepository.findByName(policy_group_name);
		if (policy_group_by_name == null && !isExists) {
			auditUtil.setAuditRequestDto(auditEnum, policy_group_name, "policyGroupName");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_NAME_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_GROUP_NAME_NOT_EXISTS.getErrorMessage());
		}
		if (policy_group_by_name != null && isExists) {
			auditUtil.setAuditRequestDto(auditEnum, policy_group_name, "policyGroupName");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_NAME_DUPLICATE.getErrorCode(),
					ErrorMessages.POLICY_GROUP_NAME_DUPLICATE.getErrorMessage() + policy_group_name);
		}
		return policy_group_by_name;
	}

	/**
	 * 
	 * @param policyGroup
	 * @return
	 */
	private PolicyGroupCreateResponseDto savePolicyGroup(PolicyGroup policyGroup) {
		policyGroupRepository.save(policyGroup);
		PolicyGroupCreateResponseDto responseDto = new PolicyGroupCreateResponseDto();
		responseDto.setCr_by(policyGroup.getCrBy());
		responseDto.setCr_dtimes(policyGroup.getCrDtimes());
		responseDto.setDesc(policyGroup.getDesc());
		responseDto.setName(policyGroup.getName());
		responseDto.setId(policyGroup.getId());
		responseDto.setIs_Active(policyGroup.getIsActive());
		responseDto.setUp_by(policyGroup.getUpdBy());
		responseDto.setUpd_dtimes(policyGroup.getUpdDtimes());		
		return responseDto;
	}

	/**
	 * 
	 * @param auth_policy_name
	 * @param isExists
	 * @return
	 */
	private void validateAuthPolicyName(String policyGroupId, String auth_policy_name) {
		AuthPolicy auth_policy_by_name = authPolicyRepository.findByPolicyGroupIdAndName(policyGroupId, auth_policy_name);
		if (auth_policy_by_name != null) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_FAILURE, auth_policy_name, "policyName");
			throw new PolicyManagementServiceException(
					ErrorMessages.AUTH_POLICY_NAME_DUPLICATE_EXCEPTION.getErrorCode(),
					ErrorMessages.AUTH_POLICY_NAME_DUPLICATE_EXCEPTION.getErrorMessage() + auth_policy_name);
		}
	}

	/**
	 * 
	 * @param policyGroupName
	 * @param policyName
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public PolicyResponseDto publishPolicy(String policyGroupName, String policyName)
			throws JsonParseException, JsonMappingException, IOException {
		AuthPolicy authPolicy = checkMappingExists(policyGroupName, policyName, false,PolicyManageEnum.PUBLISH_POLICY_FAILURE);
		if (authPolicy.getPolicySchema() != null) {			
			auditUtil.setAuditRequestDto(PolicyManageEnum.PUBLISH_POLICY_FAILURE, policyName, "policyId");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_PUBLISHED.getErrorCode(),
					ErrorMessages.POLICY_PUBLISHED.getErrorMessage());
		}
		authPolicy.setPolicySchema(policySchemaUrl);
		authPolicy.setIsActive(true);
		authPolicy.setUpdBy(getUser());
		authPolicy.setUpdDtimes(LocalDateTime.now());
		authPolicyRepository.save(authPolicy);
		insertIntoAuthPolicyH(authPolicy);
		notify(MapperUtils.mapPolicyToPublishDto(authPolicy,getPolicyObject(authPolicy.getPolicyFileId())));		
		auditUtil.setAuditRequestDto(PolicyManageEnum.PUBLISH_POLICY_SUCCESS, policyName, "policyId");
		return mapPolicyAndPolicyGroup(authPolicy);
	}

	/**
	 * 
	 * @param policyGroup
	 * @param authPolicy
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private PolicyResponseDto mapPolicyAndPolicyGroup(AuthPolicy authPolicy)
			throws JsonParseException, JsonMappingException, IOException {
		PolicyResponseDto response = new PolicyResponseDto();
		response.setCr_by(authPolicy.getCrBy());
		response.setCr_dtimes(getLocalDateTime(authPolicy.getCrDtimes()));
		response.setIs_Active(authPolicy.getIsActive());
		response.setPolicyDesc(authPolicy.getDescr());
		response.setPolicyGroupDesc(authPolicy.getPolicyGroup().getDesc());
		response.setPolicyGroupId(authPolicy.getPolicyGroup().getId());
		response.setPolicyGroupName(authPolicy.getPolicyGroup().getName());
		response.setPolicyGroupStatus(authPolicy.getPolicyGroup().getIsActive());
		response.setPolicyGroup_cr_by(authPolicy.getPolicyGroup().getCrBy());
		response.setPolicyGroup_cr_dtimes(authPolicy.getPolicyGroup().getCrDtimes());
		response.setPolicyGroup_up_by(authPolicy.getPolicyGroup().getUpdBy());
		response.setPolicyGroup_upd_dtimes(authPolicy.getPolicyGroup().getUpdDtimes());
		response.setPolicyId(authPolicy.getId());
		response.setPolicyName(authPolicy.getName());
		response.setPolicyType(authPolicy.getPolicy_type());
		response.setPublishDate(authPolicy.getValidFromDate());
		response.setValidTill(authPolicy.getValidToDate());
		response.setSchema(authPolicy.getPolicySchema());
		response.setStatus(authPolicy.getIsActive() == true ? "PUBLISHED" : "DRAFTED");
		response.setUp_by(authPolicy.getUpdBy());
		response.setUpd_dtimes(authPolicy.getUpdDtimes());
		response.setVersion(authPolicy.getVersion());
		response.setPolicies(getPolicyObject(authPolicy.getPolicyFileId()));
		return response;
	}

	/**
	 * 
	 * @param request
	 * @param oldPolicyName
	 * @param newPolicyName
	 * @param policyDesc
	 * @param policyGroupId
	 * @param policyType
	 * @param policyGroupName
	 * @return
	 * @throws PolicyManagementServiceException
	 * @throws Exception
	 */
	private PolicyCreateResponseDto savePolicy(JSONObject policyJson, String oldPolicyName, String newPolicyName,
			String policyDesc, String policyGroupId, String policyType, String policyGroupName, String version,
			String authPolicyId) throws PolicyManagementServiceException, Exception {
		AuthPolicy authPolicy = authPolicyRepository.findByPolicyGroupIdAndName(policyGroupId, oldPolicyName);
		if (authPolicy != null) {
			authPolicy.setId(authPolicy.getId());
			authPolicy.setDescr(policyDesc);
			authPolicy.setName(newPolicyName);
			authPolicy.getPolicyGroup().setId(policyGroupId);			
			authPolicy.SetVersion(version);
			authPolicy.setPolicyFileId(policyJson.toJSONString());
			authPolicy.setUpdBy(getUser());
			authPolicy.setUpdDtimes(LocalDateTime.now());			
			notify(MapperUtils.mapPolicyToPublishDto(authPolicy,getPolicyObject(authPolicy.getPolicyFileId())));
		} else {
			authPolicy = new AuthPolicy();
			authPolicy.setCrBy(getUser());
			authPolicy.setId((authPolicyId == null || authPolicyId.isBlank() || authPolicyId.isEmpty()) ? PolicyUtil.generateId() : authPolicyId);
			authPolicy.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
			authPolicy.setDescr(policyDesc);
			authPolicy.setName(newPolicyName);
			authPolicy.setIsActive(false);
			authPolicy.setPolicy_type(policyType);
			authPolicy.setIsDeleted(false);
			authPolicy.SetVersion(version);
			authPolicy.setValidFromDate(LocalDateTime.now());
			authPolicy.setValidToDate(LocalDateTime.now().plusYears(200));
			authPolicy.setPolicyGroup(new PolicyGroup());
			authPolicy.getPolicyGroup().setId(policyGroupId);
			authPolicy.setPolicyFileId(policyJson.toJSONString());
		}

		authPolicyRepository.save(authPolicy);
		insertIntoAuthPolicyH(authPolicy);

		PolicyCreateResponseDto responseDto = new PolicyCreateResponseDto();
		responseDto.setIs_Active(authPolicy.getIsActive());
		responseDto.setId(authPolicy.getId());
		responseDto.setName(authPolicy.getName());
		responseDto.setDesc(authPolicy.getDescr());
		responseDto.setCr_by(authPolicy.getCrBy());
		responseDto.setCr_dtimes(authPolicy.getCrDtimes());
		responseDto.setUp_by(authPolicy.getUpdBy());
		responseDto.setUpd_dtimes(authPolicy.getUpdDtimes());
		responseDto.setPolicyGroupName(policyGroupName);
		return responseDto;
	}

	/**
	 * This function inserts the records into auth history table
	 * 
	 * @param authPolicy
	 */
	private void insertIntoAuthPolicyH(AuthPolicy authPolicy) {
		AuthPolicyH authPolicyH = new AuthPolicyH();
		authPolicyH.setEffDtimes(new Date());
		authPolicyH.setCrBy(getUser());
		authPolicyH.setCrDtimes(LocalDateTime.now());
		authPolicyH.setDescr(authPolicy.getDescr());
		authPolicyH.setId(PolicyUtil.generateId());
		authPolicyH.setIsActive(authPolicy.getIsActive());
		authPolicyH.setIsDeleted(authPolicy.getIsDeleted());
		authPolicyH.setName(authPolicy.getName());
		authPolicyH.setPolicyFileId(authPolicy.getPolicyFileId());
		authPolicyH.setUpdBy(authPolicy.getUpdBy());
		authPolicyH.setUpdDtimes(LocalDateTime.now());
		authPolicyH.setPolicy_group_id(authPolicy.getPolicyGroup().getId());
		authPolicyH.setPolicy_type(authPolicy.getPolicy_type());
		authPolicyH.SetVersion(authPolicy.getVersion());
		authPolicyH.setValidFromDate(authPolicy.getValidFromDate());
		authPolicyH.setValidToDate(authPolicy.getValidToDate());
		authPolicyH.SetPolicySchema(authPolicy.getPolicySchema());
		authPolicyHRepository.save(authPolicyH);
	}

	/**
	 * @param statusUpdateRequest
	 * @return
	 */
	public ResponseWrapper<PolicyStatusUpdateResponseDto> updatePolicyStatus(
			PolicyStatusUpdateRequestDto statusUpdateRequest, String policyGroupId, String policyId) {
		if (!(statusUpdateRequest.getStatus().toLowerCase().equals(ACTIVE_STATUS)
				|| statusUpdateRequest.getStatus().toLowerCase().equals(NOTACTIVE_STATUS))) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_STATUS_FAILURE, policyId, "policyId");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_STATUS_CODE_EXCEPTION.getErrorCode(),
					ErrorMessages.POLICY_STATUS_CODE_EXCEPTION.getErrorMessage());
		}
		Boolean status = statusUpdateRequest.getStatus().equalsIgnoreCase("De-Active") ? false : true;
		AuthPolicy authPolicy = checkMappingExists(policyGroupId, policyId, false,
				PolicyManageEnum.UPDATE_POLICY_STATUS_FAILURE);

		//published policy status cannot be changed
		if (authPolicy.getPolicySchema() != null) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_STATUS_FAILURE, policyId, "policyId");
			throw new PolicyManagementServiceException(ErrorMessages.PUBLISHED_POLICY_STATUS_UPDATE.getErrorCode(),
					ErrorMessages.PUBLISHED_POLICY_STATUS_UPDATE.getErrorMessage());
		}

		// Unpublished policy cannot be made active
		if (authPolicy.getPolicySchema() == null && status) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_STATUS_FAILURE, policyId, "policyId");
			throw new PolicyManagementServiceException(ErrorMessages.DRAFTED_POLICY_NOT_ACTIVE.getErrorCode(),
					ErrorMessages.DRAFTED_POLICY_NOT_ACTIVE.getErrorMessage());
		}
		// policy having active apikeys cannot be made de-active
		if(!partnerPolicyRepository.findByPolicyIdAndIsActiveTrue(policyId).isEmpty() && !status) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_STATUS_FAILURE, policyId, "policyId");
			throw new PolicyManagementServiceException(ErrorMessages.ACTIVE_APIKEY_EXISTS_UNDER_POLICY.getErrorCode(),
					ErrorMessages.ACTIVE_APIKEY_EXISTS_UNDER_POLICY.getErrorMessage());
		}
		authPolicy.setIsActive(status);
		authPolicy.setUpdBy(getUser());
		authPolicy.setUpdDtimes(LocalDateTime.now());
		authPolicyRepository.save(authPolicy);
		insertIntoAuthPolicyH(authPolicy);
		ResponseWrapper<PolicyStatusUpdateResponseDto> response = new ResponseWrapper<>();
		PolicyStatusUpdateResponseDto responseDto = new PolicyStatusUpdateResponseDto();
		responseDto.setMessage("status updated successfully");
		notify(MapperUtils.mapPolicyToPublishDto(authPolicy,getPolicyObject(authPolicy.getPolicyFileId())));
		response.setResponse(responseDto);
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_STATUS_SUCCESS, policyId, "policyId");
		return response;
	}

	/**
	 * 
	 * @param uniquePolicyGroupAttribute
	 * @param uniquePolicyAttribute
	 * @param hasToCheckWithName
	 * @return
	 */
	private AuthPolicy checkMappingExists(String uniquePolicyGroupAttribute, String uniquePolicyAttribute,
			boolean hasToCheckWithName,PolicyManageEnum auditEnum) {
		Optional<PolicyGroup> policyGroup = Optional.empty();
		Optional<AuthPolicy> authPolicy = Optional.empty();
		if (hasToCheckWithName) {
			policyGroup = Optional.of(policyGroupRepository.findByName(uniquePolicyGroupAttribute));
			authPolicy = Optional.of(authPolicyRepository.findByName(uniquePolicyAttribute));
		}
		if (!hasToCheckWithName) {
			policyGroup = policyGroupRepository.findById(uniquePolicyGroupAttribute);
			authPolicy = authPolicyRepository.findById(uniquePolicyAttribute);
		}
		if (policyGroup.isEmpty()) {
			auditUtil.setAuditRequestDto(auditEnum, uniquePolicyGroupAttribute, "policyGroupAttr");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_ID_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_GROUP_ID_NOT_EXISTS.getErrorMessage());
		}
		if(!policyGroup.get().getIsActive()) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_FAILURE, policyGroup.get().getName(), "policyGroupName");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorMessages.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		if (authPolicy.isEmpty()) {
			auditUtil.setAuditRequestDto(auditEnum, uniquePolicyGroupAttribute, "policyAttr");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());
		}
		if (!policyGroup.get().getId().equals(authPolicy.get().getPolicyGroup().getId())) {
			auditUtil.setAuditRequestDto(auditEnum, authPolicy.get().getName(), "policyName");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_POLICY_NOT_MAPPED.getErrorCode(),
					ErrorMessages.POLICY_GROUP_POLICY_NOT_MAPPED.getErrorMessage());
		}
		return authPolicy.get();
	}

	/**
	 * @param policyId
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public PolicyResponseDto findPolicy(String policyId) throws FileNotFoundException, IOException, ParseException {
		AuthPolicy authPolicy = getAuthPolicy(policyId);					
		return mapPolicyAndPolicyGroup(authPolicy);
	}

	/**
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	public List<PolicyResponseDto> findAllPolicies() throws FileNotFoundException, IOException, ParseException {
		List<AuthPolicy> authPolicies = authPolicyRepository.findAll();
		List<PolicyResponseDto> authPolicesResponse = new ArrayList<PolicyResponseDto>();
		for (AuthPolicy authPolicy : authPolicies) {
			authPolicesResponse.add(findPolicy(authPolicy.getId()));
		}
		return authPolicesResponse;
	}

	/**
	 * 
	 * @param policyId
	 * @return
	 */
	private AuthPolicy getAuthPolicy(String policyId) {
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(policyId);
		if (authPolicy.isEmpty()) {
			logger.error("Policy not exists with id {} ", policyId);
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());

		}
		return authPolicy.get();
	}

	/**
	 * 
	 * @param policyGroupId
	 * @return
	 */
	private PolicyGroup getPolicyGroup(String policyGroupId) {
		Optional<PolicyGroup> policyGroup = policyGroupRepository.findById(policyGroupId);
		if (policyGroup.isEmpty()) {
			logger.error("Policy group not exists with id {} ", policyGroupId);
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_ID_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_GROUP_ID_NOT_EXISTS.getErrorMessage());
		}
		return policyGroup.get();
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
	public PolicyResponseDto getPartnerMappedPolicy(String partnerId, String policyId)
			throws JsonParseException, JsonMappingException, IOException {
		List<PartnerPolicy> partnerPolicy = partnerPolicyRepository.findByPartnerIdAndPolicyIdAndIsActiveTrue(partnerId, policyId);
		if (partnerPolicy.isEmpty()) {
			logger.error("Policy is not mapped for given partner {} and policy {}", partnerId, policyId);
			throw new PolicyManagementServiceException(ErrorMessages.NO_POLICY_AGAINST_PARTNER.getErrorCode(),
					ErrorMessages.NO_POLICY_AGAINST_PARTNER.getErrorMessage());
		}
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(policyId);
		if (authPolicy.isEmpty()) {	
			logger.error("Policy not exists with id {} ", policyId);
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage());

		}
		return mapPolicyAndPolicyGroup(authPolicy.get());
	}

	/**
	 * 
	 * @param policyGroupId
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public PolicyWithAuthPolicyDto getPolicyGroupPolicy(String policyGroupId)
			throws JsonParseException, JsonMappingException, IOException {
		PolicyGroup policyGroup = getPolicyGroup(policyGroupId);
		List<AuthPolicy> policies = authPolicyRepository.findByPolicyGroupId(policyGroupId);
		List<PolicyDto> policyGroupPolicies = new ArrayList<PolicyDto>();
		for (AuthPolicy authPolicy : policies) {
			policyGroupPolicies.add(mapPolicyToPolicyDto(authPolicy));
		}
		PolicyWithAuthPolicyDto response = new PolicyWithAuthPolicyDto();
		response.setPolicyGroup(policyGroup);
		response.setPolicies(policyGroupPolicies);		
		return response;
	}

	/**
	 * 
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public List<PolicyWithAuthPolicyDto> getPolicyGroup() throws JsonParseException, JsonMappingException, IOException {
		List<PolicyGroup> policyGroups = policyGroupRepository.findAll();
		logger.debug("policyGroups having size- "+ policyGroups.size());
		logger.debug(String.valueOf(policyGroups));
		List<PolicyWithAuthPolicyDto> response = new ArrayList<PolicyWithAuthPolicyDto>();
		for (PolicyGroup policyGroup : policyGroups) {
			PolicyWithAuthPolicyDto policyGroupWthPolicy = new PolicyWithAuthPolicyDto();
			List<AuthPolicy> policies = authPolicyRepository.findByPolicyGroupId(policyGroup.getId());
			logger.debug("Polices from authpolicyRepo"+ policies);
			List<PolicyDto> policyGroupPolicies = new ArrayList<PolicyDto>();
			for (AuthPolicy authPolicy : policies) {
				logger.debug("authPolicy"+authPolicy);
				policyGroupPolicies.add(mapPolicyToPolicyDto(authPolicy));
			}
			policyGroupWthPolicy.setPolicyGroup(policyGroup);
			policyGroupWthPolicy.setPolicies(policyGroupPolicies);
			response.add(policyGroupWthPolicy);
		}		
		return response;
	}

	/**
	 * 
	 * @param authPolicy
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private PolicyDto mapPolicyToPolicyDto(AuthPolicy authPolicy)
			throws JsonParseException, JsonMappingException, IOException {
		PolicyDto policyDto = new PolicyDto();
		policyDto.setCr_by(authPolicy.getCrBy());
		policyDto.setCr_dtimes(getLocalDateTime(authPolicy.getCrDtimes()));
		policyDto.setIs_Active(authPolicy.getIsActive());
		policyDto.setPolicyDesc(authPolicy.getDescr());
		policyDto.setPolicyId(authPolicy.getId());
		policyDto.setPolicyName(authPolicy.getName());
		policyDto.setPublishDate(authPolicy.getValidFromDate());
		policyDto.setSchema(authPolicy.getPolicySchema());
		policyDto.setStatus(getPolicyStatus(authPolicy.getIsActive()));
		policyDto.setUp_by(authPolicy.getUpdBy());
		policyDto.setUpd_dtimes(authPolicy.getUpdDtimes());
		policyDto.setValidTill(authPolicy.getValidToDate());
		policyDto.setVersion(authPolicy.getVersion());
		logger.debug("authPolicy file id"+authPolicy.getPolicyFileId());
		logger.debug("policydto"+ policyDto);
		policyDto.setPolicies(getPolicyObject(authPolicy.getPolicyFileId()));
		return policyDto;
	}

	private JSONObject getPolicyObject(String policy) {
		JSONParser parser = new JSONParser();
		String error = null;
		logger.debug("policy in getpolicy object"+policy);
		try {
			return ((JSONObject) parser.parse(policy));
		} catch (ParseException e) {
			logger.error("Error occured while getting the getPolicyObject {} ", e.getLocalizedMessage(), e);
			auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_FAILURE);
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_PARSING_ERROR.getErrorCode(),
					ErrorMessages.POLICY_PARSING_ERROR.getErrorMessage() + error);
		}
	}

	/**
	 * 
	 * @param isActive
	 * @return
	 */
	private String getPolicyStatus(Boolean isActive) {
		return isActive == true ? "PUBLISHED" : "DRAFTED";
	}

	/**
	 * 
	 * @return
	 */
	private String getUser() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.getUserId();
		} else {
			return "system";
		}
	}

	/**
	 * 
	 * @param policyType
	 */
	private void validatePolicyTypes(String policyType) {
		if (!Arrays.stream(supportedPolicyTypes.split(",")).anyMatch(policyType::equalsIgnoreCase)) {
			auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_FAILURE, policyType, "policyType");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_TYPE_NOT_ALLOWED.getErrorCode(),
					ErrorMessages.POLICY_TYPE_NOT_ALLOWED.getErrorMessage());
		}
	}

	/**
	 * 
	 * @param version
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	private String getPolicySchema(String policyType)
			throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		return mapper.readValue(new URL(environment.getProperty("pmp." + policyType.toLowerCase() + ".policy.schema")),
				JsonNode.class).toString();
	}

	/**
	 * 
	 * @param partner
	 */
	private void notify(String policy) {
		Type type = new Type();
		type.setName("PolicyManagementService");
		type.setNamespace("io.mosip.pmp.policy.service");
		Map<String, Object> data = new HashMap<>();
		data.put("policyId", policy);
		webSubPublisher.notify(EventType.POLICY_UPDATED, data, type);
	}

	private LocalDateTime getLocalDateTime(Timestamp date) {
		if (date != null) {
			return date.toLocalDateTime();
		}
		return LocalDateTime.now();
	}

	public PageResponseDto<PartnerPolicySearchDto> searchPartnerPolicy(SearchDto dto) {
		List<PartnerPolicySearchDto> partnerPolicies = new ArrayList<>();
		PageResponseDto<PartnerPolicySearchDto> pageDto = new PageResponseDto<>();
		Page<PartnerPolicy> page = searchHelper.search(PartnerPolicy.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partnerPolicies = MapperUtils.mapAll(page.getContent(), PartnerPolicySearchDto.class);
			pageDto = pageUtils.sortPage(partnerPolicies, dto.getSort(), dto.getPagination(),page.getTotalElements());
		}
		return pageDto;
	}

	public PageResponseDto<PolicyGroup> searchPolicyGroup(SearchDto dto) {
		List<PolicyGroup> policies = new ArrayList<>();
		PageResponseDto<PolicyGroup> pageDto = new PageResponseDto<>();
		Page<PolicyGroup> page = searchHelper.search(PolicyGroup.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			policies = MapperUtils.mapAll(page.getContent(), PolicyGroup.class);
			pageDto = pageUtils.sortPage(policies, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		auditUtil.setAuditRequestDto(PolicyManageEnum.SEARCH_POLICY_GROUP_SUCCESS);
		return pageDto;
	}


	public PageResponseDto<SearchAuthPolicy> searchPolicy(PolicySearchDto dto) {
		List<SearchAuthPolicy> policies = new ArrayList<>();
		PageResponseDto<SearchAuthPolicy> pageDto = new PageResponseDto<>();
		if (!dto.getPolicyType().equalsIgnoreCase(ALL)) {
			List<SearchFilter> filters = new ArrayList<>();
			SearchFilter authtypeSearch = new SearchFilter();
			authtypeSearch.setColumnName("policyType");
			authtypeSearch.setValue(dto.getPolicyType());
			authtypeSearch.setType("equals");
			filters.addAll(dto.getFilters());
			filters.add(authtypeSearch);
			dto.setFilters(filters);
		}		
		
		Optional<SearchFilter> policyGroupNameFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("policyGroupName")).findFirst();
		dto.getFilters().removeIf(f->f.getColumnName().equalsIgnoreCase("policyGroupName"));		
		Optional<SearchFilter> policydescFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("desc")).findFirst();		
		dto.getFilters().removeIf(f->f.getColumnName().equalsIgnoreCase("desc"));
		if(policydescFilter.isPresent()) {
			policydescFilter.get().setColumnName("descr");
			dto.getFilters().add(policydescFilter.get());
		}
		
		Page<AuthPolicy> page = searchHelper.search(AuthPolicy.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			if (policyGroupNameFilter.isPresent()) {
				String value = policyGroupNameFilter.get().getValue();
				policies = MapperUtils.mapAuthPolicySearch(page.getContent().stream()
						.filter(f -> f.getPolicyGroup().getName().contains(value))
						.collect(Collectors.toList()));
			} else {
				policies = MapperUtils.mapAuthPolicySearch(page.getContent());
			}
			pageDto = pageUtils.sortPage(policies, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		auditUtil.setAuditRequestDto(PolicyManageEnum.SEARCH_POLICY_SUCCESS);
		return pageDto;
	}

	/**
	 * This method returns value based on the key from configuration.
	 * 
	 * @param key
	 * @return
	 */
	public KeyValuePair<String, Object> getValueForKey(String key) {
		JSONParser parser = new JSONParser();
		String configValue = environment.getProperty(key);
		if (configValue == null) {
			return new KeyValuePair<String, Object>(key, configValue);
		}
		if (StringUtils.isNumeric(configValue)) {
			return new KeyValuePair<String, Object>(key, configValue);
		}
		try {
			return new KeyValuePair<String, Object>(key, (JSONObject) parser.parse(configValue));
		} catch (ParseException e) {
			logger.error("Error occured while reading the getValueForKey {} ", e.getLocalizedMessage(), e);
		}

		return new KeyValuePair<String, Object>(key, configValue);
	}


	/**
	 * 
	 * @param filterValueDto
	 * @return
	 */
	public FilterResponseCodeDto policyGroupFilterValues(FilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), PolicyGroup.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(PolicyGroup.class,
						filterDto, filterValueDto, "name");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		auditUtil.setAuditRequestDto(PolicyManageEnum.FILTERVALUES_POLICY_GROUP_SUCCESS);
		return filterResponseDto;
	}

	/**
	 * 
	 * @param filterValueDto
	 * @return
	 */
	public FilterResponseCodeDto policyFilterValues(PolicyFilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if(!filterValueDto.getPolicyType().equalsIgnoreCase(ALL)) {
			List<SearchFilter> filters = new ArrayList<>();
			SearchFilter authtypeSearch = new SearchFilter();
			authtypeSearch.setColumnName("policyType");
			authtypeSearch.setValue(filterValueDto.getPolicyType());
			authtypeSearch.setType("equals");
			filters.addAll(filterValueDto.getOptionalFilters());
			filters.add(authtypeSearch);
			filterValueDto.setOptionalFilters(filters);
		}
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), AuthPolicy.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(AuthPolicy.class,
						filterDto, filterValueDto, "id");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		auditUtil.setAuditRequestDto(PolicyManageEnum.FILTERVALUES_POLICY_SUCCESS);
		return filterResponseDto;
	}
	
	private void notify(PolicyPublishDto dataToPublish) {
		Type type = new Type();
		type.setName("PolicyManagementService");
		type.setNamespace("io.mosip.pmp.policy.service");
		Map<String, Object> data = new HashMap<>();
		data.put("policyData", dataToPublish);
		webSubPublisher.notify(EventType.POLICY_UPDATED, data, type);
	}
	
	/**
	 * Gets the active policyDetails for given policy group name
	 * 
	 * @param policyGroupName
	 * @return
	 */
	public List<PolicyDetailsDto> getActivePolicyDetailsByGroupName(String policyGroupName) {
		PolicyGroup policy_group_by_name = policyGroupRepository.findByName(policyGroupName);
		if (policy_group_by_name == null) {
			logger.error("Policy group not exists with name {}", policyGroupName);
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_NAME_NOT_EXISTS.getErrorCode(),
					ErrorMessages.POLICY_GROUP_NAME_NOT_EXISTS.getErrorMessage());
		}
		if (!policy_group_by_name.getIsActive()) {
			logger.error("Policy group with name {} is not active", policyGroupName);
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorMessages.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}

		List<PolicyDetailsDto> policiesByGroupName = new ArrayList<>();
		List<AuthPolicy> authPoliciesByGroupName = authPolicyRepository
				.findActivePoliciesByPolicyGroupId(policy_group_by_name.getId());
		authPoliciesByGroupName.forEach(policies -> {
			PolicyDetailsDto dto = new PolicyDetailsDto();
			dto.setDescr(policies.getDescr());
			dto.setId(policies.getId());
			dto.setName(policies.getName());
			dto.setPolicyGroupId(policies.getPolicyGroup().getId());
			dto.setPolicyGroupName(policies.getPolicyGroup().getName());
			dto.setPolicyType(policies.getPolicy_type());
			policiesByGroupName.add(dto);
		});
		return policiesByGroupName;

	}

	public List<PolicyGroup> getAllPolicyGroups() {
		List<PolicyGroup> policyGroupsList = policyGroupRepository.findAllActivePolicyGroups();
		if (policyGroupsList.isEmpty()) {
			logger.error("There are no active policy groups");
			throw new PolicyManagementServiceException(ErrorMessages.POLICY_GROUPS_NOT_AVAILABLE.getErrorCode(),
					ErrorMessages.POLICY_GROUPS_NOT_AVAILABLE.getErrorMessage());
		}
		return policyGroupsList;
	}
}
