package io.mosip.pmp.partnermanagement.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pmp.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pmp.common.constant.EventType;
import io.mosip.pmp.common.dto.Type;
import io.mosip.pmp.common.entity.AuthPolicy;
import io.mosip.pmp.common.entity.BiometricExtractorProvider;
import io.mosip.pmp.common.entity.MISPEntity;
import io.mosip.pmp.common.entity.MISPLicenseEntity;
import io.mosip.pmp.common.entity.Partner;
import io.mosip.pmp.common.entity.PartnerPolicy;
import io.mosip.pmp.common.entity.PartnerPolicyRequest;
import io.mosip.pmp.common.exception.ApiAccessibleException;
import io.mosip.pmp.common.helper.WebSubPublisher;
import io.mosip.pmp.common.repository.AuthPolicyRepository;
import io.mosip.pmp.common.repository.BiometricExtractorProviderRepository;
import io.mosip.pmp.common.repository.MispLicenseKeyRepository;
import io.mosip.pmp.common.repository.MispServiceRepository;
import io.mosip.pmp.common.repository.PartnerPolicyRepository;
import io.mosip.pmp.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.common.repository.PartnerRepository;
import io.mosip.pmp.common.repository.PolicyGroupRepository;
import io.mosip.pmp.common.util.RestUtil;
import io.mosip.pmp.partnermanagement.constant.InvalidInputParameterConstant;
import io.mosip.pmp.partnermanagement.constant.NoPartnerApiKeyRequestsConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIKeyDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerIdDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerValidationsConstants;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.AuthPolicyAttributes;
import io.mosip.pmp.partnermanagement.dto.KYCAttributes;
import io.mosip.pmp.partnermanagement.dto.MISPValidatelKeyResponseDto;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnerCertDownloadResponeDto;
import io.mosip.pmp.partnermanagement.dto.PartnerPolicyResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.Policies;
import io.mosip.pmp.partnermanagement.dto.PolicyDTO;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.exception.InvalidInputParameterException;
import io.mosip.pmp.partnermanagement.exception.NoPartnerApiKeyRequestsException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIKeyDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerIdDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerValidationException;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;
import io.mosip.pmp.partnermanagement.util.PartnerUtil;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
public class PartnerManagementServiceImpl implements PartnerManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerManagementServiceImpl.class);

	@Autowired
	private MispLicenseKeyRepository misplKeyRepository;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Autowired
	PartnerRepository partnerRepository;

	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@Autowired
	PolicyGroupRepository policyGroupRepository;

	@Autowired
	BiometricExtractorProviderRepository extractorProviderRepository;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

	@Autowired
	MispServiceRepository mispRepository;
	
	@Autowired
	private WebSubPublisher webSubPublisher;
	
	@Autowired
	RestUtil restUtil;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private ObjectMapper mapper;

	private static final String APPROVED = "Approved";

	private static final String REJECTED = "Rejected";	
	
	private static final String ERRORS = "errors";

	private static final String ERRORCODE = "errorCode";

	private static final String ERRORMESSAGE = "message";

	private static final String ACTIVE = "Active";

	private static final String DEACTIVE = "De-Active";
	
	@Value("${mosip.pmp.partner.policy.expiry.period.indays}")
	private int partnerPolicyExpiryInDays;
	
	@Value("${pmp.bioextractors.required.partner.types}")
	private String biometricExtractorsRequiredPartnerTypes;

	@Override
	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerId, String policyAPIKey) {
		LOGGER.info("Getting details from partner Policy for given API_KEY and PartnerID :" + policyAPIKey + " and "
				+ partnerId);
		Optional<PartnerPolicy> partnerPolicyByAPIKey = partnerPolicyRepository.findById(policyAPIKey);
		if (!partnerPolicyByAPIKey.isEmpty()) {
			if (partnerPolicyByAPIKey.get().getPartner().getId().equals(partnerId)
					&& partnerPolicyByAPIKey.get().getPolicyId().equals(request.getOldPolicyID())) {
				Optional<AuthPolicy> requestedPolicy = authPolicyRepository.findById(request.getNewPolicyID());
				if (!requestedPolicy.isEmpty() && requestedPolicy.get().getPolicyGroup().getId()
						.equals(partnerPolicyByAPIKey.get().getPartner().getPolicyGroupId())) {
					PartnerPolicy updateObject = partnerPolicyByAPIKey.get();
					updateObject.setUpdBy(getUser());
					updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
					updateObject.setPolicyId(requestedPolicy.get().getId());
					partnerPolicyRepository.save(updateObject);
				} else {
					throw new PartnerValidationException(
							PartnerValidationsConstants.POLICY_NOT_BELONGS_TO_PARTNER_POLICY_GROUP.getErrorCode(),
							PartnerValidationsConstants.POLICY_NOT_BELONGS_TO_PARTNER_POLICY_GROUP.getErrorMessage());
				}

			} else {
				throw new PartnerValidationException(
						PartnerValidationsConstants.PARTNER_APIKEY_POLICY_MAPPING_NOTEXISTS.getErrorCode(),
						PartnerValidationsConstants.PARTNER_APIKEY_POLICY_MAPPING_NOTEXISTS.getErrorMessage());
			}

		} else {
			throw new PartnerAPIKeyDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}

		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner api key to Policy Mappings updated successfully");
		notify(policyAPIKey, true);
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID,
			ActivateDeactivatePartnerRequest request) {
		if (!(request.getStatus().equalsIgnoreCase(ACTIVE) || request.getStatus().equalsIgnoreCase(DEACTIVE))) {
			LOGGER.info(request.getStatus() + " : is Invalid Input Parameter, it should be (Active/De-Active)");
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorCode(),
					InvalidInputParameterConstant.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorMessage());
		}
		Boolean status = request.getStatus().equalsIgnoreCase(ACTIVE) ? true : false;
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerID);
		if (partnerFromDb.isPresent()) {
			Partner updatePartnerObject = partnerFromDb.get();
			updatePartnerObject.setIsActive(status);
			updatePartnerObject.setUpdBy(getUser());
			updatePartnerObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
			partnerRepository.save(updatePartnerObject);
		} else {
			LOGGER.info(partnerID + " : Partner Id Does Not Exist");
			throw new PartnerIdDoesNotExistException(
					PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		partnersPolicyMappingResponse.setMessage("Partner status updated successfully");
		notify(partnerID, false);
		return partnersPolicyMappingResponse;		
		
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID,
			ActivateDeactivatePartnerRequest request, String partnerAPIKey) {
		if (!(request.getStatus().equalsIgnoreCase(ACTIVE) || request.getStatus().equalsIgnoreCase(DEACTIVE))) {
			LOGGER.info(request.getStatus() + " : is Invalid Input Parameter, it should be (Active/De-Active)");
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorCode(),
					InvalidInputParameterConstant.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorMessage());
		}
		Boolean status = request.getStatus().equalsIgnoreCase(ACTIVE) ? true : false;
		Optional<PartnerPolicy> partnerPolicyFromDb = partnerPolicyRepository.findById(partnerAPIKey);		
		if (partnerPolicyFromDb.isPresent()) {
			PartnerPolicy partnerPolicy = partnerPolicyFromDb.get();
			if (partnerPolicy.getPartner().getId().equals(partnerID)) {
				PartnerPolicy updatepartnerPolicyObject = partnerPolicyFromDb.get();
				updatepartnerPolicyObject.setIsActive(status);
				updatepartnerPolicyObject.setUpdBy(getUser());
				updatepartnerPolicyObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
				partnerPolicyRepository.save(updatepartnerPolicyObject);
				LOGGER.info(partnerAPIKey + " : API KEY Status Updated Successfully");			
			}else {
				LOGGER.info(partnerID + " : Partner Id Does Not Exist");
				throw new PartnerValidationException(
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());			
			}
		}else {
			throw new PartnerAPIKeyDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner API Key status updated successfully");
		notify(partnerAPIKey, true);
		return partnersPolicyMappingResponse;
	}

	@Override
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup(Optional<String> partnerType) {
		RetrievePartnerDetailsResponse partnersResponse = new RetrievePartnerDetailsResponse();
		List<RetrievePartnersDetails> partners = new ArrayList<RetrievePartnersDetails>();
		List<Partner> partnersFromDb = null;
		if (partnerType.isPresent() && !partnerType.get().trim().isEmpty()) {
			partnersFromDb = partnerRepository.findByPartnerType(partnerType.get());
		} else {
			partnersFromDb = partnerRepository.findAll();
		}
		Partner partner = null;
		if (partnersFromDb.isEmpty()) {
			throw new PartnerValidationException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Iterator<Partner> partnerIterat = partnersFromDb.iterator();
		while (partnerIterat.hasNext()) {
			RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
			partner = partnerIterat.next();
			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails.setStatus(partner.getIsActive() == true ? ACTIVE : DEACTIVE);
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContactNo());
			retrievePartnersDetails.setEmailId(partner.getEmailId());
			retrievePartnersDetails.setAddress(partner.getAddress());
			retrievePartnersDetails.setPartnerType(partner.getPartnerTypeCode());
			partners.add(retrievePartnersDetails);
		}
		partnersResponse.setPartners(partners);
		return partnersResponse;
	}

	@Override
	public RetrievePartnersDetails getparticularAuthEKYCPartnerDetailsForGivenPartnerId(String partnerID) {
		Optional<Partner> findPartnerById = partnerRepository.findById(partnerID);
		RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
		if (findPartnerById.isPresent()) {
			Partner partner = findPartnerById.get();
			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails.setStatus(partner.getIsActive() == true ? ACTIVE : DEACTIVE);
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContactNo());
			retrievePartnersDetails.setEmailId(partner.getEmailId());
			retrievePartnersDetails.setAddress(partner.getAddress());

		} else {
			throw new PartnerValidationException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return retrievePartnersDetails;
	}

	@Override
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerID,
			String PartnerAPIKey) {
		PartnerAPIKeyToPolicyMappingsResponse response = new PartnerAPIKeyToPolicyMappingsResponse();
		Optional<PartnerPolicy> partnerPolicyFromDb = partnerPolicyRepository.findById(PartnerAPIKey);		
		if (partnerPolicyFromDb.isPresent() &&
				partnerPolicyFromDb.get().getPartner().getId().equals(partnerID)) {
			response.setPartnerID(partnerID);
			response.setPolicyId(partnerPolicyFromDb.get().getPolicyId());			 
		} else {
			LOGGER.info(PartnerAPIKey + " : Partner API KEY Not Exist");
			LOGGER.info(PartnerAPIKey + " : Partner api key mapping not exists.");
			throw new PartnerAPIDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_NOT_MAPPED.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_NOT_MAPPED.getErrorMessage());
		}
		return response;
	}

	@Override
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers() {
		List<ApikeyRequests> response = new ArrayList<ApikeyRequests>();
		List<PartnerPolicyRequest> findAllRequests = partnerPolicyRequestRepository.findAll();		
		if(!findAllRequests.isEmpty()) {
			Iterator<PartnerPolicyRequest> allRequests = findAllRequests.iterator();
			while(allRequests.hasNext()) {
				PartnerPolicyRequest nextRecord = allRequests.next();
				ApikeyRequests apikeyRequests = new ApikeyRequests();
				apikeyRequests.setApiKeyReqNo(nextRecord.getId());
				apikeyRequests.setOrganizationName(nextRecord.getPartner().getName());
				apikeyRequests.setPartnerID(nextRecord.getPartner().getId());
				apikeyRequests.setPolicyDesc(nextRecord.getRequestDetail());
				apikeyRequests.setPolicyId(nextRecord.getPolicyId());
				apikeyRequests.setStatus(nextRecord.getStatusCode());
				response.add(apikeyRequests);
			}
		}else {
			throw new NoPartnerApiKeyRequestsException(
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}
		return response;			
	}

	@Override
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String apiKeyReqID) {
		Optional<PartnerPolicyRequest> findRequestById = partnerPolicyRequestRepository.findById(apiKeyReqID);
		ApikeyRequests apikeyRequest = new ApikeyRequests();
		if(!findRequestById.isEmpty()) {
			apikeyRequest.setApiKeyReqNo(findRequestById.get().getId());
			apikeyRequest.setOrganizationName(findRequestById.get().getPartner().getName());
			apikeyRequest.setPartnerID(findRequestById.get().getPartner().getId());
			apikeyRequest.setPolicyDesc(findRequestById.get().getRequestDetail());
			apikeyRequest.setPolicyId(findRequestById.get().getPolicyId());
			apikeyRequest.setStatus(findRequestById.get().getStatusCode());			
		}else{			throw new NoPartnerApiKeyRequestsException(
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}
		return apikeyRequest;
	}

	@Override
	public PartnersPolicyMappingResponse approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			ActivateDeactivatePartnerRequest request, String partnerKeyReqId) {
		PartnerPolicyRequest partnerPolicyRequest = null;		
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<PartnerPolicyRequest> partnerPolicyRequestFromDb = partnerPolicyRequestRepository
				.findById(partnerKeyReqId);
		if (!partnerPolicyRequestFromDb.isPresent()) {
			throw new PartnerAPIDoesNotExistException(
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (partnerPolicyRequestFromDb.get().getStatusCode().equalsIgnoreCase(APPROVED)) {
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_APPROVED.getErrorCode(),
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_APPROVED.getErrorMessage());

		}
		if (partnerPolicyRequestFromDb.get().getStatusCode().equalsIgnoreCase(REJECTED)) {
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_REJECTED.getErrorCode(),
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_REJECTED.getErrorMessage());
		}

		partnerPolicyRequest = partnerPolicyRequestFromDb.get();
		if ((request.getStatus().equalsIgnoreCase(APPROVED) || request.getStatus().equalsIgnoreCase(REJECTED))) {
			partnerPolicyRequest.setStatusCode(request.getStatus());
		} else {
			LOGGER.info(request.getStatus() + " : Invalid Input Parameter (status should be Approved/Rejected)");
			throw new InvalidInputParameterException(InvalidInputParameterConstant.INVALID_STATUS_CODE.getErrorCode(),
					InvalidInputParameterConstant.INVALID_STATUS_CODE.getErrorMessage());
		}
		if (Arrays.stream(biometricExtractorsRequiredPartnerTypes.split(",")).anyMatch(partnerPolicyRequestFromDb.get().getPartner().getPartnerTypeCode()::equalsIgnoreCase)) {			
			List<BiometricExtractorProvider> extractorsFromDb = extractorProviderRepository.findByPartnerAndPolicyId(
					partnerPolicyRequestFromDb.get().getPartner().getId(),
					partnerPolicyRequestFromDb.get().getPolicyId());
			if (extractorsFromDb.isEmpty()) {
				throw new InvalidInputParameterException(
						InvalidInputParameterConstant.EXTRACTORS_NOT_PRESENT.getErrorCode(),
						InvalidInputParameterConstant.EXTRACTORS_NOT_PRESENT.getErrorMessage());
			}
		}
		partnerPolicyRequest.setUpdBy(getUser());
		partnerPolicyRequest.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		if (request.getStatus().equalsIgnoreCase("Approved")) {
			LOGGER.info("Creating PartnerAPIKey");
			LOGGER.info("Partner_API_Key should be unique for same partner");
			PartnerPolicy partnerPolicy = new PartnerPolicy();
			String partnerAPIKey = PartnerUtil.createPartnerApiKey();
			partnerPolicy.setPolicyApiKey(partnerAPIKey);
			partnerPolicy.setPartner(partnerPolicyRequest.getPartner());
			partnerPolicy.setPolicyId(partnerPolicyRequestFromDb.get().getPolicyId());
			partnerPolicy.setIsActive(true);
			partnerPolicy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now()));
			partnerPolicy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(partnerPolicyExpiryInDays)));
			partnerPolicy.setCrBy(partnerPolicyRequest.getCrBy());
			partnerPolicy.setCrDtimes(partnerPolicyRequest.getCrDtimes());
			partnerPolicyRepository.save(partnerPolicy);
		}
		partnersPolicyMappingResponse.setMessage("PartnerAPIKey Updated successfully");
		return partnersPolicyMappingResponse;
	}

	/**
	 * (One Partner will have only one policy) This method retrieves the policy json
	 * file for a partner. 1. Validated the partner 2. Checks the json file.
	 */
	@Override
	public PartnerPolicyResponse getPartnerMappedPolicyFile(String mispLicenseKey, String policy_api_key,
			String partnerId, boolean needPartnerCert) {
		LOGGER.info("Getting the partner from db.");
		PartnerPolicyResponse response = new PartnerPolicyResponse();
		MISPValidatelKeyResponseDto licenseKeyresponse = validateLicenseKey(mispLicenseKey);
		if (!licenseKeyresponse.isActive()) {
			throw new PartnerValidationException(PartnerValidationsConstants.MISP_IS_BLOCKED.getErrorCode(),
					PartnerValidationsConstants.MISP_IS_BLOCKED.getErrorMessage());
		}
		if (!licenseKeyresponse.isValid()) {
			throw new PartnerValidationException(PartnerValidationsConstants.MISP_LICENSE_KEY_EXPIRED.getErrorCode(),
					PartnerValidationsConstants.MISP_LICENSE_KEY_EXPIRED.getErrorMessage());
		}
		Optional<MISPEntity> mispFromDb = mispRepository.findById(licenseKeyresponse.getMisp_id());
		if(mispFromDb.isEmpty()) {
			Optional<Partner> mispPartner = partnerRepository.findById(licenseKeyresponse.getMisp_id());
			if(!mispPartner.get().getIsActive()) {
				throw new PartnerValidationException(PartnerValidationsConstants.MISP_IS_BLOCKED.getErrorCode(),
						PartnerValidationsConstants.MISP_IS_BLOCKED.getErrorMessage());				
			}
		}else if (!mispFromDb.get().getIsActive()) {
			throw new PartnerValidationException(PartnerValidationsConstants.MISP_IS_BLOCKED.getErrorCode(),
					PartnerValidationsConstants.MISP_IS_BLOCKED.getErrorMessage());
		}
		Optional<Partner> partner = partnerRepository.findById(partnerId);
		if (!partner.isPresent()) {
			throw new PartnerValidationException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (!partner.get().getIsActive()) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		if(needPartnerCert && partner.get().getCertificateAlias() == null) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_CERTIFICATE_NOT_EXISTS.getErrorCode(),
					PartnerValidationsConstants.PARTNER_CERTIFICATE_NOT_EXISTS.getErrorMessage());			
		}		
		if(needPartnerCert) {
			response.setCertificateData(getPartnerCertificate(partner.get().getCertificateAlias()));
		}
		PartnerPolicy partnerPolicy = partnerPolicyRepository.findByApiKey(policy_api_key);
		if (partnerPolicy == null) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorMessage());
		}
		if (!partnerPolicy.getPartner().getId().equals(partner.get().getId())) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorMessage());

		}
		if (!partnerPolicy.getIsActive()) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		if (partnerPolicy.getValidToDatetime().before(Timestamp.valueOf(LocalDateTime.now()))) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorMessage());
		}
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(partnerPolicy.getPolicyId());
		if (!authPolicy.isPresent()) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorMessage());
		}
		if (!authPolicy.get().getIsActive()) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		if (authPolicy.get().getPolicyGroup() == null) {
			throw new PartnerValidationException(PartnerValidationsConstants.POLICY_GROUP_NOT_EXISTS.getErrorCode(),
					PartnerValidationsConstants.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
		}
		if (!authPolicy.get().getPolicyGroup().getIsActive()) {
			throw new PartnerValidationException(PartnerValidationsConstants.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					PartnerValidationsConstants.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		if (authPolicy.get().getValidToDate().isBefore(LocalDateTime.now())) {
			throw new PartnerValidationException(
					PartnerValidationsConstants.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorCode(),
					PartnerValidationsConstants.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorMessage());
		}		
		response.setPolicyId(partnerPolicy.getPolicyId());
		response.setPolicyDescription(authPolicy.get().getPolicyGroup().getDescr());
		response.setPolicy(getAuthPolicies(authPolicy.get().getPolicyFileId(), authPolicy.get().getId()));
		response.setPolicyStatus(authPolicy.get().getIsActive());
		response.setPartnerId(partnerPolicy.getPartner().getId());
		response.setPartnerName(partnerPolicy.getPartner().getName());
		response.setPolicyName(authPolicy.get().getName());
		response.setMispExpiresOn(toISOFormat(licenseKeyresponse.getValidTo()));
		response.setPolicyExpiresOn(toISOFormat(authPolicy.get().getValidToDate()));
		response.setApiKeyExpiresOn(toISOFormat(partnerPolicy.getValidToDatetime().toLocalDateTime()));
		return response;
	}

	@SuppressWarnings("unchecked")
	private PolicyDTO getAuthPolicies(String policeFileId, String authPolicyId) {
		Policies policies = new Policies();
		PolicyDTO authPolicies = new PolicyDTO();
		Map<?, ?> readValue = null;
		try {
			readValue = new ObjectMapper().readValue(policeFileId, Map.class);
		} catch (Exception e) {
			LOGGER.info("Error occured while parsing the policy file" + e.getStackTrace());
			throw new PartnerValidationException(PartnerValidationsConstants.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					PartnerValidationsConstants.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		policies.setAuthPolicies((List<AuthPolicyAttributes>) readValue.get("allowedAuthTypes"));
		policies.setAllowedKycAttributes((List<KYCAttributes>) readValue.get("allowedKycAttributes"));
		policies.setAuthTokenType((String) readValue.get("authTokenType"));
		authPolicies.setPolicies(policies);
		authPolicies.setPolicyId(authPolicyId);
		return authPolicies;
	}

	/**
	 * This method validates the license key.
	 * 
	 * @param licenseKey
	 * @return
	 */
	public MISPValidatelKeyResponseDto validateLicenseKey(String licenseKey) {
		MISPValidatelKeyResponseDto response = new MISPValidatelKeyResponseDto();
		LOGGER.info("Getting the misp license key " + licenseKey);
		MISPLicenseEntity mispLicense = getLicenseDetails(licenseKey);
		response.setActive(mispLicense.getIsActive());
		response.setLicenseKey(mispLicense.getMispLicenseUniqueKey().getLicense_key());
		response.setValid(mispLicense.getValidToDate().isAfter(LocalDateTime.now()));
		response.setValidFrom(mispLicense.getValidFromDate());
		response.setValidTo(mispLicense.getValidToDate());
		response.setMisp_id(mispLicense.getMispLicenseUniqueKey().getMisp_id());
		String message;
		if (response.isValid()) {
			message = "Valid";
		} else {
			message = "Expired";
		}
		response.setMessage("MISP " + mispLicense.getMispLicenseUniqueKey().getMisp_id() + " with license key "
				+ mispLicense.getMispLicenseUniqueKey().getLicense_key() + "  is " + message);
		return response;
	}

	/**
	 * This method retrieves the license details with license key.
	 * 
	 * @param licenseKey
	 * @return
	 */
	private MISPLicenseEntity getLicenseDetails(String licenseKey) {
		LOGGER.info("Retrieving the misp license key " + licenseKey);
		MISPLicenseEntity mispLicense = null;
		try {
			mispLicense = misplKeyRepository.findByLicensekey(licenseKey);
			if (mispLicense == null) {
				LOGGER.warn("No details found for license key " + licenseKey);
				throw new PartnerValidationException(
						PartnerValidationsConstants.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
						PartnerValidationsConstants.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage() + " " + licenseKey);
			}
		} catch (Exception e) {
			LOGGER.warn("No details found for license key " + licenseKey + e.getStackTrace());
			throw new PartnerValidationException(PartnerValidationsConstants.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
					PartnerValidationsConstants.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage() + " " + licenseKey);
		}

		return mispLicense;
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
	
	/**
	 * 
	 * @param updatedObject
	 * @param isApiKeyUpdated
	 */
	private void notify(String updatedObject,boolean isApiKeyUpdated) {
		Type type = new Type();
		type.setName("PartnerManagementServiceImpl");
		type.setNamespace("io.mosip.pmp.misp.service");
		Map<String,Object> data = new HashMap<>();
		String updatedParam = (isApiKeyUpdated == true) ? "apiKey" : "partnerId";
		data.put(updatedParam, updatedObject);
		EventType event = (isApiKeyUpdated == true)?EventType.APIKEY_UPDATED:EventType.PARTNER_UPDATED;
		webSubPublisher.notify(event,data,type);
	}
	
	@SuppressWarnings("unchecked")
	private String getPartnerCertificate(String certificateAlias) {
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", certificateAlias);
		Map<String, Object> getApiResponse = restUtil
				.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		PartnerCertDownloadResponeDto responseObject = null;
		try {
			responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")),
					PartnerCertDownloadResponeDto.class);
		} catch (IOException e) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
		}
		if (responseObject == null && getApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse.get(ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),
						certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			} else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
		
		return responseObject.getCertificateData();
	}
	
	private static LocalDateTime toISOFormat(LocalDateTime localDateTime) {
		ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.systemDefault());
		ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.UTC);
		return converted.toLocalDateTime();
	}
}
