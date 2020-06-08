package io.mosip.pmp.partnermanagement.test.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

import io.mosip.pmp.partnermanagement.entity.MISPEntity;
import io.mosip.pmp.partnermanagement.PartnermanagementApplication;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.entity.AuthPolicy;
import io.mosip.pmp.partnermanagement.entity.MISPLicenseEntity;
import io.mosip.pmp.partnermanagement.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.partnermanagement.entity.Partner;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicyRequest;
import io.mosip.pmp.partnermanagement.entity.PolicyGroup;
import io.mosip.pmp.partnermanagement.exception.InvalidInputParameterException;
import io.mosip.pmp.partnermanagement.exception.NoPartnerApiKeyRequestsException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIKeyDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException;
import io.mosip.pmp.partnermanagement.exception.PartnerValidationException;
import io.mosip.pmp.partnermanagement.exception.PartnerIdDoesNotExistException;
import io.mosip.pmp.partnermanagement.repository.AuthPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.MispLicenseKeyRepository;
import io.mosip.pmp.partnermanagement.repository.MispServiceRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerRepository;
import io.mosip.pmp.partnermanagement.repository.PolicyGroupRepository;
import io.mosip.pmp.partnermanagement.service.impl.PartnerManagementServiceImpl;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnermanagementApplication.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class PartnerManagementServiceImplTest {
	
	private PartnerManagementServiceImpl partnerManagementImpl;
	
	@Mock
	PartnerPolicyRepository partnerPolicyRepository;

	@Mock
	PartnerRepository partnerRepository;
	
	@Mock
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;
	
	@Mock
	PolicyGroupRepository policyGroupRepository;
	
	@Mock
	AuthPolicyRepository authPolicyRepository;
	
	@Mock
	private MispLicenseKeyRepository misplKeyRepository;	
	
	@Mock
	MispServiceRepository mispRepository;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		partnerManagementImpl = new PartnerManagementServiceImpl();
		ReflectionTestUtils.setField(partnerManagementImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "misplKeyRepository", misplKeyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "mispRepository", mispRepository);
	}
	
	@Test(expected = PartnerValidationException.class)
	public void partnerApiKeyPolicyMappingsTest(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "678789";
		String PolicyAPIKey = "56784567";
		String old_auth_pilicy_id = "56784569";
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("id");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("policyGroupId");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
		
		
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("12133");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(old_auth_pilicy_id)).thenReturn(opt_authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		partnerManagementImpl.partnerApiKeyPolicyMappings(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
	
	@Test(expected = PartnerIdDoesNotExistException.class)
	public void activateDeactivateAuthEKYCPartnerTest_S1() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerId = "12345";
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	@Test
	public void activateDeactivateAuthEKYCPartnerTest_S2() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test(expected = InvalidInputParameterException.class)
	public void activateDeactivateAuthEKYCPartnerTest_S3() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("ctive");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test
	public void activateDeactivateAuthEKYCPartnerTest_S4() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test(expected = PartnerAPIKeyDoesNotExistException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S1() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("ctive");
		String partnerId = "12345";
		String apiKey ="2345";
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = PartnerValidationException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S2() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("ctive");
		String partnerId = "12345";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = InvalidInputParameterException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S3() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("ctive");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S4() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Active");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S5() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getAllAuthEKYCPartnersForThePolicyGroupTest_S1() {
		partnerManagementImpl.getAllAuthEKYCPartnersForThePolicyGroup();
	}
	
	@Test
	public void getAllAuthEKYCPartnersForThePolicyGroupTest_S2() {		
		List<Partner> partners = new ArrayList<Partner>();
		partners.add(getPartner());
		Mockito.when(partnerRepository.findAll()).thenReturn(partners);
		partnerManagementImpl.getAllAuthEKYCPartnersForThePolicyGroup();
	}
	
	@Test
	public void getAllAuthEKYCPartnersForThePolicyGroupTest_S3() {		
		List<Partner> partners = new ArrayList<Partner>();
		Optional<Partner> partner = Optional.of(getPartner());
		Partner part = partner.get();
		part.setIsActive(false);
		partners.add(part);
		Mockito.when(partnerRepository.findAll()).thenReturn(partners);
		partnerManagementImpl.getAllAuthEKYCPartnersForThePolicyGroup();
	}
	
	@Test
	public void getparticularAuthEKYCPartnerDetailsForGivenPartnerIdTest_S1() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.getparticularAuthEKYCPartnerDetailsForGivenPartnerId(partnerId);
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getparticularAuthEKYCPartnerDetailsForGivenPartnerIdTest_S2() {
		String partnerId = "89765";		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(Optional.empty());
		partnerManagementImpl.getparticularAuthEKYCPartnerDetailsForGivenPartnerId(partnerId);
	}
	
	@Test
	public void getparticularAuthEKYCPartnerDetailsForGivenPartnerIdTest_S3() {
		String partnerId = "12345";
		Partner partn = getPartner();
		partn.setIsActive(false);
		Optional<Partner> partner = Optional.of(partn);	
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.getparticularAuthEKYCPartnerDetailsForGivenPartnerId(partnerId);
	}
	
	@Test(expected = PartnerAPIDoesNotExistException.class)
	public void getPartnerAPIKeyToPolicyMappingTest_S1() {
		String partnerId = "123456";
		String apiKey ="2345";
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test(expected = PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException.class)
	public void getPartnerAPIKeyToPolicyMappingTest_S2() {
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test
	public void getPartnerAPIKeyToPolicyMappingTest_S3() {
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test(expected = PartnerIdDoesNotExistException.class)
	public void getPartnerAPIKeyToPolicyMappingTest_S4() {
		String partnerId = "asddsasd";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test(expected = NoPartnerApiKeyRequestsException.class)
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S1() {
		
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S2() {
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<PartnerPolicyRequest>();
		partnerPolicyRequests.add(getPartnerPolicyRequestData());
		Mockito.when(partnerPolicyRequestRepository.findAll()).thenReturn(partnerPolicyRequests);		
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S3() {
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<PartnerPolicyRequest>();
		partnerPolicyRequests.add(getPartnerPolicyRequestData());
		Mockito.when(partnerPolicyRequestRepository.findAll()).thenReturn(partnerPolicyRequests);	
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S4() {
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<PartnerPolicyRequest>();
		partnerPolicyRequests.add(getPartnerPolicyRequestData());
		Mockito.when(partnerPolicyRequestRepository.findAll()).thenReturn(partnerPolicyRequests);	
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		Optional<PolicyGroup> policyGroup = Optional.of(getPolicyGroupData());
		Mockito.when(policyGroupRepository.findById("234")).thenReturn(policyGroup);
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test(expected = NoPartnerApiKeyRequestsException.class)
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S1() {
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId("12345");
	}
	
	@Test
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S2() {
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));			
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apiKey);
	}
	
	@Test
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S3() {
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apiKey);
	}
	
	@Test
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S4() {
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);	
		Optional<PolicyGroup> policyGroup = Optional.of(getPolicyGroupData());
		Mockito.when(policyGroupRepository.findById("2345")).thenReturn(policyGroup);
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apiKey);
	}
	
	
	@Test(expected = PartnerAPIDoesNotExistException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S1() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerApiKey="2345";
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,partnerApiKey);
	}
	
	@Test(expected = InvalidInputParameterException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S2() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S3() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Approved");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S4() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Rejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S5() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Approved");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		String partnerId = "123456";
		PartnerPolicy partnerFromDB = Optional.of(getPartnerPolicy()).get();
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerId)).thenReturn(partnerFromDB);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}	
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S6() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Approved");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);	
		Mockito.when(partnerPolicyRepository.findByPartnerId("7267132")).thenReturn(null);
		Optional<PartnerPolicy> partnerFromDB1 = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById("30636009")).thenReturn(partnerFromDB1);
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}	
	
	@Test
	public void getPartnerManagerTest_S1() {
		partnerManagementImpl.getPartnerManager();
	}
	
	@Test
	public void getPartnerManagerTest_S2() {
		List<Partner> partners = new ArrayList<Partner>();
		Optional<Partner> partner = Optional.of(getPartner());
		partners.add(partner.get());
		Mockito.when(partnerRepository.findAll()).thenReturn(partners);
		partnerManagementImpl.getPartnerManager();
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerManagerTest_S3() {
		List<Partner> partners = new ArrayList<Partner>();
		Optional<Partner> partner = Optional.of(getPartner());
		partners.add(partner.get());
		Mockito.when(partnerRepository.findAll()).thenReturn(null);
		partnerManagementImpl.getPartnerManager();
	}
	
	@Test
	public void getPartnerPolicyIDTest_S1() {
		partnerManagementImpl.getPartnerPolicyID("Test");
	}
	
	@Test
	public void getPartnerPolicyIDTest_S2() {
		Optional<PolicyGroup> policyGroup = Optional.of(getPolicyGroupData());
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup.get());
		partnerManagementImpl.getPartnerPolicyID("Test");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S1() {
		partnerManagementImpl.getPartnerMappedPolicyFile("","","");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S2() {
		MISPLicenseEntity license = mispLicense();
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","");
	}	
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S3() {
		MISPLicenseEntity license = mispLicense();
		license.setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","");
	}	
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S4() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(false);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}	
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S5() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(-10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S6() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);		
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.empty());
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S7() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(false);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S8() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.setPolicyGroup(null);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S9() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(false);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S10() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S11() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\\\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerValidationException.class)
	public void getPartnerMappedPolicyFileTest_S12() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispUniqueEntity().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\\\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(false));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"");
	}
	
	@Test(expected = PartnerAPIKeyDoesNotExistException.class)
	public void partnerApiKeyPolicyMappingsTest_S1() {
		PartnersPolicyMappingRequest request = createRequest();
		partnerManagementImpl.partnerApiKeyPolicyMappings(request,"1234","543");
	}
	
	private MISPEntity misp(boolean isActive) {
		MISPEntity dto = new MISPEntity();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setID("100");;
		dto.setStatus_code("approved");
		dto.setIsActive(isActive);
		return dto;
	}
	
	private PartnersPolicyMappingRequest createRequest() {
		PartnersPolicyMappingRequest req = new PartnersPolicyMappingRequest();
		req.setOldPolicyID("1234");
		req.setNewPolicyID("456");
		return req;
	}

	private MISPlKeyUniqueKeyEntity mispLUniqueKey() {
		MISPlKeyUniqueKeyEntity unique = new MISPlKeyUniqueKeyEntity();
		unique.setMisp_id("100");
		unique.setLicense_key("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy");
		return unique;
	}
	private MISPLicenseEntity mispLicense() {
		MISPLicenseEntity mispLices = new MISPLicenseEntity();
		mispLices.setMispUniqueEntity(mispLUniqueKey());
		mispLices.setValidFromDate(LocalDateTime.now());
		mispLices.setValidToDate(LocalDateTime.now().plusDays(90));
		mispLices.setIsActive(true);
		return mispLices;
	}
	
	private PolicyGroup getPolicyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("12133");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		return policyGroup;
	}

	private PartnerPolicyRequest getPartnerPolicyRequestData() {
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setId("1234456");
		partnerPolicyRequest.setPartner(getPartner());
		partnerPolicyRequest.setPolicyId("2345");
		partnerPolicyRequest.setStatusCode("in-progress");
		return partnerPolicyRequest;
	}
	private List<AuthPolicy> getAuthPolicies(){
		AuthPolicy policy = new AuthPolicy();
		List<AuthPolicy> policies = new ArrayList<AuthPolicy>();
		policy.setPolicyGroup(policyGroupData());
		policy.setId("234");
		policy.setName("Test");
		policy.setDescr("Policy Desc");
		policy.setIsActive(true);
		policy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		policies.add(policy);
		return policies;
		
	}
	
	private PolicyGroup policyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		return policyGroup;
	}
	private PartnerPolicy getPartnerPolicy() {
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey("2345");
		partnerPolicy.setPartner(getPartner());
		partnerPolicy.setIsActive(true);
		partnerPolicy.setPolicyId("234");
		return partnerPolicy;
	}
	private Partner getPartner() {
		Partner part = new Partner();
		part.setAddress("Bane");
		part.setContactNo("0123456789");
		part.setIsActive(true);
		part.setId("123456");
		part.setPolicyGroupId("234");
		return part;
	}
}
