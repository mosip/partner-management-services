package io.mosip.pmp.partner.test.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pmp.partner.PartnerserviceApplication;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.ExtractorDto;
import io.mosip.pmp.partner.dto.ExtractorProviderDto;
import io.mosip.pmp.partner.dto.ExtractorsDto;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;
import io.mosip.pmp.partner.entity.AuthPolicy;
import io.mosip.pmp.partner.entity.BiometricExtractorProvider;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PartnerPolicyCredentialType;
import io.mosip.pmp.partner.entity.PartnerPolicyCredentialTypePK;
import io.mosip.pmp.partner.entity.PartnerPolicyRequest;
import io.mosip.pmp.partner.entity.PartnerType;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.APIKeyReqIdStatusInProgressException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyIsNotCreatedException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyReqIDDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistsException;
import io.mosip.pmp.partner.exception.PartnerServiceException;
import io.mosip.pmp.partner.exception.PartnerTypeDoesNotExistException;
import io.mosip.pmp.partner.exception.PolicyGroupDoesNotExistException;
import io.mosip.pmp.partner.repository.AuthPolicyRepository;
import io.mosip.pmp.partner.repository.BiometricExtractorProviderRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyCredentialTypeRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.partner.repository.PartnerTypeRepository;
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
	
	@Autowired
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
	@Mock
	PartnerTypeRepository partnerTypeRepository;
	@Mock 
	BiometricExtractorProviderRepository extractorProviderRepository;	
	@Mock
	PartnerPolicyCredentialTypeRepository partnerCredentialTypePolicyRepo;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(pserviceImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(pserviceImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerTypeRepository", partnerTypeRepository);
		ReflectionTestUtils.setField(pserviceImpl, "extractorProviderRepository", extractorProviderRepository);
		ReflectionTestUtils.setField(pserviceImpl, "partnerCredentialTypePolicyRepo", partnerCredentialTypePolicyRepo);

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
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);

	}
	
	@Test(expected = PolicyGroupDoesNotExistException.class)
	public void throwExceptionWhenPartnerPolicyGroupIsNullTest() {
		PartnerRequest partnerRequest = createPartnerRequest();
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(null);		
		pserviceImpl.savePartner(partnerRequest);
	}

	@Test(expected = PartnerTypeDoesNotExistException.class)	
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
	public void updatePartnerDetailsTest_S1() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "id");
		assertEquals(updatePartnerDetail.getStatus(), "Active");

	}
	
	@Test(expected = PartnerDoesNotExistException.class)
	public void updatePartnerDetailTest_S2() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";
		pserviceImpl.updatePartnerDetail(req, partnerId);
	}
	
	@Test	
	public void updatePartnerDetailTest_S3() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";
		Partner part = createPartner(Boolean.TRUE);
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.updatePartnerDetail(req, partnerId);
	}
	
	@Test
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
	
	@Test(expected = PartnerServiceException.class)
	public void addBiometricExtractorsTest_001() {
		Partner part = createPartner(Boolean.TRUE);
		String partnerId = "12345";
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.addBiometricExtractors("12345", "12345", getExtractorsInput());
	}
	
	@Test(expected = PartnerDoesNotExistsException.class)
	public void addBiometricExtractorsTest_002() {
		Partner part = createPartner(Boolean.TRUE);
		String partnerId = "456789";
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.addBiometricExtractors("12345", "12345", getExtractorsInput());
	}
	
	@Test(expected = PartnerServiceException.class)
	public void addBiometricExtractorsTest_003() {
		Partner part = createPartner(Boolean.TRUE);
		part.setPartnerTypeCode("Auth");
		String partnerId = "12345";
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.addBiometricExtractors("12345", "12345", getExtractorsInput());
	}
	
	@Test(expected = PolicyGroupDoesNotExistException.class)
	public void addBiometricExtractorsTest_004() {
		Partner part = createPartner(Boolean.TRUE);
		String partnerId = "12345";
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.addBiometricExtractors("12345", "123456", getExtractorsInput());
	}
	
	@Test
	public void addBiometricExtractorsTest_005() {
		Partner part = createPartner(Boolean.TRUE);
		String partnerId = "12345";
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		PartnerPolicyRequest partnerRequestedData = new PartnerPolicyRequest();
		partnerRequestedData.setPartner(part);
		partnerRequestedData.setPolicyId("123456");
		partnerRequestedData.setStatusCode("In-Progress");
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(partnerId,"12345")).thenReturn(partnerRequestedData);
		pserviceImpl.addBiometricExtractors("12345", "12345", getExtractorsInput());
	}
	
	@Test(expected = PartnerServiceException.class)
	public void addBiometricExtractorsTest_006() {
		Partner part = createPartner(Boolean.TRUE);
		String partnerId = "12345";
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		PartnerPolicyRequest partnerRequestedData = new PartnerPolicyRequest();
		partnerRequestedData.setPartner(part);
		partnerRequestedData.setPolicyId("123456");
		partnerRequestedData.setStatusCode("Approved");
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(partnerId,"12345")).thenReturn(partnerRequestedData);
		pserviceImpl.addBiometricExtractors("12345", "12345", getExtractorsInput());
	}
	
	@Test
	public void addBiometricExtractorsTest_007() {
		Partner part = createPartner(Boolean.TRUE);
		String partnerId = "12345";
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		PartnerPolicyRequest partnerRequestedData = new PartnerPolicyRequest();
		partnerRequestedData.setPartner(part);
		partnerRequestedData.setPolicyId("123456");
		partnerRequestedData.setStatusCode("In-Progress");
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(partnerId,"12345")).thenReturn(partnerRequestedData);
		BiometricExtractorProvider extractorsFromDb = new BiometricExtractorProvider();
		extractorsFromDb.setId("1234567");
		Mockito.when(extractorProviderRepository.findByPartnerAndPolicyIdAndAttributeName(partnerId, "12345", "face")).thenReturn(extractorsFromDb);
		pserviceImpl.addBiometricExtractors("12345", "12345", getExtractorsInput());
	}
	
	@Test(expected = PartnerServiceException.class)
	public void getBiometricExtractorsTest_001() {
		List<BiometricExtractorProvider> data = new ArrayList<>();
		Mockito.when(extractorProviderRepository.findByPartnerAndPolicyId("12345", "12345")).thenReturn(data);
		pserviceImpl.getBiometricExtractors("12345", "12345");
	}
	
	@Test
	public void getBiometricExtractorsTest_002() {
		List<BiometricExtractorProvider> data = new ArrayList<>();
		BiometricExtractorProvider extractorsFromDb = new BiometricExtractorProvider();
		extractorsFromDb.setId("1234567");	
		data.add(extractorsFromDb);
		Mockito.when(extractorProviderRepository.findByPartnerAndPolicyId("12345", "12345")).thenReturn(data);
		pserviceImpl.getBiometricExtractors("12345", "12345");
	}
	
	@Test
	public void getBiometricExtractorsTest_003() {
		List<BiometricExtractorProvider> data = new ArrayList<>();
		BiometricExtractorProvider extractorsFromDb = new BiometricExtractorProvider();
		extractorsFromDb.setId("1234567");
		extractorsFromDb.setBiometricModality("finger");
		extractorsFromDb.setBiometricSubTypes("RightIndex");
		data.add(extractorsFromDb);
		Mockito.when(extractorProviderRepository.findByPartnerAndPolicyId("12345", "12345")).thenReturn(data);
		pserviceImpl.getBiometricExtractors("12345", "12345");
	}
	
	@Test
	public void doNotSetstatusWhenPartnerIsDeactiveTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.FALSE));
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "id");
	}

	@Test(expected = PartnerDoesNotExistException.class)
	public void doNotUpdaePartnerWhenPartnerDetailsIsEmptyTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.empty();
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
	}
	

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
	public void submitPartnerApiKeyReqTest_S4() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("policyGroupId", "Banking")).thenReturn(createAuthPolicy());
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Approved");
		requests.add(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(requests);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test
	public void submitPartnerApiKeyReqTest_S5() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("policyGroupId", "Banking")).thenReturn(createAuthPolicy());
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Rejected");
		requests.add(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(requests);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test
	public void submitPartnerApiKeyReqTest_S6() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("policyGroupId", "Banking")).thenReturn(createAuthPolicy());
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
	
	@Test
	public void mapPartnerPolicyCredentialType_001() {		
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("euin", "12345", "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void mapPartnerPolicyCredentialType_002() {		
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("uin", "12345", "12345");
	}
	
	@Test(expected = PartnerDoesNotExistsException.class)
	public void mapPartnerPolicyCredentialType_003() {		
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("euin", "1234578", "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void mapPartnerPolicyCredentialType_004() {		
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("euin", "12345", "12345678");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void mapPartnerPolicyCredentialType_005() {		
		Optional<Partner> partner = Optional.of(createPartner(true));	
		partner.get().setPartnerTypeCode("Auth_Partner");
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("euin", "12345", "12345678");
	}
	
	@Test
	public void getPartnerCredentialTypePolicy_001() throws JsonParseException, JsonMappingException, IOException {		
		PartnerPolicyCredentialType response = new PartnerPolicyCredentialType();
		PartnerPolicyCredentialTypePK key = new PartnerPolicyCredentialTypePK();
		key.setCredentialType("euin");
		key.setPartId("12345");
		key.setPolicyId("12345");
		response.setId(key);
		response.setCrBy("system");
		response.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		Mockito.when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType("12345", "euin")).thenReturn(response);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.getPartnerCredentialTypePolicy("euin", "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void getPartnerCredentialTypePolicy_002() throws JsonParseException, JsonMappingException, IOException {		
		PartnerPolicyCredentialType response = new PartnerPolicyCredentialType();
		PartnerPolicyCredentialTypePK key = new PartnerPolicyCredentialTypePK();
		key.setCredentialType("euin");
		key.setPartId("12345");
		key.setPolicyId("12345");
		response.setId(key);
		response.setCrBy("system");
		response.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		Mockito.when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType("12345", "euin")).thenReturn(null);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.getPartnerCredentialTypePolicy("euin", "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void getPartnerCredentialTypePolicy_003() throws JsonParseException, JsonMappingException, IOException {		
		PartnerPolicyCredentialType response = new PartnerPolicyCredentialType();
		PartnerPolicyCredentialTypePK key = new PartnerPolicyCredentialTypePK();
		key.setCredentialType("euin");
		key.setPartId("12345");
		key.setPolicyId("12345");
		response.setId(key);
		response.setCrBy("system");
		response.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		Mockito.when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType("12345", "euin")).thenReturn(response);
		Mockito.when(authPolicyRepository.findById("1234578")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.getPartnerCredentialTypePolicy("euin", "12345");
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

	private AuthPolicy createAuthPolicy() {
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("12345");
		authPolicy.setName("name");
		authPolicy.setIsActive(true);
		authPolicy.setPolicyFileId("{\"allowedAuthTypes\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true}],\"shareableAttributes\":[{\"encrypted\":false,\"format\":null,\"attributeName\":\"fullName\"},{\"encrypted\":false,\"format\":\"yyyy\",\"attributeName\":\"dateOfBirth\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"gender\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"phone\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"email\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"postalCode\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"face\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"finger\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"iris\"}],\"dataSharePolicies\":{\"transactionsAllowed\":\"2\",\"shareDomain\":\"mosip.io\",\"encryptionType\":\"partnerBased\",\"validForInMinutes\":\"30\",\"typeOfShare\":\"dataShare\"}}");
		return authPolicy;

	}
//
//	private PartnerAPIKeyRequest createPartnerAPIKeyRequest() {
//		PartnerAPIKeyRequest partnerAPIKeyRequest = new PartnerAPIKeyRequest();
//		partnerAPIKeyRequest.setPolicyName("policyName");
//		partnerAPIKeyRequest.setUseCaseDescription("useCaseDescription");
//		return partnerAPIKeyRequest;
//	}

	private ExtractorsDto getExtractorsInput() {
    	ExtractorsDto request = new ExtractorsDto();
    	List<ExtractorDto> extractors = new ArrayList<>();
    	ExtractorDto dto = new ExtractorDto();
    	dto.setAttributeName("face");
    	dto.setBiometric("face[RightIndex]");
    	ExtractorProviderDto provider = new ExtractorProviderDto();
    	provider.setProvider("t5");
    	provider.setVersion("1.1");
    	dto.setExtractor(provider);
    	extractors.add(dto);
    	request.setExtractors(extractors);
    	return request;
    }
	
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
		partner.setPartnerTypeCode("Credential_Partner");
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
		prequest.setPartnerId("abc1234");
		prequest.setPartnerType("Auth");
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
	
	private PartnerType getPartnerType() {
		PartnerType partnerType = new PartnerType();
		partnerType.setCode("Auth");
		partnerType.setPartnerDescription("Auth");
		partnerType.setIsPolicyRequired(true);
		return partnerType;
	}

}