package io.mosip.pmp.partnermanagement.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pmp.partnermanagement.constant.InvalidInputParameterConstant;
import io.mosip.pmp.partnermanagement.constant.NoPartnerApiKeyRequestsConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIKeyDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerIdDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerValidationsConstants;
import io.mosip.pmp.partnermanagement.controller.PartnerManagementController;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.AuthPolicyAttributes;
import io.mosip.pmp.partnermanagement.dto.KYCAttributes;
import io.mosip.pmp.partnermanagement.dto.MISPValidatelKeyResponseDto;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnerPolicyResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.Policies;
import io.mosip.pmp.partnermanagement.dto.PolicyDTO;
import io.mosip.pmp.partnermanagement.dto.PolicyIDResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerManagers;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersManagersDetails;
import io.mosip.pmp.partnermanagement.entity.AuthPolicy;
import io.mosip.pmp.partnermanagement.entity.MISPEntity;
import io.mosip.pmp.partnermanagement.entity.MISPLicenseEntity;
import io.mosip.pmp.partnermanagement.entity.Partner;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicyRequest;
import io.mosip.pmp.partnermanagement.entity.PolicyGroup;
import io.mosip.pmp.partnermanagement.exception.InvalidInputParameterException;
import io.mosip.pmp.partnermanagement.exception.NoPartnerApiKeyRequestsException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIKeyDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException;
import io.mosip.pmp.partnermanagement.exception.PartnerIdDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerValidationException;
import io.mosip.pmp.partnermanagement.exception.PolicyNotExistException;
import io.mosip.pmp.partnermanagement.repository.AuthPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.MispLicenseKeyRepository;
import io.mosip.pmp.partnermanagement.repository.MispServiceRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerRepository;
import io.mosip.pmp.partnermanagement.repository.PolicyGroupRepository;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;
import io.mosip.pmp.partnermanagement.util.PartnerUtil;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
public class PartnerManagementServiceImpl implements PartnerManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerManagementController.class);

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
	AuthPolicyRepository authPolicyRepository;

	@Autowired
	MispServiceRepository mispRepository;

	@Value("${mosip.pmp.partner.policy.expiry.period.indays}")
	private int partnerPolicyExpiryInDays;

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
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID,
			ActivateDeactivatePartnerRequest request) {
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		LocalDateTime now = LocalDateTime.now();
		if (findById.isPresent()) {
			Partner partner = findById.get();
			String partnerStatus = request.getStatus();

			if (partnerStatus.equalsIgnoreCase("Active") || partnerStatus.equalsIgnoreCase("De-Active")) {
				if (partnerStatus.equalsIgnoreCase("Active")) {
					partner.setIsActive(true);
				} else {
					partner.setIsActive(false);
				}
			} else {
				LOGGER.info(partnerStatus + " : is Invalid Input Parameter, it should be (Active/De-Active)");
				throw new InvalidInputParameterException(
						InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorCode(),
						InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorMessage());
			}
			partner.setUpdBy(getUser());
			partner.setUpdDtimes(Timestamp.valueOf(now));
			partnerRepository.save(partner);
		} else {
			LOGGER.info(partnerID + " : Partner Id Does Not Exist");
			throw new PartnerIdDoesNotExistException(
					PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		partnersPolicyMappingResponse.setMessage("Partner status updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID,
			ActivateDeactivatePartnerRequest request, String partnerAPIKey) {
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(partnerAPIKey);
		LocalDateTime now = LocalDateTime.now();
		if (findById.isPresent()) {
			PartnerPolicy partnerPolicy = findById.get();
			if (partnerPolicy.getPartner().getId().equals(partnerID)) {

				if (request.getStatus().equalsIgnoreCase("Active")
						|| request.getStatus().equalsIgnoreCase("De-Active")) {
					if (request.getStatus().equalsIgnoreCase("Active")) {
						partnerPolicy.setIsActive(true);
					} else {
						partnerPolicy.setIsActive(false);
					}
				} else {
					LOGGER.info("Invalid Input Parameter, it should be (Active/De-Active)");
					throw new InvalidInputParameterException(
							InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorCode(),
							InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorMessage());
				}
				partnerPolicy.setUpdBy(getUser());
				partnerPolicy.setUpdDtimes(Timestamp.valueOf(now));
				partnerPolicyRepository.save(partnerPolicy);
				LOGGER.info(partnerAPIKey + " : API KEY Status Updated Successfully");
			} else {
				LOGGER.info(partnerID + " : Partner Id Does Not Exist");
				throw new PartnerValidationException(
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			LOGGER.info(partnerAPIKey + " : partnerAPIKey Does Not Exist");
			throw new PartnerAPIKeyDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner API Key status updated successfully");
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
			retrievePartnersDetails.setStatus(partner.getIsActive() == true ? "Active" : "De-Active");
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
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
		if (findById.isPresent()) {
			Partner partner = findById.get();

			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails.setStatus(partner.getIsActive() == true ? "Active" : "De-Active");
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
		AuthPolicy authPolicy = null;
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(PartnerAPIKey);
		PartnerAPIKeyToPolicyMappingsResponse response = new PartnerAPIKeyToPolicyMappingsResponse();
		if (findById.isPresent()) {
			PartnerPolicy partnerPolicy = findById.get();
			if (partnerPolicy.getPartner().getId().equals(partnerID)) {
				response.setPartnerID(partnerID);
				String authId = partnerPolicy.getPolicyId();
				Optional<AuthPolicy> findByAuthId = authPolicyRepository.findById(authId);
				if (findByAuthId.isPresent()) {
					authPolicy = findByAuthId.get();
					response.setPolicyId(authPolicy.getPolicyGroup().getId());
				} else {
					throw new PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException(
							PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant.PARTNER_API_DOES_NOT_BELONGS_TO_THE_POLICYGROUP_OF_PARTNERMANAGER_EXCEPTION
									.getErrorCode(),
							PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant.PARTNER_API_DOES_NOT_BELONGS_TO_THE_POLICYGROUP_OF_PARTNERMANAGER_EXCEPTION
									.getErrorMessage());
				}
			} else {
				LOGGER.info(partnerID + " : Partner Not Exist Exception");
				throw new PartnerIdDoesNotExistException(
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			LOGGER.info(PartnerAPIKey + " : Partner API KEY Not Exist");
			throw new PartnerAPIDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return response;
	}

	@Override
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers() {

		List<ApikeyRequests> requests = new ArrayList<ApikeyRequests>();

		List<PartnerPolicyRequest> findAll = partnerPolicyRequestRepository.findAll();

		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();

		String parnerId = null;
		PolicyGroup policyGroup = null;
		if (!findAll.isEmpty()) {
			Iterator<PartnerPolicyRequest> partnerPolicyRequestIterat = findAll.iterator();
			while (partnerPolicyRequestIterat.hasNext()) {
				ApikeyRequests apikeyRequests = new ApikeyRequests();
				partnerPolicyRequest = partnerPolicyRequestIterat.next();
				parnerId = partnerPolicyRequest.getPartner().getId();
				apikeyRequests.setPartnerID(parnerId);
				apikeyRequests.setStatus(partnerPolicyRequest.getStatusCode());

				Optional<Partner> findByPartnerId = partnerRepository.findById(parnerId);

				if (!findByPartnerId.isPresent()) {
					throw new PartnerValidationException(
							PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
							PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
				}

				Partner partner = findByPartnerId.get();

				apikeyRequests.setOrganizationName(partner.getName());

				String policyGroupId = partner.getPolicyGroupId();
				Optional<PolicyGroup> findById = policyGroupRepository.findById(policyGroupId);
				if (findById.isPresent()) {
					policyGroup = findById.get();
				}

				if (policyGroup != null) {
					apikeyRequests.setPolicyName(policyGroup.getName());
					apikeyRequests.setPolicyDesc(policyGroup.getDescr());
					apikeyRequests.setApiKeyReqNo(partnerPolicyRequest.getId());
					requests.add(apikeyRequests);
				}
			}
		} else {
			throw new NoPartnerApiKeyRequestsException(
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}

		return requests;
	}

	@Override
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String apiKeyReqID) {
		Partner partner = null;
		PolicyGroup policyGroup = null;
		ApikeyRequests apikeyRequests = new ApikeyRequests();
		Optional<PartnerPolicyRequest> findById = partnerPolicyRequestRepository.findById(apiKeyReqID);
		if (findById.isPresent()) {
			PartnerPolicyRequest partnerPolicyRequest = findById.get();

			String partnerId = partnerPolicyRequest.getPartner().getId();
			String policyId = partnerPolicyRequest.getPolicyId();

			Optional<Partner> findByPartnerId = partnerRepository.findById(partnerId);
			if (findByPartnerId.isPresent()) {
				partner = findByPartnerId.get();
			}

			Optional<PolicyGroup> findByPolicyId = policyGroupRepository.findById(policyId);

			if (findByPolicyId.isPresent()) {
				policyGroup = findByPolicyId.get();
			}

			if (partner != null & policyGroup != null) {
				apikeyRequests.setPartnerID(partner.getId());
				apikeyRequests.setStatus(partnerPolicyRequest.getStatusCode());
				apikeyRequests.setOrganizationName(partner.getName());
				apikeyRequests.setPolicyName(policyGroup.getName());
				apikeyRequests.setPolicyDesc(policyGroup.getDescr());
				apikeyRequests.setApiKeyReqNo(partnerPolicyRequest.getId());
			}

		} else {
			throw new NoPartnerApiKeyRequestsException(
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}

		return apikeyRequests;
	}

	@Override
	public PartnersPolicyMappingResponse approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			ActivateDeactivatePartnerRequest request, String partnerKeyReqId) {
		PartnerPolicyRequest partnerPolicyRequest = null;
		LocalDateTime now = LocalDateTime.now();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<PartnerPolicyRequest> findById = partnerPolicyRequestRepository.findById(partnerKeyReqId);
		if (!findById.isPresent()) {
			throw new PartnerAPIDoesNotExistException(
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (findById.get().getStatusCode().equalsIgnoreCase("Approved")) {
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_APPROVED.getErrorCode(),
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_APPROVED.getErrorMessage());

		}
		if (findById.get().getStatusCode().equalsIgnoreCase("Rejected")) {
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_REJECTED.getErrorCode(),
					InvalidInputParameterConstant.POLICY_REQUEST_ALREADY_REJECTED.getErrorMessage());
		}
		partnerPolicyRequest = findById.get();
		if ((request.getStatus().equalsIgnoreCase("Approved") || request.getStatus().equalsIgnoreCase("Rejected"))) {
			partnerPolicyRequest.setStatusCode(request.getStatus());
		} else {
			LOGGER.info(request.getStatus() + " : Invalid Input Parameter (status should be Approved/Rejected)");
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorCode(),
					InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorMessage());
		}
		partnerPolicyRequest.setUpdBy(getUser());
		partnerPolicyRequest.setUpdDtimes(Timestamp.valueOf(now));
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		if (request.getStatus().equalsIgnoreCase("Approved")) {
			LOGGER.info("Creating PartnerAPIKey");
			LOGGER.info("Partner_API_Key should be unique for same partner");
			PartnerPolicy partnerPolicy = new PartnerPolicy();
			String partnerAPIKey = PartnerUtil.createPartnerApiKey();
			partnerPolicy.setPolicyApiKey(partnerAPIKey);
			partnerPolicy.setPartner(partnerPolicyRequest.getPartner());
			AuthPolicy findByPolicyId = authPolicyRepository.findByPolicyId(findById.get().getPolicyId());
			if (findByPolicyId != null) {
				partnerPolicy.setPolicyId(findByPolicyId.getId());
			}
			partnerPolicy.setIsActive(true);
			partnerPolicy.setValidFromDatetime(Timestamp.valueOf(now));
			partnerPolicy.setValidToDatetime(Timestamp.valueOf(now.plusDays(partnerPolicyExpiryInDays)));
			partnerPolicy.setCrBy(partnerPolicyRequest.getCrBy());
			partnerPolicy.setCrDtimes(partnerPolicyRequest.getCrDtimes());
			partnerPolicyRepository.save(partnerPolicy);
		}
		partnersPolicyMappingResponse.setMessage("PartnerAPIKey Updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public RetrievePartnerManagers getPartnerManager() {
		RetrievePartnerManagers partnersResponse = new RetrievePartnerManagers();
		List<RetrievePartnersManagersDetails> partners = new ArrayList<>();
		List<Partner> listPart = partnerRepository.findAll();
		Partner partner = null;
		if (listPart.isEmpty()) {
			throw new PartnerValidationException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Iterator<Partner> partnerIterat = listPart.iterator();
		while (partnerIterat.hasNext()) {
			RetrievePartnersManagersDetails retrievePartnersManagersDetails = new RetrievePartnersManagersDetails();
			partner = partnerIterat.next();
			retrievePartnersManagersDetails.setPartnerID(partner.getId());
			retrievePartnersManagersDetails.setPartnerStatus(partner.getIsActive() == true ? "Active" : "De-Active");
			retrievePartnersManagersDetails.setPolicyID(partner.getPolicyGroupId());
			Optional<PolicyGroup> findByIdpolicyGroup = policyGroupRepository.findById(partner.getPolicyGroupId());
			if (findByIdpolicyGroup.isPresent()) {
				retrievePartnersManagersDetails.setPolicyName(findByIdpolicyGroup.get().getName());
			}
			String statusCode = null;
			String aPIKeyReqID = null;

			if (!partnerPolicyRequestRepository.findByPartnerId(partner.getId()).isEmpty()) {
				statusCode = partnerPolicyRequestRepository.findByPartnerId(partner.getId()).get(0).getStatusCode();
				aPIKeyReqID = partnerPolicyRequestRepository.findByPartnerId(partner.getId()).get(0).getId();
			} else {
				statusCode = "YET TO SUBMIT";
				aPIKeyReqID = "NOT CREATED";
			}
			retrievePartnersManagersDetails.setApikeyReqIDStatus(statusCode);
			retrievePartnersManagersDetails.setApikeyReqID(aPIKeyReqID);

			String policyId = null;
			String policyIdStatus = null;

			PartnerPolicy partnerPolicy = null;
			partnerPolicy = partnerPolicyRepository.findByPartnerId(partner.getId());
			if (partnerPolicy != null) {
				policyId = partnerPolicy.getPolicyApiKey();
				policyIdStatus = partnerPolicy.getIsActive() == true ? "Active" : "De-Active";
			} else {
				policyId = "NOT CREATED";
				policyIdStatus = "UNDEFINED";
			}

			retrievePartnersManagersDetails.setPartnerAPIKey(policyId);
			retrievePartnersManagersDetails.setPartnerAPIKeyStatus(policyIdStatus);

			partners.add(retrievePartnersManagersDetails);
		}
		partnersResponse.setPartners(partners);
		return partnersResponse;
	}

	@Override
	public PolicyIDResponse getPartnerPolicyID(String policyName) {

		PolicyGroup policyGroup = null;
		PolicyIDResponse policyIDResponse = new PolicyIDResponse();
		LOGGER.info("validating the policy group");
		policyGroup = policyGroupRepository.findByName(policyName);
		if (policyGroup != null) {
			LOGGER.info(policyName + " : Policy Group is available for the partner");
			policyIDResponse.setPolicyID(policyGroup.getId());
		}
		return policyIDResponse;
	}

	/**
	 * (One Partner will have only one policy) This method retrieves the policy json
	 * file for a partner. 1. Validated the partner 2. Checks the json file.
	 */
	@Override
	public PartnerPolicyResponse getPartnerMappedPolicyFile(String mispLicenseKey, String policy_api_key,
			String partnerId) {
		LOGGER.info("Getting the partner from db.");
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
		if (!mispFromDb.get().getIsActive()) {
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

		PartnerPolicyResponse response = new PartnerPolicyResponse();
		response.setPolicyId(partnerPolicy.getPolicyId());
		response.setPolicyDescription(authPolicy.get().getPolicyGroup().getDescr());
		response.setPolicy(getAuthPolicies(authPolicy.get().getPolicyFileId(), authPolicy.get().getId()));
		response.setPolicyStatus(authPolicy.get().getIsActive());
		response.setPartnerId(partnerPolicy.getPartner().getId());
		response.setPartnerName(partnerPolicy.getPartner().getName());
		response.setPolicyName(authPolicy.get().getName());

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
		response.setLicenseKey(mispLicense.getMispUniqueEntity().getLicense_key());
		response.setValid(mispLicense.getValidToDate().isAfter(LocalDateTime.now()));
		response.setValidFrom(mispLicense.getValidFromDate());
		response.setValidTo(mispLicense.getValidToDate());
		response.setMisp_id(mispLicense.getMispUniqueEntity().getMisp_id());
		String message;
		if (response.isValid()) {
			message = "Valid";
		} else {
			message = "Expired";
		}
		response.setMessage("MISP " + mispLicense.getMispUniqueEntity().getMisp_id() + " with license key "
				+ mispLicense.getMispUniqueEntity().getLicense_key() + "  is " + message);
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
}
