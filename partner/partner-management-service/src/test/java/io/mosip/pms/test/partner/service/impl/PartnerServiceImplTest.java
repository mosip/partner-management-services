package io.mosip.pms.test.partner.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.BiometricExtractorProvider;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerContact;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PartnerPolicyCredentialType;
import io.mosip.pms.common.entity.PartnerPolicyCredentialTypePK;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.PartnerType;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.BiometricExtractorProviderRepository;
import io.mosip.pms.common.repository.PartnerContactRepository;
import io.mosip.pms.common.repository.PartnerPolicyCredentialTypeRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.PartnerTypeRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.keycloak.service.KeycloakImpl;
import io.mosip.pms.partner.request.dto.AddContactRequestDto;
import io.mosip.pms.partner.request.dto.ExtractorDto;
import io.mosip.pms.partner.request.dto.ExtractorProviderDto;
import io.mosip.pms.partner.request.dto.ExtractorsDto;
import io.mosip.pms.partner.request.dto.PartnerAPIKeyRequest;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateRequestDto;
import io.mosip.pms.partner.request.dto.PartnerRequest;
import io.mosip.pms.partner.request.dto.PartnerSearchDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateRequest;
import io.mosip.pms.partner.response.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.response.dto.PartnerResponse;
import io.mosip.pms.partner.response.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.service.impl.PartnerServiceImpl;


