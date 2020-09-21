package io.mosip.pmp.partner.test.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
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
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PartnerPolicyRequest;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.APIKeyReqIdStatusInProgressException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyIsNotCreatedException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyReqIDDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerAlreadyRegisteredException;
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
	
	@Test
	public void getPartnerDetailsWithName_Test(){
		RetrievePartnerDetailsWithNameResponse response = new RetrievePartnerDetailsWithNameResponse();
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> findByIdpolicyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Partner par = partner.get();
		PolicyGroup policyGroup = findByIdpolicyGroup.get();
		
		response.setId(par.getId());
		response.setAddress(par.getAddress());
		response.setContactNo(par.getContactNo());
		response.setCrBy(par.getCrBy());
		response.setCrDtimes(par.getCrDtimes());
		response.setEmailId(par.getEmailId());
		response.setIsActive(par.getIsActive());
		response.setName(par.getName());
		response.setUpdBy(par.getUpdBy());
		response.setUpdDtimes(par.getUpdDtimes());
		response.setUserId(par.getUserId());
		
		Mockito.when(partnerRepository.findByName(par.getName())).thenReturn(par);
		Mockito.when(policyGroupRepository.findById(par.getPolicyGroupId())).thenReturn(findByIdpolicyGroup);
		response.setPolicyGroupName(policyGroup.getName());
		pserviceImpl.getPartnerDetailsWithName(par.getName());
	}
	
	@Test(expected = PartnerDoesNotExistsException.class)
	public void getPartnerDetailsWithName_Null_Test(){
		RetrievePartnerDetailsWithNameResponse response = new RetrievePartnerDetailsWithNameResponse();
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> findByIdpolicyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Partner par = partner.get();
		PolicyGroup policyGroup = findByIdpolicyGroup.get();
		
		response.setId(par.getId());
		response.setAddress(par.getAddress());
		response.setContactNo(par.getContactNo());
		response.setCrBy(par.getCrBy());
		response.setCrDtimes(par.getCrDtimes());
		response.setEmailId(par.getEmailId());
		response.setIsActive(par.getIsActive());
		response.setName(par.getName());
		response.setUpdBy(par.getUpdBy());
		response.setUpdDtimes(par.getUpdDtimes());
		response.setUserId(par.getUserId());
		
		Mockito.when(partnerRepository.findByName(par.getName())).thenReturn(null);
		Mockito.when(policyGroupRepository.findById(par.getPolicyGroupId())).thenReturn(findByIdpolicyGroup);
		response.setPolicyGroupName(policyGroup.getName());
		pserviceImpl.getPartnerDetailsWithName(par.getName());
	}
	
	@Test
	public void getPolicyId_Test(){
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		Mockito.when(policyGroupRepository.findByName(policyGroup.getName())).thenReturn(policyGroup);
		pserviceImpl.getPolicyId(policyGroup.getName());
	}
	

	@Test
	public void getPolicyId_Null_Test(){
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		Mockito.when(policyGroupRepository.findByName(policyGroup.getName())).thenReturn(null);
		pserviceImpl.getPolicyId(policyGroup.getName());
	}

	@Test
	@Ignore
	public void savePartnerTest() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Partner partner = new Partner();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);

	}

	@Ignore
	@Test(expected = PolicyGroupDoesNotExistException.class)
	public void throwExceptionWhenPartnerPolicyGroupIsNullTest() {
		PartnerRequest partnerRequest = createPartnerRequest();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(null);
		pserviceImpl.savePartner(partnerRequest);
	}

	@Test(expected = PartnerAlreadyRegisteredException.class)
	@Ignore
	public void throwExceptionWhenPartnerNameAlreadyRegisteredTest() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.TRUE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName(Mockito.anyString())).thenReturn(new Partner());
		pserviceImpl.savePartner(partnerRequest);
	}

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
	@Ignore
	public void updatePartnerDetailsTest_S1() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "id");
		assertEquals(updatePartnerDetail.getStatus(), "Active");

	}
	
	@Ignore
	@Test(expected = PartnerDoesNotExistException.class)
	public void updatePartnerDetailTest_S2() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";
		pserviceImpl.updatePartnerDetail(req, partnerId);
	}
	
	@Test
	@Ignore
	public void updatePartnerDetailTest_S3() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";		
		Partner part = createPartner(Boolean.TRUE);
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.updatePartnerDetail(req, partnerId);
	}
	
	@Test(expected = PartnerAlreadyRegisteredException.class)
	@Ignore
	public void updatePartnerDetailTest_S4() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";
		Partner part = createPartner(Boolean.TRUE);
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		updatePartner(Boolean.TRUE);		
		pserviceImpl.updatePartnerDetail(req, partnerId);
	}
	

	@Test
	@Ignore
	public void doNotSetstatusWhenPartnerIsDeactiveTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.FALSE));
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "id");
		//assertNull(updatePartnerDetail.getStatus());
	}

	@Test(expected = PartnerDoesNotExistException.class)
	@Ignore
	public void doNotUpdaePartnerWhenPartnerDetailsIsEmptyTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.empty();
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
	}
	
	/*@Test
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
	
	public void submitPartnerApiKeyReq_Test(){
		PartnerAPIKeyRequest request = new PartnerAPIKeyRequest();
		String partnerId = "12345"; 
		
		String policyName = "Banking";
		String useCaseDescription = "This is Banking domain";

		request.setPolicyName(policyName);
		request.setUseCaseDescription(useCaseDescription);
		
		
	}*/

	/*@Test
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
	}*/

	/*@Test(expected = PartnerDoesNotExistsException.class)
	public void throwExceptionWhenPartnerNotFoundByPartnerIdTest() {
		String partnerId = "12345";
		PartnerAPIKeyRequest request = createPartnerAPIKeyRequest();
		Optional<Partner> partner = Optional.empty();
		Mockito.when(policyGroupRepository.findByName(request.getPolicyName())).thenReturn(null);
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.submitPartnerApiKeyReq(request, partnerId);
	}*/
	
	/*@Test(expected = PolicyGroupDoesNotExistException.class)
	public void throwExceptionWhenPartnerNotFoundByPartnerIdTest() {
		String partnerId = "12345";
		PartnerAPIKeyRequest request = createPartnerAPIKeyRequest();
		Optional<Partner> partner = Optional.empty();
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
	public void throwExceptionWhenPartnerPolicyIsNotFoundByPartnerIdTest(){
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

	@Test(expected = PartnerDoesNotExistsException.class)
	public void submitPartnerApiKeyReqTest_S1() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		pserviceImpl.submitPartnerApiKeyReq(req, "12345");
	}
	
	@Test(expected = PolicyGroupDoesNotExistException.class)
	public void submitPartnerApiKeyReqTest_S2() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test(expected = PartnerDoesNotExistsException.class)
	public void submitPartnerApiKeyReqTest_S3() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(false));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test
	@Ignore
	public void submitPartnerApiKeyReqTest_S4() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Approved");
		requests.add(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(requests);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test
	@Ignore
	public void submitPartnerApiKeyReqTest_S5() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Rejected");
		requests.add(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(requests);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test
	@Ignore
	public void submitPartnerApiKeyReqTest_S6() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("in-progress");
		requests.add(request);
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		Mockito.when(policyGroupRepository.findByName(policyGroup.getName())).thenReturn(policyGroup);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(new ArrayList<PartnerPolicyRequest>());
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test(expected = PartnerDoesNotExistsException.class)
	public void retrieveAllApiKeyRequestsSubmittedByPartnerTest_S1() {
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner("12345");
	}	
	
	@Test
	public void retrieveAllApiKeyRequestsSubmittedByPartnerTest_S2() {
		String partnerId = "12345";
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Rejected");
		requests.add(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(requests);
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerId);
	}
	
	@Test
	public void retrieveAllApiKeyRequestsSubmittedByPartnerTest_S3() {
		String partnerId = "12345";
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Approved");
		requests.add(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(requests);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerId)).thenReturn(createPartnerPolicy());
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerId);
	}
	
	private PartnerAPIKeyRequest createPartnerAPIKeyRequest() {
		PartnerAPIKeyRequest req = new PartnerAPIKeyRequest();
		req.setPolicyName("Banking");
		req.setUseCaseDescription("Banking Policy Description");
		return req;
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

//	private AuthPolicy createAuthPolicy() {
//		AuthPolicy authPolicy = new AuthPolicy();
//		authPolicy.setName("name");
//		return authPolicy;
//
//	}
//
//	private PartnerAPIKeyRequest createPartnerAPIKeyRequest() {
//		PartnerAPIKeyRequest partnerAPIKeyRequest = new PartnerAPIKeyRequest();
//		partnerAPIKeyRequest.setPolicyName("policyName");
//		partnerAPIKeyRequest.setUseCaseDescription("useCaseDescription");
//		return partnerAPIKeyRequest;
//	}

	private PartnerUpdateRequest createPartnerUpdateRequest() {
		PartnerUpdateRequest partnerUpdateRequest = new PartnerUpdateRequest();
		partnerUpdateRequest.setAddress("address");
		partnerUpdateRequest.setContactNumber("87878787");
		return partnerUpdateRequest;
	}

	private Partner createPartner(Boolean isActive) {
		LocalDateTime now = LocalDateTime.now();
		Partner partner = new Partner();
		partner.setId("id");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("policyGroupId");
		partner.setIsActive(isActive);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
		return partner;
	}
	
	private Partner updatePartner(Boolean isActive) {
		LocalDateTime now = LocalDateTime.now();
		Partner partner = new Partner();
		partner.setId("id");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("airtel");
		partner.setPolicyGroupId("policyGroupId");
		partner.setIsActive(isActive);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
		return partner;
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
		policyGroup.setName("Banking");
		policyGroup.setId("12133");
		policyGroup.setIsActive(isActive);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		return policyGroup;
	}

}