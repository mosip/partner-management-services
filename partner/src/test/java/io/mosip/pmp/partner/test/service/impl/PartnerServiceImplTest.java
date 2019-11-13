package io.mosip.pmp.partner.test.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.mosip.pmp.partner.PartnerserviceApplication;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.entity.AuthPolicy;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PartnerPolicyRequest;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.APIKeyReqIdStatusInProgressException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyIsNotCreatedException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyReqIDDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistsException;
import io.mosip.pmp.partner.exception.PolicyGroupDoesNotExistException;
import io.mosip.pmp.partner.repository.AuthPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.partner.repository.PolicyGroupRepository;
import io.mosip.pmp.partner.service.impl.PartnerServiceImpl;

/**
 * @author sanjeev.shrivastava
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerserviceApplication.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class PartnerServiceImplTest {
	private PartnerServiceImpl pserviceImpl;

	@Mock
	PolicyGroupRepository policyGroupRepository;
	@Mock
	PartnerServiceRepository partnerRepository;
	@Mock
	AuthPolicyRepository authPolicyRepository;
	@Mock
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;
	@Mock
	PartnerPolicyRepository partnerPolicyRepository;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		pserviceImpl = new PartnerServiceImpl();
		ReflectionTestUtils.setField(pserviceImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(pserviceImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerPolicyRepository", partnerPolicyRepository);

	}

	/*@Test
	public void savePartnerTest() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createPartnerRequest();
		List<Partner> list = new ArrayList<>();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(list);
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);

	}*/

	@Test(expected = PolicyGroupDoesNotExistException.class)
	public void throwExceptionWhenPartnerPolicyGroupIsNullTest() {
		PartnerRequest partnerRequest = createPartnerRequest();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(null);
		pserviceImpl.savePartner(partnerRequest);

	}

	/*@Test(expected = PartnerAlreadyRegisteredException.class)
	public void throwExceptionWhenPartnerNameAlreadyRegisteredTest() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.TRUE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName(Mockito.anyString())).thenReturn(createlistOfPartner(new Partner()));
		pserviceImpl.savePartner(partnerRequest);

	}*/

	/*@Test
	public void savePartnerWhenPartnerListIsEmptyTest() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.TRUE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName(Mockito.anyString())).thenReturn(Mockito.anyList());
		pserviceImpl.savePartner(partnerRequest);

	}*/

	@Test
	public void getPartnerDetailsTest() {
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> policyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		Mockito.when(policyGroupRepository.findById(partner.get().getPolicyGroupId())).thenReturn(policyGroup);
		RetrievePartnerDetailsResponse partnerDetails = pserviceImpl.getPartnerDetails("12345");
		assertNotNull(partnerDetails);
		assertEquals(partnerDetails.getContactNumber(), "47384384");
		assertEquals(partnerDetails.getEmailId(), "xyz@hotmail.com");
		assertEquals(partnerDetails.getAddress(), "address");
	}

	@Test(expected = PartnerDoesNotExistsException.class)
	public void throwExceptionWhenPartnerDetailsIsEmptyTest() {
		Optional<Partner> partner = Optional.empty();
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		pserviceImpl.getPartnerDetails("12345");
	}

	@Test
	public void updatePartnerDetailsTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "id");
		assertEquals(updatePartnerDetail.getStatus(), "Active");

	}

	/*@Test
	public void doNotSetstatusWhenPartnerIsDeactiveTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.FALSE));
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "id");
		assertNull(updatePartnerDetail.getStatus());
	}*/

	@Test(expected = PartnerDoesNotExistException.class)
	public void doNotUpdaePartnerWhenPartnerDetailsIsEmptyTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.empty();
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
	}

	@Test
	public void submitPartnerApiKeyReqTest() {
		String partnerId = "12345";
		String auth_policy_id = "2223232";
		Optional<AuthPolicy> findByAuthId = Optional.empty();
		PartnerAPIKeyRequest request = createPartnerAPIKeyRequest();
		Optional<Partner> partner = Optional.of(createPartner(Boolean.FALSE));
		Mockito.when(policyGroupRepository.findByName(request.getPolicyName()))
				.thenReturn(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(partnerPolicyRequestRepository.save(Mockito.any())).thenReturn(Mockito.any());
		Mockito.when(authPolicyRepository.findById(auth_policy_id)).thenReturn(findByAuthId);
		PartnerAPIKeyResponse submitPartnerApiKeyReq = pserviceImpl.submitPartnerApiKeyReq(request, partnerId);
		assertNotNull(submitPartnerApiKeyReq);
		assertEquals(submitPartnerApiKeyReq.getMessage(), "partnerAPIKeyRequest successfully created");
	}

	@Test
	public void doNotSubmitPartnerApiKeyReqWhenAuthPolicyIsPresentTest() {
		String partnerId = "12345";
		String auth_policy_id = "2223232";
		Optional<AuthPolicy> findByAuthId = Optional.of(createAuthPolicy());
		PartnerAPIKeyRequest request = createPartnerAPIKeyRequest();
		Optional<Partner> partner = Optional.of(createPartner(Boolean.FALSE));
		Mockito.when(policyGroupRepository.findByName(request.getPolicyName()))
				.thenReturn(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(partnerPolicyRequestRepository.save(Mockito.any())).thenReturn(Mockito.any());
		Mockito.when(authPolicyRepository.findById(auth_policy_id)).thenReturn(findByAuthId);
		PartnerAPIKeyResponse submitPartnerApiKeyReq = pserviceImpl.submitPartnerApiKeyReq(request, partnerId);
		assertNotNull(submitPartnerApiKeyReq);
		assertEquals(submitPartnerApiKeyReq.getMessage(), "partnerAPIKeyRequest successfully created");
	}

	/*@Test(expected = PartnerDoesNotExistsException.class)
	public void throwExceptionWhenPartnerNotFoundByPartnerIdTest() {
		String partnerId = "12345";
		PartnerAPIKeyRequest request = createPartnerAPIKeyRequest();
		Optional<Partner> partner = Optional.empty();
		Mockito.when(policyGroupRepository.findByName(request.getPolicyName())).thenReturn(null);
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.submitPartnerApiKeyReq(request, partnerId);
	}*/

	@Test
	public void downloadPartnerAPIkeyTest() {
		String partnerID = "id";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partner_request = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(createPartnerPolicy());
		DownloadPartnerAPIkeyResponse downloadPartnerAPIkey = pserviceImpl.downloadPartnerAPIkey(partnerID,
				aPIKeyReqID);
		assertNotNull(downloadPartnerAPIkey);
		assertEquals(downloadPartnerAPIkey.getPartnerAPIKey(), createPartnerPolicy().getPolicyApiKey());
	}

	@Test(expected = PartnerAPIKeyIsNotCreatedException.class)
	public void throwExceptionWhenPartnerPolicyNotFoundByPartnerIdTest() {
		String partnerID = "id";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partner_request = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(null);
		pserviceImpl.downloadPartnerAPIkey(partnerID, aPIKeyReqID);
	}

	@Test(expected = PartnerDoesNotExistsException.class)
	public void throwExceptionWhenPolicyRequestIdNotEqualsPartnerIdTest() {
		String partnerID = "pid";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partner_request = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		pserviceImpl.downloadPartnerAPIkey(partnerID, aPIKeyReqID);
	}

	@Test(expected = PartnerAPIKeyReqIDDoesNotExistException.class)
	public void throwExceptionWhenPartnerPolicyRequestNotFoundByAPIKeyReqIDTest() {
		String partnerID = "id";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partner_request = Optional.empty();
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		pserviceImpl.downloadPartnerAPIkey(partnerID, aPIKeyReqID);
	}

	@Test
	public void viewApiKeyRequestStatusApiKeyTest() {
		String partnerID = "id";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.of(createPartnerPolicyRequest("Approved"));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(createPartnerPolicy());
		APIkeyRequests viewApiKeyRequestStatusApiKey = pserviceImpl.viewApiKeyRequestStatusApiKey(partnerID,
				aPIKeyReqID);
		assertNotNull(viewApiKeyRequestStatusApiKey);
		assertEquals(viewApiKeyRequestStatusApiKey.getApiKeyRequestStatus(), "Approved");
		assertEquals(viewApiKeyRequestStatusApiKey.getPartnerApiKey(), "partnerAPIKey_1");
	}

	@Test(expected = PartnerDoesNotExistsException.class)
	public void throwExceptionWhenPartnerPolicyIdNotEqualsPartnerIdTest() {
		String partnerID = "pid";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.viewApiKeyRequestStatusApiKey(partnerID, aPIKeyReqID);
	}

	@Test(expected = APIKeyReqIdStatusInProgressException.class)
	public void throwExceptionWhenPartnerPolicyStatusIsNotApprovedTest() {
		String partnerID = "id";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.of(createPartnerPolicyRequest("Rejected"));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.viewApiKeyRequestStatusApiKey(partnerID, aPIKeyReqID);
	}

	@Test(expected = PartnerAPIKeyReqIDDoesNotExistException.class)
	public void throwExceptionWhenPartnerPolicyRequestIsNotFoundByAPIKeyReqIDTest() {
		String partnerID = "pid";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.empty();
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.viewApiKeyRequestStatusApiKey(partnerID, aPIKeyReqID);
	}
	
	/*@Test
	public void retrieveAllApiKeyRequestsSubmittedByPartnerTest() {
		String partnerID = "id";
		List<PartnerPolicyRequest> partnerPolicyRequest = new ArrayList<>();
		partnerPolicyRequest.add(createPartnerPolicyRequest("approved"));
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerID)).thenReturn(partnerPolicyRequest);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(createPartnerPolicy());
		PartnersRetrieveApiKeyRequests result = pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
		assertNotNull(result);
		assertEquals(result.getAPIkeyRequests().get(0).getPartnerApiKey(), "partnerAPIKey_1");
		assertEquals(result.getAPIkeyRequests().get(0).getApiKeyRequestStatus(), "approved");
	}*/
	
	/*@Test
	public void retrieveAllApiKeyRequestsSubmittedByPartnerWhenStatusIsNotApprovedTest() {
		String partnerID = "id";
		List<PartnerPolicyRequest> partnerPolicyRequest = new ArrayList<>();
		partnerPolicyRequest.add(createPartnerPolicyRequest("Rejected"));
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerID)).thenReturn(partnerPolicyRequest);
		PartnersRetrieveApiKeyRequests result = pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
		assertNotNull(result);
		assertEquals(result.getAPIkeyRequests().get(0).getApiKeyRequestStatus(), "Rejected");
	}*/
	
	/*@Test(expected = PartnerAPIKeyIsNotCreatedException.class)
	public void throwExceptionWhenPartnerPolicyIsNotFoundByPartnerIdTest() {
		String partnerID = "id";
		List<PartnerPolicyRequest> partnerPolicyRequest = new ArrayList<>();
		partnerPolicyRequest.add(createPartnerPolicyRequest("approved"));
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerID)).thenReturn(partnerPolicyRequest);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(null);
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
	}*/
	
	@Test(expected = PartnerDoesNotExistsException.class)
	public void throwExceptionWhenPartnerPolicyRequestIsEmptyTest() {
		String partnerID = "id";
		List<PartnerPolicyRequest> partnerPolicyRequest = new ArrayList<>();
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
		
	}

	private PartnerPolicy createPartnerPolicy() {
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey("partnerAPIKey_1");
		return partnerPolicy;
	}

	private PartnerPolicyRequest createPartnerPolicyRequest(String statusCode) {
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(createPartner(Boolean.TRUE));
		partnerPolicyRequest.setStatusCode(statusCode);
		return partnerPolicyRequest;
	}

	private AuthPolicy createAuthPolicy() {
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		return authPolicy;

	}

	private PartnerAPIKeyRequest createPartnerAPIKeyRequest() {
		PartnerAPIKeyRequest partnerAPIKeyRequest = new PartnerAPIKeyRequest();
		partnerAPIKeyRequest.setPolicyName("policyName");
		partnerAPIKeyRequest.setUseCaseDescription("useCaseDescription");
		return partnerAPIKeyRequest;
	}

	private PartnerUpdateRequest createPartnerUpdateRequest() {
		PartnerUpdateRequest partnerUpdateRequest = new PartnerUpdateRequest();
		partnerUpdateRequest.setAddress("address");
		partnerUpdateRequest.setContactNumber("87878787");
		partnerUpdateRequest.setEmailId("xyz@facebook.com");
		partnerUpdateRequest.setOrganizationName("airtel");
		return partnerUpdateRequest;
	}

	private Partner createPartner(Boolean isActive) {
		Partner partner = new Partner();
		partner.setId("id");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("policyGroupId");
		partner.setIsActive(isActive);
		return partner;
	}

	private List<Partner> createlistOfPartner(Partner partner) {
		List<Partner> list = new ArrayList<>();
		list.add(partner);
		return list;
	}

	private PartnerRequest createPartnerRequest() {
		PartnerRequest prequest = new PartnerRequest();
		prequest.setAddress("addresss-1");
		prequest.setContactNumber("8273283283");
		prequest.setEmailId("xyz@gmail.com");
		prequest.setOrganizationName("airtel India");
		prequest.setPolicyGroup("Telecom sector");
		return prequest;
	}

	private PolicyGroup createPolicyGroup(Boolean isActive) {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("12133");
		policyGroup.setIsActive(isActive);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		return policyGroup;
	}

}