/**
 * @author sanjeev.shrivastava
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
@Ignore
public class PartnerServiceImplTest {
	
	@Autowired
	private PartnerServiceImpl pserviceImpl;

	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	FilterColumnValidator filterColumnValidator;
	@MockBean
	RestUtil restUtil;
	@MockBean
	PageUtils pageUtils;
	@MockBean
	SearchHelper partnerSearchHelper;
	@MockBean
	PolicyGroupRepository policyGroupRepository;
	@MockBean
	PartnerServiceRepository partnerRepository;
	@MockBean
	AuthPolicyRepository authPolicyRepository;
	@MockBean
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;
	@MockBean
	PartnerPolicyRepository partnerPolicyRepository;
	@MockBean
	PartnerTypeRepository partnerTypeRepository;
	@MockBean
	PartnerContactRepository partnerContactRepository;
	@MockBean 
	BiometricExtractorProviderRepository extractorProviderRepository;	
	@MockBean
	PartnerPolicyCredentialTypeRepository partnerCredentialTypePolicyRepo;
	@MockBean
	private WebSubPublisher webSubPublisher;
	
	@MockBean
	private KeycloakImpl keycloakImpl;
	
	FilterValueDto deviceFilterValueDto = new FilterValueDto();
	FilterDto filterDto = new FilterDto();
	SearchFilter searchFilter = new SearchFilter();
	PartnerSearchDto partnerSearchDto = new PartnerSearchDto();
	PartnerSearchDto partnerSearchDto1 = new PartnerSearchDto();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	SearchDto searchDto = new SearchDto();
	PartnerCertificateRequestDto partnerCertificateRequestDto = new PartnerCertificateRequestDto();
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
		ReflectionTestUtils.setField(pserviceImpl, "partnerContactRepository", partnerContactRepository);
		ReflectionTestUtils.setField(pserviceImpl, "filterColumnValidator", filterColumnValidator);
		ReflectionTestUtils.setField(pserviceImpl, "partnerSearchHelper", partnerSearchHelper);
		ReflectionTestUtils.setField(pserviceImpl, "pageUtils", pageUtils);
		ReflectionTestUtils.setField(pserviceImpl, "restUtil", restUtil);
		
		//Filter_Test
		searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("all");
		searchFilter.setValue("m");
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	searchDtos.add(searchFilter);
    	filterDto.setColumnName("name");
    	filterDto.setText("");
    	filterDto.setType("all");
    	List<FilterDto> filterDtos = new ArrayList<FilterDto>();
    	filterDtos.add(filterDto);
    	deviceFilterValueDto.setFilters(filterDtos);
    	//Search_Partner_Test
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	searchSort.setSortField("name");
    	searchSort.setSortType("asc");
    	List<SearchSort> sortDtos = new ArrayList<SearchSort>();
    	sortDtos.add(searchSort);
    	partnerSearchDto.setPartnerType("all");
    	partnerSearchDto.setFilters(searchDtos);
    	partnerSearchDto.setSort(sortDtos);
    	partnerSearchDto1.setPartnerType("1234");
    	partnerSearchDto1.setFilters(searchDtos);
    	partnerSearchDto1.setSort(sortDtos);
    	//Search_PartnerType_Test
    	searchDto.setSort(sortDtos);
    	searchDto.setFilters(searchDtos);
    	//certificate
    	partnerCertificateRequestDto.setCertificateData("1234");
		partnerCertificateRequestDto.setOrganizationName("airtel");
		partnerCertificateRequestDto.setPartnerDomain("network");
		partnerCertificateRequestDto.setPartnerId("id");
		partnerCertificateRequestDto.setPartnerType("Auth");		
	}

	@Test(expected = PartnerServiceException.class) 
	public void getPartnerCertificate_Test() throws Exception{
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> policyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		Mockito.when(policyGroupRepository.findById(partner.get().getPolicyGroupId())).thenReturn(policyGroup);
		PartnerCertDownloadRequestDto partnerCertDownloadRequestDto = new PartnerCertDownloadRequestDto();
		partnerCertDownloadRequestDto.setPartnerId("id");
		PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = pserviceImpl.getPartnerCertificate(partnerCertDownloadRequestDto);
		assertNotNull(partnerCertDownloadResponeDto);
		assertEquals(partnerCertDownloadResponeDto.getCertificateData(), "12345");
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(),Mockito.any(),Mockito.any());

	}
	
	@Test
	public void addContact_test() throws Exception{
		AddContactRequestDto addContactRequestDto = new AddContactRequestDto();
    	addContactRequestDto.setAddress("Banglore");
    	addContactRequestDto.setContactNumber("123456789");
    	addContactRequestDto.setEmailId("xyz@gmail.com");
    	addContactRequestDto.setIs_Active(true);
    	
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Partner par = partner.get();
		PartnerContact contactFromDB = new PartnerContact();
		
		Mockito.when(partnerRepository.findById(par.getId())).thenReturn(partner);
		Mockito.when(partnerContactRepository.findByPartnerAndEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(contactFromDB);
	    pserviceImpl.createAndUpdateContactDetails(addContactRequestDto, par.getId());
	}
	
	@Test
	public void addContact_test01() throws Exception{
		AddContactRequestDto addContactRequestDto = new AddContactRequestDto();
    	addContactRequestDto.setAddress("Banglore");
    	addContactRequestDto.setContactNumber("123456789");
    	addContactRequestDto.setEmailId("xyz@gmail.com");
    	addContactRequestDto.setIs_Active(true);
    	
		Optional<Partner> partner = Optional.of(createPartner(true));
		Partner par = partner.get();
		//PartnerContact contactFromDB = new PartnerContact();
		
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		//Mockito.when(partnerContactRepository.findByPartnerAndEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(contactFromDB);
	    pserviceImpl.createAndUpdateContactDetails(addContactRequestDto, par.getId());
	}
	
	@Test
	public void searchPartnerApiKeyRequestsTest() throws Exception {
		objectMapper.writeValueAsString(searchDto);
		PartnerPolicyRequest partnerPolicy = new PartnerPolicyRequest();
		partnerPolicy.setId("12345");
		partnerPolicy.setPartner(createPartner(true));
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partnerPolicy))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.anyString());
		pserviceImpl.searchPartnerApiKeyRequests(searchDto);
	}
	
	@Test
	public void searchPartnerApiKeysTest() throws Exception {
		objectMapper.writeValueAsString(searchDto);
		PartnerPolicy policy = new PartnerPolicy();
		policy.setPolicyId("12345");
		policy.setPartner(createPartner(true));
		Mockito.doReturn(new PageImpl<>(Arrays.asList(policy))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.anyString());
		pserviceImpl.searchPartnerApiKeys(searchDto);
	}
	
	@Test
	public void searchPartnertest() throws Exception{
		objectMapper.writeValueAsString(partnerSearchDto);
		Partner partner = new Partner();
		partner.setId("id");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partner))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.anyString());
		pserviceImpl.searchPartner(partnerSearchDto);
	}
	
	@Test
	public void searchPartnertest01() throws Exception{
		objectMapper.writeValueAsString(partnerSearchDto1);
		Partner partner = new Partner();
		partner.setId("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partner))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.anyString());
		pserviceImpl.searchPartner(partnerSearchDto1);
	}
	
	@Test
	public void searchPartnerTypetest() throws Exception{
		objectMapper.writeValueAsString(searchDto);
		PartnerType partnerType = new PartnerType();
		partnerType.setCode("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partnerType))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.anyString());
		pserviceImpl.searchPartnerType(searchDto);
	}
	@Test
	public void partnerFilterValues_Test() {
		pserviceImpl.filterValues(deviceFilterValueDto);
		}
	
	@Test
	public void apiKeyRequestFilterTest() {
		pserviceImpl.apiKeyRequestFilter(deviceFilterValueDto);
		}
	
	@Test
	public void getPartnerDetailsWithName_Test(){
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> findByIdpolicyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Partner par = partner.get();
		response.setAddress(par.getAddress());
		response.setContactNumber(par.getContactNo());
		response.setEmailId(par.getEmailId());
		response.setOrganizationName("org");
		response.setPartnerID(par.getId());
		response.setPartnerType(par.getPartnerTypeCode());
		response.setPolicyGroup("policyGroup");
		response.setStatus("Approved");	
		
		Mockito.when(partnerRepository.findByName(par.getName())).thenReturn(par);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		Mockito.when(policyGroupRepository.findById(par.getPolicyGroupId())).thenReturn(findByIdpolicyGroup);
		pserviceImpl.getPartnerDetails(par.getName());
	}
	
	@Test (expected = PartnerServiceException.class)
	public void getPartnerDetailsWithName_Test01(){
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Partner par = partner.get();
		response.setAddress(par.getAddress());
		response.setContactNumber(par.getContactNo());
		response.setEmailId(par.getEmailId());
		response.setOrganizationName("org");
		response.setPartnerID(par.getId());
		response.setPartnerType(par.getPartnerTypeCode());
		response.setPolicyGroup("policyGroup");
		response.setStatus("Approved");		
		Mockito.when(partnerRepository.findByName(par.getName())).thenReturn(par);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		pserviceImpl.getPartnerDetails(par.getName());
	}
	
	@Test(expected = PartnerServiceException.class)
	public void getPartnerDetailsWithName_Null_Test(){
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> findByIdpolicyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Partner par = partner.get();
		
		response.setAddress(par.getAddress());
		response.setContactNumber(par.getContactNo());
		response.setEmailId(par.getEmailId());
		response.setOrganizationName("org");
		response.setPartnerID(par.getId());
		response.setPartnerType(par.getPartnerTypeCode());
		response.setPolicyGroup("policyGroup");
		response.setStatus("Approved");	
		
		Mockito.when(partnerRepository.findByName(par.getName())).thenReturn(null);
		Mockito.when(policyGroupRepository.findById(par.getPolicyGroupId())).thenReturn(findByIdpolicyGroup);
		pserviceImpl.getPartnerDetails(par.getName());
	}

	@Test
	public void savePartnerTest() {
		MosipUserDto userDto = new MosipUserDto();
		userDto.setName("PARTNER");
		userDto.setMobile("partner@gmail.com");
		Mockito.doReturn(userDto).when(keycloakImpl).registerUser(Mockito.any(io.mosip.pms.partner.dto.UserRegistrationRequestDto.class));
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Partner partner = new Partner();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void savePartnerTest01() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Partner partner = new Partner();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		//Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void savePartnerTest02() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Partner partner = new Partner();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		Mockito.when(partnerRepository.findByEmailId(Mockito.anyString())).thenReturn(partner);
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void savePartnerTest03() {
		PartnerRequest partnerRequest = createInvalidPolicyGroupPartnerRequest();
		Partner partner = new Partner();
		PartnerType partType = new PartnerType();
		partType.setIsPolicyRequired(true);
		Optional<PartnerType>opt_partType = Optional.of(partType);
		Mockito.when(partnerTypeRepository.findById(Mockito.anyString())).thenReturn(opt_partType);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void savePartnerTest04() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Partner partner = new Partner();
		Optional<Partner>opt_prt = Optional.of(partner);
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(opt_prt);
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void savePartnerTest05() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createInvalidIDLengthPartnerRequest();
		Partner partner = new Partner();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void savePartnerTest06() {
		PolicyGroup policyGroup = createPolicyGroup(Boolean.FALSE);
		PartnerRequest partnerRequest = createInvalidEmailPartnerRequest();
		Partner partner = new Partner();
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		Mockito.when(partnerTypeRepository.findById("Auth")).thenReturn(Optional.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
	}

	@Test(expected = PartnerServiceException.class)	
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

	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerDetailsIsEmptyTest() {
		Optional<Partner> partner = Optional.empty();
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		pserviceImpl.getPartnerDetails("12345");
	}

	@Test	
	public void updatePartnerDetailsTest_S1() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));
		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
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
	
	@Test(expected = PartnerServiceException.class)
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
	
	@Test(expected = PartnerServiceException.class)
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
		List<PartnerPolicyRequest> request = new ArrayList<>();
		request.add(partnerRequestedData);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(partnerId,"12345")).thenReturn(request);
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
		List<PartnerPolicyRequest> request = new ArrayList<>();
		request.add(partnerRequestedData);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(partnerId,"12345")).thenReturn(request);
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
		List<PartnerPolicyRequest> request = new ArrayList<>();
		request.add(partnerRequestedData);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(partnerId,"12345")).thenReturn(request);
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
		Optional<Partner> partner = Optional.of(createPartner(true));
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "12345");
	}

	@Test(expected = PartnerServiceException.class)
	public void doNotUpdaePartnerWhenPartnerDetailsIsEmptyTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.empty();
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
	}
	

	@Test
	@Ignore
	public void downloadPartnerAPIkeyTest() {
		String partnerID = "12345";
		String aPIKeyReqID = "12345";
		PartnerPolicy policy = new PartnerPolicy();
		policy.setPolicyApiKey("12345");
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now()));
		Partner part = createPartner(Boolean.TRUE);
		part.setName("name");
		PartnerPolicyRequest partnerRequestedData = new PartnerPolicyRequest();
		partnerRequestedData.setPartner(part);
		partnerRequestedData.setPolicyId("123456");
		partnerRequestedData.setStatusCode("In-Progress");
		List<PartnerPolicyRequest> request = new ArrayList<>();
		request.add(partnerRequestedData);
		Optional<PartnerPolicyRequest> partner_request = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndReqId(Mockito.anyString(), Mockito.anyString())).thenReturn(createPartnerPolicyRequest("Approved"));
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(createPartnerPolicy());
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(Mockito.anyString(), Mockito.anyString())).thenReturn(request);
		Mockito.when(partnerPolicyRepository.findByApiKey(Mockito.anyString())).thenReturn(policy);
		DownloadPartnerAPIkeyResponse downloadPartnerAPIkey = pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
		assertNotNull(downloadPartnerAPIkey);
		assertEquals(downloadPartnerAPIkey.getPartnerAPIKey(), createPartnerPolicy().getPolicyApiKey());
	}

	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerPolicyNotFoundByPartnerIdTest() {
		String partnerID = "id";
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partner_request = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(null);
		pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
	}

	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPolicyRequestIdNotEqualsPartnerIdTest() {
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partner_request = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
	}

	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerPolicyRequestNotFoundByAPIKeyReqIDTest() {
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partner_request = Optional.empty();
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partner_request);
		pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
	}

	@Test
	@Ignore
	public void viewApiKeyRequestStatusApiKeyTest() {
		String partnerID = "id";
		String aPIKeyReqID = "aPIKeyReqID";
		PartnerPolicy policy = new PartnerPolicy();
		policy.setPolicyApiKey("12345");
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now()));
		Partner part = createPartner(Boolean.TRUE);
		part.setName("name");
		PartnerPolicyRequest partnerRequestedData = new PartnerPolicyRequest();
		partnerRequestedData.setPartner(part);
		partnerRequestedData.setPolicyId("123456");
		partnerRequestedData.setStatusCode("In-Progress");
		List<PartnerPolicyRequest> request = new ArrayList<>();
		request.add(partnerRequestedData);
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.of(createPartnerPolicyRequest("Approved"));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerID)).thenReturn(createPartnerPolicy());
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(Mockito.anyString(), Mockito.anyString())).thenReturn(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndReqId(Mockito.anyString(), Mockito.anyString())).thenReturn(createPartnerPolicyRequest("Approved"));
		Mockito.when(partnerPolicyRepository.findByApiKey(Mockito.anyString())).thenReturn(policy);
		DownloadPartnerAPIkeyResponse viewApiKeyRequestStatusApiKey = pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
		assertNotNull(viewApiKeyRequestStatusApiKey);
		assertEquals(viewApiKeyRequestStatusApiKey.getApikeyReqStatus(), "Approved");
		assertEquals(viewApiKeyRequestStatusApiKey.getPartnerAPIKey(), "12345");
	}

	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerPolicyIdNotEqualsPartnerIdTest() {
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.of(createPartnerPolicyRequest(""));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
	}

	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerPolicyStatusIsNotApprovedTest() {
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.of(createPartnerPolicyRequest("Rejected"));
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
	}

	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerPolicyRequestIsNotFoundByAPIKeyReqIDTest() {
		String aPIKeyReqID = "aPIKeyReqID";
		Optional<PartnerPolicyRequest> partnerPolicyRequest = Optional.empty();
		Mockito.when(partnerPolicyRequestRepository.findById(aPIKeyReqID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.getApikeyFromRequestKey(aPIKeyReqID);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerPolicyRequestIsEmptyTest() {
		String partnerID = "id";
		List<PartnerPolicyRequest> partnerPolicyRequest = new ArrayList<>();
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
		
	}

	@Test
	public void submitPartnerApiKeyReqTest() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Partner part = createPartner(true);
		AuthPolicy authPolicy =createAuthPolicy();		
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void submitPartnerApiKeyReqTest01() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Partner part = createPartner(true);
		AuthPolicy authPolicy =createInactiveAuthPolicy();		
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void submitPartnerApiKeyReqTest02() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Partner part = createPartner(true);
		AuthPolicy authPolicy =createTimeInvariantAuthPolicy();		
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	@Test(expected = PartnerServiceException.class)
	public void submitPartnerApiKeyReqTest03() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Partner part = createPartner(true);
		AuthPolicy authPolicy =createInactiveGroupAuthPolicy();		
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test
	public void submitPartnerApiKeyReqTest04() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Partner part = createPartner(true);
		PartnerPolicy partPolicy = new PartnerPolicy();
		List<PartnerPolicy> partPolicyList = new ArrayList<>();
		partPolicyList.add(partPolicy);
		AuthPolicy authPolicy =createAuthPolicy();		
		Optional<Partner> partner = Optional.of(part);		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndIsActiveTrue(Mockito.anyString())).thenReturn(partPolicyList);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void submitPartnerApiKeyReqTest_S1() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		pserviceImpl.submitPartnerApiKeyReq(req, "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void submitPartnerApiKeyReqTest_S2() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void submitPartnerApiKeyReqTest_S3() {
		PartnerAPIKeyRequest req = createPartnerAPIKeyRequest();
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(false));		
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		pserviceImpl.submitPartnerApiKeyReq(req, partnerId);
	}
	
	@Test(expected = PartnerServiceException.class)
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
	
	@Test(expected = PartnerServiceException.class)
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
	
	
	@Test(expected = PartnerServiceException.class)
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
	
	@Test(expected = PartnerServiceException.class)
	public void retrieveAllApiKeyRequestsSubmittedByPartnerTest_S1() {
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner("12345");
	}	
	
	@Test
	public void retrieveAllApiKeyRequestsSubmittedByPartnerTest_S2() {
		String partnerId = "12345";
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Rejected");
		requests.add(request);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(Mockito.anyString(), Mockito.anyString())).thenReturn(requests);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerId)).thenReturn(requests);
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerId);
	}
	
	@Test
	public void retrieveAllApiKeyRequestsSubmittedByPartnerTest_S3() {
		String partnerId = "12345";
		List<PartnerPolicyRequest> requests = new ArrayList<PartnerPolicyRequest>();
		PartnerPolicyRequest request = createPartnerPolicyRequest("Approved");
		requests.add(request);
		PartnerPolicy policy = new PartnerPolicy();
		policy.setPolicyApiKey("abcd");
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now()));
		Mockito.when(partnerPolicyRepository.findByApiKey(Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(Mockito.anyString())).thenReturn(requests);
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerId);
	}
	
	@Test
	public void mapPartnerPolicyCredentialType_001() {		
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		Mockito.when(authPolicyRepository
				.findByPolicyGroupAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(createAuthPolicy());
		pserviceImpl.mapPartnerPolicyCredentialType("euin", "12345", "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void mapPartnerPolicyCredentialType_002() {		
		Optional<Partner> partner = Optional.of(createPartner(true));		
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("uin", "12345", "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
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
		partnerPolicy.setPolicyApiKey("12345");
		partnerPolicy.setIsActive(true);
		partnerPolicy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now()));
		return partnerPolicy;
	}

	private PartnerPolicyRequest createPartnerPolicyRequest(String statusCode) {
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setId("12345");
		partnerPolicyRequest.setPolicyId("12345");
		partnerPolicyRequest.setPartner(createPartner(Boolean.TRUE));
		partnerPolicyRequest.setStatusCode(statusCode);
		return partnerPolicyRequest;
	}

	private AuthPolicy createAuthPolicy() {
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("12345");
		authPolicy.setName("Banking");
		authPolicy.setValidToDate(LocalDateTime.now().plusYears(1));
		authPolicy.setPolicyGroup(createPolicyGroup(true));
		authPolicy.setIsActive(true);
		authPolicy.setPolicyFileId("{\"allowedAuthTypes\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true}],\"shareableAttributes\":[{\"encrypted\":false,\"format\":null,\"attributeName\":\"fullName\"},{\"encrypted\":false,\"format\":\"yyyy\",\"attributeName\":\"dateOfBirth\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"gender\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"phone\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"email\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"postalCode\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"face\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"finger\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"iris\"}],\"dataSharePolicies\":{\"transactionsAllowed\":\"2\",\"shareDomain\":\"mosip.io\",\"encryptionType\":\"partnerBased\",\"validForInMinutes\":\"30\",\"typeOfShare\":\"dataShare\"}}");
		return authPolicy;
	}
	
	private AuthPolicy createInactiveAuthPolicy() {
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("12345");
		authPolicy.setName("Banking");
		authPolicy.setValidToDate(LocalDateTime.now().plusYears(1));
		authPolicy.setPolicyGroup(createPolicyGroup(true));
		authPolicy.setIsActive(false);
		authPolicy.setPolicyFileId("{\"allowedAuthTypes\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true}],\"shareableAttributes\":[{\"encrypted\":false,\"format\":null,\"attributeName\":\"fullName\"},{\"encrypted\":false,\"format\":\"yyyy\",\"attributeName\":\"dateOfBirth\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"gender\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"phone\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"email\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"postalCode\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"face\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"finger\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"iris\"}],\"dataSharePolicies\":{\"transactionsAllowed\":\"2\",\"shareDomain\":\"mosip.io\",\"encryptionType\":\"partnerBased\",\"validForInMinutes\":\"30\",\"typeOfShare\":\"dataShare\"}}");
		return authPolicy;
	}
	
	private AuthPolicy createTimeInvariantAuthPolicy() {
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("12345");
		authPolicy.setName("Banking");
		authPolicy.setValidToDate(LocalDateTime.now().plusYears(-1));
		authPolicy.setPolicyGroup(createPolicyGroup(true));
		authPolicy.setIsActive(true);
		authPolicy.setPolicyFileId("{\"allowedAuthTypes\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true}],\"shareableAttributes\":[{\"encrypted\":false,\"format\":null,\"attributeName\":\"fullName\"},{\"encrypted\":false,\"format\":\"yyyy\",\"attributeName\":\"dateOfBirth\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"gender\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"phone\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"email\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"postalCode\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"face\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"finger\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"iris\"}],\"dataSharePolicies\":{\"transactionsAllowed\":\"2\",\"shareDomain\":\"mosip.io\",\"encryptionType\":\"partnerBased\",\"validForInMinutes\":\"30\",\"typeOfShare\":\"dataShare\"}}");
		return authPolicy;
	}

	private AuthPolicy createInactiveGroupAuthPolicy() {
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("12345");
		authPolicy.setName("Banking");
		authPolicy.setValidToDate(LocalDateTime.now().plusYears(1));
		authPolicy.setPolicyGroup(createPolicyGroup(false));
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
		partner.setId("12345");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("12345");
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
	
	private PartnerRequest createInvalidIDLengthPartnerRequest() {
		PartnerRequest prequest = new PartnerRequest();
		prequest.setAddress("addresss-1");
		prequest.setContactNumber("8273283283");
		prequest.setEmailId("xyz@gmail.com");
		prequest.setOrganizationName("airtel India");
		prequest.setPolicyGroup("Telecom sector");
		prequest.setPartnerId("abc1234dasjdsadhasukhdaskjhdksajhdksahkdjaskjdaksjdksad");
		prequest.setPartnerType("Auth");
		return prequest;
	}
	
	private PartnerRequest createInvalidEmailPartnerRequest() {
		PartnerRequest prequest = new PartnerRequest();
		prequest.setAddress("addresss-1");
		prequest.setContactNumber("8273283283");
		prequest.setEmailId("xyz");
		prequest.setOrganizationName("airtel India");
		prequest.setPolicyGroup("Telecom sector");
		prequest.setPartnerId("abc1234");
		prequest.setPartnerType("Auth");
		return prequest;
	}
	
	private PartnerRequest createInvalidPolicyGroupPartnerRequest() {
		PartnerRequest prequest = new PartnerRequest();
		prequest.setAddress("addresss-1");
		prequest.setContactNumber("8273283283");
		prequest.setEmailId("xyz@gmail.com");
		prequest.setOrganizationName("airtel India");
		prequest.setPolicyGroup(null);
		prequest.setPartnerId("abc1234");
		prequest.setPartnerType("Auth");
		return prequest;
	}
	

	private PolicyGroup createPolicyGroup(Boolean isActive) {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Banking");
		policyGroup.setId("12345");
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

	@SuppressWarnings("unused")
	private String caCertResponse() {
	 return "{\n"
	 		+ "\"id\": null,\n"
	 		+ "\"version\": null,\n"
	 		+ "\"responsetime\": \"2021-01-15T11:03:21.741Z\",\n"
	 		+ "\"metadata\": null,\n"
	 		+ "\"response\": {\n"
	 		+ "\"status\": \"Upload Success.\",\n"
	 		+ "\"timestamp\": \"2021-01-15T11:03:21.786597\"\n"
	 		+ "},\n"
	 		+ "\"errors\": null\n"
	 		+ "}"	;
	}
	

}