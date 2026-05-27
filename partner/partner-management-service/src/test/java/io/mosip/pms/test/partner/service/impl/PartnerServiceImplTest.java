package io.mosip.pms.test.partner.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.dto.*;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.DataShareDto;
import io.mosip.pms.partner.dto.DataShareResponseDto;
import io.mosip.pms.partner.dto.UploadCertificateRequestDto;
import io.mosip.pms.partner.dto.PartnerDtoV3;
import io.mosip.pms.partner.request.dto.*;
import io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2;
import io.mosip.pms.partner.response.dto.*;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.service.impl.PartnerServiceImpl;
import io.mosip.pms.partner.util.PartnerUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;
import io.mosip.pms.test.config.TestSecurityConfig;


/**
 * @author sanjeev.shrivastava
 *
 */
@SpringBootTest(classes =PartnerManagementServiceTest.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class PartnerServiceImplTest {

	private AuthUserDetails mockAuthUserDetails(String userId, String... authorities) {
		AuthUserDetails details = Mockito.mock(AuthUserDetails.class);
		when(details.getUserId()).thenReturn(userId);
		Collection auths = new ArrayList<GrantedAuthority>();
		if (authorities != null) {
			for (String a : authorities) {
				((Collection<GrantedAuthority>) auths).add(new SimpleGrantedAuthority(a));
			}
		}
		when(details.getAuthorities()).thenReturn(auths);
		return details;
	}
	
	@Value("${pmp.allowed.credential.types}")
	private String allowedCredentialTypes;
	
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
	PartnerHRepository partnerHRepository;
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
	PartnerPolicyBioextractRequestRepository partnerPolicyBioextractRequestRepository;
	@MockBean
	PartnerPolicyCredentialTypeRequestRepository partnerPolicyCredentialTypeRequestRepository;
	@MockBean
	PartnerPolicyCredentialTypeRepository partnerCredentialTypePolicyRepo;
	@MockBean
	private WebSubPublisher webSubPublisher;
	@MockBean
	UserDetailUtil userDetailUtil;
    @MockBean
	AuditUtil auditUtil;
	@MockBean
	PartnerHelper partnerHelper;
	@MockBean
	KeyManagerHelper keyManagerHelper;

    @Mock
	FilterHelper filterHelper;

	@Mock
	SecurityContext securityContext;

	@Mock
	Authentication authentication;

	@Mock
	Environment environment;

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
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		ReflectionTestUtils.setField(target, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(target, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(target, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(target, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(target, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(target, "partnerTypeRepository", partnerTypeRepository);
		ReflectionTestUtils.setField(target, "partnerHRepository", partnerHRepository);
		ReflectionTestUtils.setField(target, "extractorProviderRepository", extractorProviderRepository);
		ReflectionTestUtils.setField(target, "partnerPolicyBioextractRequestRepository", partnerPolicyBioextractRequestRepository);
		ReflectionTestUtils.setField(target, "partnerPolicyCredentialTypeRequestRepository", partnerPolicyCredentialTypeRequestRepository);
		ReflectionTestUtils.setField(target, "partnerCredentialTypePolicyRepo", partnerCredentialTypePolicyRepo);
		ReflectionTestUtils.setField(target, "partnerContactRepository", partnerContactRepository);
		ReflectionTestUtils.setField(target, "filterColumnValidator", filterColumnValidator);
		ReflectionTestUtils.setField(target, "partnerSearchHelper", partnerSearchHelper);
		ReflectionTestUtils.setField(target, "pageUtils", pageUtils);
		ReflectionTestUtils.setField(target, "filterHelper", filterHelper);
		ReflectionTestUtils.setField(target, "restUtil", restUtil);
		ReflectionTestUtils.setField(target, "environment", environment);
		ReflectionTestUtils.setField(target, "allowedCredentialTypes", allowedCredentialTypes);
		// ensure the internal mapper used by isJSONValid is set
		ObjectMapper serviceMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		ReflectionTestUtils.setField(target, "mapper", serviceMapper);

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
	
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(PartnerServiceAuditEnum.class));

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
	}

	@After
	public void tearDownSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}

	@Test(expected = PartnerServiceException.class) 
	public void getPartnerCertificate_Test() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "AUTH_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		PartnerCertDownloadRequestDto partnerCertDownloadRequestDto = new PartnerCertDownloadRequestDto();
		partnerCertDownloadRequestDto.setPartnerId("123");

		pserviceImpl.getPartnerCertificate(partnerCertDownloadRequestDto);
		Optional<Partner> getPartner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> policyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(getPartner);
		Mockito.when(policyGroupRepository.findById(getPartner.get().getPolicyGroupId())).thenReturn(policyGroup);
		
		PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = pserviceImpl.getPartnerCertificate(partnerCertDownloadRequestDto);
		assertNotNull(partnerCertDownloadResponeDto);
		assertEquals(partnerCertDownloadResponeDto.getCertificateData(), "12345");
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(),Mockito.any(),Mockito.any());
	}

	@Test
	public void getPartnerCertificateData_Test() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "AUTH_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		PartnerCertDownloadRequestDto partnerCertDownloadRequestDto = new PartnerCertDownloadRequestDto();
		partnerCertDownloadRequestDto.setPartnerId("abc");

		ResponseWrapperV2<OriginalCertDownloadResponseDto> originalCertDownloadResponseDto = pserviceImpl.getPartnerCertificateData(partnerCertDownloadRequestDto);
		assertNotNull(originalCertDownloadResponseDto);
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(),Mockito.any(),Mockito.any());

		partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		pserviceImpl.getPartnerCertificateData(partnerCertDownloadRequestDto);

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
		contactFromDB.setEmailId("email@gmail.com");

		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerRepository.findById(par.getId())).thenReturn(partner);
		Mockito.when(partnerContactRepository.findByPartnerAndEmailIdHash(Mockito.anyString(), Mockito.anyString())).thenReturn(contactFromDB);
	    pserviceImpl.createAndUpdateContactDetails(addContactRequestDto, par.getId());
	    addContactRequestDto.setEmailId("email");
	    try {
	    	pserviceImpl.createAndUpdateContactDetails(addContactRequestDto, par.getId());
	    }catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorCode()));
		}
	    addContactRequestDto.setEmailId("email@gamil.com");
	    addContactRequestDto.setContactNumber("09876543211234567");
	    try {
	    	pserviceImpl.createAndUpdateContactDetails(addContactRequestDto, par.getId());
	    }catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode()));
		}
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

		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		//Mockito.when(partnerContactRepository.findByPartnerAndEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(contactFromDB);
	    pserviceImpl.createAndUpdateContactDetails(addContactRequestDto, par.getId());
	}
	
	@Test
	public void searchPartnertest() throws Exception{
		objectMapper.writeValueAsString(partnerSearchDto);
		Partner partner = new Partner();
		partner.setId("id");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partner))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.anyString());
		pserviceImpl.searchPartner(partnerSearchDto);
	}
	
	@Test
	public void searchPartnertest01() throws Exception{
		objectMapper.writeValueAsString(partnerSearchDto1);
		Partner partner = new Partner();
		partner.setId("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partner))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.anyString());
		pserviceImpl.searchPartner(partnerSearchDto1);
	}
	
	@Test
	public void partnerFilterValues_Test() {
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(filtersData);
		pserviceImpl.filterValues(deviceFilterValueDto);
	}
	
	@Test
	public void apiKeyRequestFilterTest() {
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(filtersData);
		pserviceImpl.apiKeyRequestFilter(deviceFilterValueDto);
		}
	
	@Test
	@WithMockUser(roles = {"PARTNER"})
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
		Mockito.when(partnerHelper.getValidPartner(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(policyGroupRepository.findById(par.getPolicyGroupId())).thenReturn(findByIdpolicyGroup);
		pserviceImpl.getPartnerDetails(par.getName());
	}

	@Test (expected = PartnerServiceException.class)
	@WithMockUser(roles = {"PARTNER"})
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
		Mockito.when(partnerHelper.getValidPartner(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(partner.get());
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
		Mockito.when(partnerHelper.getValidPartner(Mockito.anyString(), Mockito.anyBoolean())).thenThrow(new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(), ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage()));
		Mockito.when(policyGroupRepository.findById(par.getPolicyGroupId())).thenReturn(findByIdpolicyGroup);
		pserviceImpl.getPartnerDetails(par.getName());
	}

	@Test
	@WithUserDetails("partner")
	public void savePartnerTest() {
		MosipUserDto userDto = new MosipUserDto();
		userDto.setName("PARTNER");
		userDto.setMobile("partner@gmail.com");		
		PolicyGroup policyGroup = createPolicyGroup(Boolean.TRUE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Partner partner = new Partner();
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		Mockito.when(partnerRepository.findByName("Airtel")).thenReturn(partner);
		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));
		PartnerResponse savePartner = pserviceImpl.savePartner(partnerRequest);
		assertNotNull(savePartner);
		
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(null);
		try {
			pserviceImpl.savePartner(partnerRequest);
		}catch(PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode()));
		}		
		policyGroup.setIsActive(false);
		Mockito.when(policyGroupRepository.findByName(partnerRequest.getPolicyGroup())).thenReturn(policyGroup);
		try {
			pserviceImpl.savePartner(partnerRequest);
		}catch(PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode()));
		}
		
		partnerRequest.setContactNumber("09876543212345889989");
		try {
			pserviceImpl.savePartner(partnerRequest);
		}catch(PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode()));
		}		
		partnerRequest.setContactNumber("0987654321234588");
		partnerRequest.setLangCode("iuy");
		try {
			pserviceImpl.savePartner(partnerRequest);
		}catch(PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_LANG_CODE_NOT_SUPPORTED.getErrorCode()));
		}
		partnerRequest.setContactNumber("0987654321234588");
		partnerRequest.setLangCode("iuy");
		partnerRequest.setPartnerId("auth partner");
		try {
			pserviceImpl.savePartner(partnerRequest);
		}catch(PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_ID_CONTAINS_SPACES.getErrorCode()));
		}
		
	}
	
	@Test(expected = PartnerServiceException.class)
	public void savePartnerTest01() {
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
	@WithMockUser(roles = {"PARTNER"})
	public void getPartnerDetailsTest() {
		Optional<Partner> partner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> policyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerHelper.getValidPartner(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(policyGroupRepository.findById(partner.get().getPolicyGroupId())).thenReturn(policyGroup);
		RetrievePartnerDetailsResponse partnerDetails = pserviceImpl.getPartnerDetails("12345");
		assertNotNull(partnerDetails);
		assertEquals(partnerDetails.getContactNumber(), "47384384");
		assertEquals(partnerDetails.getEmailId(), "xyz@hotmail.com");
		assertEquals(partnerDetails.getAddress(), "address");
	}

	@Test(expected = PartnerServiceException.class)
	@WithMockUser(roles = {"PARTNER"})
	public void throwExceptionWhenPartnerDetailsIsEmptyTest() {
		Mockito.when(partnerHelper.getValidPartner(Mockito.anyString(), Mockito.anyBoolean())).thenThrow(new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(), ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage()));
		pserviceImpl.getPartnerDetails("12345");
	}

	@Test
	@WithMockUser(roles = {"PARTNER"})
	public void updatePartnerDetailsTest_S1() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));

		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq(partnerId), Mockito.anyBoolean())).thenReturn(partner.get());
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "12345");
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		req.setContactNumber("1234567890123456789");
		try {
			pserviceImpl.updatePartnerDetail(req, partnerId);
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode()));
		}
	}
	
	@Test(expected = PartnerServiceException.class)
	public void updatePartnerDetailTest_S2() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";
		Mockito.when(partnerHelper.getValidPartner(Mockito.anyString(), Mockito.anyBoolean())).thenThrow(new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(), ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage()));
		pserviceImpl.updatePartnerDetail(req, partnerId);
	}
	
	@Test	
	@WithMockUser(roles = {"PARTNER"})
	public void updatePartnerDetailTest_S3() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";
		Partner part = createPartner(Boolean.TRUE);
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq(partnerId), Mockito.anyBoolean())).thenReturn(partner.get());
		pserviceImpl.updatePartnerDetail(req, partnerId);
	}
	
	@Test
	@WithMockUser(roles = {"PARTNER"})
	public void updatePartnerDetailTest_S4() {
		PartnerUpdateRequest req = createPartnerUpdateRequest();
		String partnerId = "12345";
		Partner part = createPartner(Boolean.TRUE);
		part.setName("name");
		Optional<Partner> partner = Optional.of(part);
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq(partnerId), Mockito.anyBoolean())).thenReturn(partner.get());
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
	@WithMockUser(roles = {"PARTNER"})
	public void doNotSetstatusWhenPartnerIsDeactiveTest() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq(partnerId), Mockito.anyBoolean())).thenReturn(partner.get());
		PartnerResponse updatePartnerDetail = pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
		assertNotNull(updatePartnerDetail);
		assertEquals(updatePartnerDetail.getPartnerId(), "12345");
	}

	@Test(expected = PartnerServiceException.class)
	public void doNotUpdaePartnerWhenPartnerDetailsIsEmptyTest() {
		String partnerId = "12345";
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq(partnerId), Mockito.anyBoolean())).thenThrow(new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(), ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage()));
		pserviceImpl.updatePartnerDetail(createPartnerUpdateRequest(), partnerId);
	}
	
	@Test(expected = PartnerServiceException.class)
	public void throwExceptionWhenPartnerPolicyRequestIsEmptyTest() {
		String partnerID = "id";
		List<PartnerPolicyRequest> partnerPolicyRequest = new ArrayList<>();
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(partnerID)).thenReturn(partnerPolicyRequest);
		pserviceImpl.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
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
	
	@Test(expected = PartnerServiceException.class)
	public void mapPartnerPolicyCredentialType_001() {
		Optional<Partner> partner = Optional.of(createPartner(true));
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		Mockito.when(authPolicyRepository
				.findByPolicyGroupIdAndName(Mockito.anyString(), Mockito.anyString())).thenReturn(createAuthPolicy());
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
		Mockito.when(partnerHelper.getValidPartner(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("euin", "1234578", "12345");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void mapPartnerPolicyCredentialType_004() {
		Optional<Partner> partner = Optional.of(createPartner(true));
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(createAuthPolicy()));
		pserviceImpl.mapPartnerPolicyCredentialType("euin", "12345", "12345678");
	}
	
	@Test(expected = PartnerServiceException.class)
	public void mapPartnerPolicyCredentialType_005() {
		Optional<Partner> partner = Optional.of(createPartner(true));
		partner.get().setPartnerTypeCode("Auth_Partner");
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenReturn(partner.get());
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

	@Test (expected = PartnerServiceException.class)
	public void requestForPolicyMappingTest() {
		PartnerPolicyMappingRequest request = new PartnerPolicyMappingRequest();
		request.setPolicyName("policyName");
		request.setUseCaseDescription("Use cases Details");
		Optional<Partner> partner = Optional.of(createPartner(true));
		partner.get().setPartnerTypeCode("Auth_Partner");
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","policyName")).thenReturn(createAuthPolicy());
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId("12345","123")).thenReturn(List.of(createPartnerPolicyRequest("approved")));
		pserviceImpl.requestForPolicyMapping(request, "12345");
		partner.get().setPolicyGroupId(null);
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_GROUP.getErrorCode()));
		}
		partner.get().setPolicyGroupId("12345");
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId("12345","12345")).thenReturn(List.of(createPartnerPolicyRequest("approved")));
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_MAPPING_APPROVED.getErrorCode()));
		}
		partner.get().setPolicyGroupId("12345");
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId("12345","12345")).thenReturn(List.of(createPartnerPolicyRequest(PartnerConstants.IN_PROGRESS)));
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_MAPPING_INPROGRESS.getErrorCode()));
		}

		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenThrow(new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(), ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage()));
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode()));
		}
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenThrow(new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(), ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage()));
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode()));
		}
		partner.get().setIsActive(true);
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","policyName")).thenReturn(null);
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_POLICY_NOT_EXISTS.getErrorCode()));
		}
		AuthPolicy authPolicy = createAuthPolicy();
		authPolicy.setIsActive(false);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","policyName")).thenReturn(authPolicy);
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode()));
		}		
		authPolicy = createAuthPolicy();
		authPolicy.setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().minusMonths(3));
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","policyName")).thenReturn(authPolicy);
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_EXPIRED_EXCEPTION.getErrorCode()));
		}
		authPolicy = createAuthPolicy();
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(false);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","policyName")).thenReturn(authPolicy);
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode()));
		}
	}
	
	@Test
	public void isPartnerExistsWithEmailTest() {
		Mockito.when(partnerRepository.findByEmailId(Mockito.anyString())).thenReturn(createPartner(true));
		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));
		assertTrue(pserviceImpl.isPartnerExistsWithEmail("test@gmail.com").getPolicyRequiredPartnerTypes().contains("AUTH"));
		try {
			pserviceImpl.isPartnerExistsWithEmail("test");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorCode()));
		}
	}
	
	@Test
	public void updatePolicyGroupTest() {
		Optional<Partner> partner = Optional.of(createPartner(true));
		partner.get().setPartnerTypeCode("Auth");
		partner.get().setIsActive(false);
		Mockito.when(partnerHelper.getValidPartner(Mockito.eq("12345"), Mockito.anyBoolean())).thenReturn(partner.get());
		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));
		Mockito.when(policyGroupRepository.findByName("policygroupname")).thenReturn(createPolicyGroup(true));
		assertTrue(pserviceImpl.updatePolicyGroup("12345","policygroupname").equals("Success"));
		partner.get().setIsActive(true);
		try {
			pserviceImpl.updatePolicyGroup("12345","policygroupname");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_MAP_ACTIVE_PARTNER.getErrorCode()));
		}
		partner.get().setIsActive(true);
		partner.get().setPartnerTypeCode("Auth_Partner");
		try {
			pserviceImpl.updatePolicyGroup("12345","policygroupname");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_MAP_ACTIVE_PARTNER.getErrorCode()));
		}
		partner.get().setIsActive(false);
		partner.get().setPartnerTypeCode("Auth");
		PartnerType partnerType = getPartnerType();
		partnerType.setIsPolicyRequired(false);
		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(partnerType));
		try {
			pserviceImpl.updatePolicyGroup("12345","policygroupname");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_REQUIRED.getErrorCode()));
		}
	}
	
	@Test
	public void searchPartnerApiKeyRequestsTest() throws Exception {
		SearchFilter partnerNameSearchFilter = new SearchFilter();
		partnerNameSearchFilter.setColumnName("partnerName");
		partnerNameSearchFilter.setValue("m");
		
		SearchFilter apikeyRequestIdSearchFilter = new SearchFilter();
		apikeyRequestIdSearchFilter.setColumnName("apikeyRequestId");
		apikeyRequestIdSearchFilter.setValue("m");
		
		SearchFilter policyNameRequestIdSearchFilter = new SearchFilter();
		policyNameRequestIdSearchFilter.setColumnName("policyName");
		policyNameRequestIdSearchFilter.setValue("m");
		
		SearchFilter partnerIdRequestIdSearchFilter = new SearchFilter();
		partnerIdRequestIdSearchFilter.setColumnName("partnerId");
		partnerIdRequestIdSearchFilter.setValue("m");
		
		searchDto.getFilters().add(partnerIdRequestIdSearchFilter);
		searchDto.getFilters().add(policyNameRequestIdSearchFilter);
		searchDto.getFilters().add(apikeyRequestIdSearchFilter);
		searchDto.getFilters().add(partnerNameSearchFilter);
		objectMapper.writeValueAsString(searchDto);
		PartnerPolicyRequest partnerPolicy = new PartnerPolicyRequest();
		partnerPolicy.setId("12345");
		partnerPolicy.setPartner(createPartner(true));
		Mockito.when(authPolicyRepository.findByNameIgnoreCase("m")).thenReturn(List.of(createAuthPolicy()));
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partnerPolicy))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any());
		pserviceImpl.searchPartnerApiKeyRequests(searchDto);		
		
		Mockito.when(authPolicyRepository.findByNameIgnoreCase("m")).thenReturn(Collections.emptyList());
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partnerPolicy))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any());
		pserviceImpl.searchPartnerApiKeyRequests(searchDto);
		
		searchDto.getFilters().add(partnerNameSearchFilter);
		objectMapper.writeValueAsString(searchDto);
		pserviceImpl.searchPartnerApiKeyRequests(searchDto);
		
		searchDto.getFilters().add(partnerIdRequestIdSearchFilter);
		objectMapper.writeValueAsString(searchDto);
		pserviceImpl.searchPartnerApiKeyRequests(searchDto);
		
		objectMapper.writeValueAsString(searchDto);
		pserviceImpl.searchPartnerApiKeyRequests(searchDto);
	}
	
	@Test
	public void searchPartnerApiKeysTest() throws JsonProcessingException {
		SearchFilter partnerNameSearchFilter = new SearchFilter();
		partnerNameSearchFilter.setColumnName("partnerName");
		partnerNameSearchFilter.setValue("m");
		
		SearchFilter apikeyRequestIdSearchFilter = new SearchFilter();
		apikeyRequestIdSearchFilter.setColumnName("apikeyRequestId");
		apikeyRequestIdSearchFilter.setValue("m");
		
		SearchFilter policyNameRequestIdSearchFilter = new SearchFilter();
		policyNameRequestIdSearchFilter.setColumnName("policyName");
		policyNameRequestIdSearchFilter.setValue("m");
		
		SearchFilter partnerIdRequestIdSearchFilter = new SearchFilter();
		partnerIdRequestIdSearchFilter.setColumnName("partnerId");
		partnerIdRequestIdSearchFilter.setValue("m");
		
		searchDto.getFilters().add(partnerIdRequestIdSearchFilter);
		searchDto.getFilters().add(policyNameRequestIdSearchFilter);
		searchDto.getFilters().add(apikeyRequestIdSearchFilter);
		searchDto.getFilters().add(partnerNameSearchFilter);
		objectMapper.writeValueAsString(searchDto);
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey("12345");
		partnerPolicy.setPartner(createPartner(true));
		Mockito.when(partnerRepository.findById("m")).thenReturn(Optional.of(createPartner(true)));
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partnerPolicy))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any());
		Mockito.when(authPolicyRepository.findByName("m")).thenReturn((createAuthPolicy()));
		pserviceImpl.searchPartnerApiKeys(searchDto);
		
		searchDto.getFilters().add(partnerNameSearchFilter);
		objectMapper.writeValueAsString(searchDto);
		pserviceImpl.searchPartnerApiKeys(searchDto);
		
		searchDto.getFilters().add(partnerIdRequestIdSearchFilter);
		objectMapper.writeValueAsString(searchDto);
		pserviceImpl.searchPartnerApiKeys(searchDto);
		
		objectMapper.writeValueAsString(searchDto);
		pserviceImpl.searchPartnerApiKeys(searchDto);
	}	
  
	@Test
	public void searchPartnerTypeTest() throws JsonProcessingException {
		objectMapper.writeValueAsString(searchDto);
		PartnerType partnerType = new PartnerType();
		partnerType.setCode("Auth_Partner");
		partnerType.setIsDeleted(false);
		Mockito.when(partnerRepository.findById("m")).thenReturn(Optional.of(createPartner(true)));
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partnerType))).when(partnerSearchHelper).search(Mockito.any(),Mockito.any(),Mockito.any());
		Mockito.when(authPolicyRepository.findByName("m")).thenReturn((createAuthPolicy()));
		pserviceImpl.searchPartnerType(searchDto);
	}

	@Test
	public void testUploadPartnerCertificate_Success() throws Exception {
		PartnerCertificateResponseDto expectedResponse = new PartnerCertificateResponseDto();
		expectedResponse.setCertificateId("123");
		expectedResponse.setSignedCertificateData(urlSafeBase64DerFromGeneratedCert());
		expectedResponse.setTimestamp(LocalDateTime.now());
		Map<String, Object> apiResponse = Collections.singletonMap("response", expectedResponse);
		when(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"))
				.thenReturn("https://localhost/v1/keymanager/uploadPartnerCertificate");
		when(restUtil.postApi(anyString(), any(), anyString(), anyString(),
				any(), any(), eq(Map.class))).thenReturn(apiResponse);

		PartnerCertificateUploadRequestDto requestDto = new PartnerCertificateUploadRequestDto();
		requestDto.setPartnerId("Partner");
		// Use FTM domain so uploadOtherDomainCertificate branch is skipped
		requestDto.setPartnerDomain("FTM");
		requestDto.setCertificateData("cert_data");
		when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		Partner p = createPartner(true);
		p.setId("Partner");
		p.setApprovalStatus(PartnerConstants.IN_PROGRESS);
		p.setIsActive(false);
		p.setName("Org");
		p.setPartnerTypeCode("Auth_Partner");
		p.setPolicyGroupId("pg1");
		when(partnerHelper.getValidPartner(eq("Partner"), anyBoolean())).thenReturn(p);
		PartnerType pt = new PartnerType();
		pt.setCode("Auth_Partner");
		pt.setIsPolicyRequired(false);
		when(partnerTypeRepository.findAll()).thenReturn(List.of(pt));
		when(environment.getProperty("pmp.certificaticate.datashare.rest.uri"))
				.thenReturn("https://localhost/ds");
		when(restUtil.postApi(any(), anyList(), any(), any(), any(), any(), eq(DataShareResponseDto.class)))
				.thenReturn(dataShareResponse("https://ds"));
		PartnerCertificateResponseDto actualResponse = pserviceImpl.uploadPartnerCertificate(requestDto);

		assertNotNull(actualResponse);
		assertTrue(actualResponse.getSignedCertificateData().contains("-----BEGIN CERTIFICATE-----"));
		assertTrue(actualResponse.getSignedCertificateData().contains("-----END CERTIFICATE-----"));
		verify(restUtil).postApi(eq("https://localhost/v1/keymanager/uploadPartnerCertificate"), isNull(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class));
	}

	@Test
	public void testUploadCACertificate_Success() throws Exception {

		CACertificateResponseDto expectedResponse = new CACertificateResponseDto();
		expectedResponse.setStatus("Active");
		expectedResponse.setTimestamp(LocalDateTime.now());
		Map<String, Object> apiResponse = Collections.singletonMap("response", expectedResponse);
		when(environment.getProperty("pmp.ca.certificaticate.upload.rest.uri"))
				.thenReturn("https://localhost/v1/keymanager/uploadCACertificate");
		when(restUtil.postApi(eq("https://localhost/v1/keymanager/uploadCACertificate"), any(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class))).thenReturn(apiResponse);

		CACertificateRequestDto requestDto = new CACertificateRequestDto();
		requestDto.setCertificateData("cert_data");
		requestDto.setPartnerDomain("Auth");
		CACertificateResponseDto actualResponse = pserviceImpl.uploadCACertificate(requestDto);

		assertEquals(expectedResponse.getStatus(), actualResponse.getStatus());
		verify(restUtil).postApi(eq("https://localhost/v1/keymanager/uploadCACertificate"), isNull(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class));
	}

	@Test
	public void uploadCACertificate_whenErrorsNonEmpty_throwsApiAccessibleException() throws Exception {
		when(environment.getProperty("pmp.ca.certificaticate.upload.rest.uri"))
				.thenReturn("https://localhost/v1/keymanager/uploadCACertificate");
		Map<String, Object> err = new HashMap<>();
		err.put(PartnerConstants.ERRORCODE, "E1");
		err.put(PartnerConstants.ERRORMESSAGE, "bad");
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		apiResponse.put(PartnerConstants.ERRORS, List.of(err));
		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), eq(Map.class)))
				.thenReturn(apiResponse);
		try {
			pserviceImpl.uploadCACertificate(new CACertificateRequestDto());
			fail("expected ApiAccessibleException");
		} catch (ApiAccessibleException e) {
			assertEquals("E1", e.getErrorCode());
		}
	}

	@Test
	public void uploadCACertificate_whenErrorsEmpty_throwsUnableToProcess() throws Exception {
		when(environment.getProperty("pmp.ca.certificaticate.upload.rest.uri"))
				.thenReturn("https://localhost/v1/keymanager/uploadCACertificate");
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		apiResponse.put(PartnerConstants.ERRORS, Collections.emptyList());
		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), eq(Map.class)))
				.thenReturn(apiResponse);
		try {
			pserviceImpl.uploadCACertificate(new CACertificateRequestDto());
			fail("expected ApiAccessibleException");
		} catch (ApiAccessibleException e) {
			assertEquals(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void uploadCACertificate_whenNullResponseAndNoErrors_throwsNullResponse() throws Exception {
		when(environment.getProperty("pmp.ca.certificaticate.upload.rest.uri"))
				.thenReturn("https://localhost/v1/keymanager/uploadCACertificate");
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), eq(Map.class)))
				.thenReturn(apiResponse);
		try {
			pserviceImpl.uploadCACertificate(new CACertificateRequestDto());
			fail("expected ApiAccessibleException");
		} catch (ApiAccessibleException e) {
			assertEquals(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(), e.getErrorCode());
		}
	}

	@Test (expected = Exception.class)
	public void testUploadOtherDomainCertificate_Success() throws IOException {
		String signedCertificateData = "signed_certificate_data";
		String partnerId = "partner_id";

		CACertificateResponseDto expectedResponse = new CACertificateResponseDto();
		expectedResponse.setStatus("Active");
		expectedResponse.setTimestamp(LocalDateTime.now());

		Map<String, Object> apiResponse = Collections.singletonMap("response", expectedResponse);
		when(restUtil.postApi(any(), any(), any(), any(), any(), any(), any())).thenReturn(apiResponse);

		UploadCertificateRequestDto requestDto = new UploadCertificateRequestDto();
		requestDto.setApplicationId("App_Id");
		requestDto.setCertificateData("cert_data");
		requestDto.setReferenceId("Ref_Id");

		CACertificateResponseDto actualResponse = ReflectionTestUtils.invokeMethod(pserviceImpl,"uploadOtherDomainCertificate", signedCertificateData, partnerId);
		assertEquals(expectedResponse, actualResponse);
		verify(restUtil).postApi(any(), any(), any(), any(), any(), any(), any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testIsJSONValid_ValidJSON() {
		String validJson = "{\"key\": \"value\"}";
		try {
			ReflectionTestUtils.invokeMethod(pserviceImpl,"isJSONValid", validJson);
			assertTrue("No exception should be thrown for valid JSON", true);
		} catch (Exception e) {
			throw new PartnerServiceException();
		}
	}

	@Test (expected = Exception.class)
	public void testNotify_CertDataAndDomain() {
		String certData = "certificate_data";
		String partnerDomain = "partner_domain";

		ReflectionTestUtils.invokeMethod(pserviceImpl,"notify",certData,partnerDomain);

		Map<String, Object> data = new HashMap<>();
		data.put(PartnerConstants.CERT_CHAIN_DATA_SHARE_URL, certData);
		data.put(PartnerConstants.PARTNER_DOMAIN, partnerDomain);
		verify(webSubPublisher).notify(EventType.CA_CERTIFICATE_UPLOADED, data, getType());
	}

	@Test (expected = Exception.class)
	public void testNotify_PartnerDataPublishDto() {
		PartnerDataPublishDto partnerDataPublishDto = new PartnerDataPublishDto();
		partnerDataPublishDto.setCertificateData("Certificate Data");
		partnerDataPublishDto.setPartnerId("PartnerId");
		partnerDataPublishDto.setPartnerName("Partner Name");
		partnerDataPublishDto.setPartnerStatus("Partner Status");

		ReflectionTestUtils.invokeMethod(pserviceImpl,"notify", partnerDataPublishDto, EventType.PARTNER_UPDATED);

		Map<String, Object> data = new HashMap<>();
		data.put(PartnerConstants.PARTNER_DATA, partnerDataPublishDto);
		verify(webSubPublisher).notify(EventType.PARTNER_UPDATED, data, getType());
	}

	@Test
	public void testGetPartnerCertFromChain_success_returnsPem() throws Exception {
		String certChain = urlSafeBase64DerFromGeneratedCert();
		String pem = ReflectionTestUtils.invokeMethod(pserviceImpl, "getPartnerCertFromChain", certChain);
		assertNotNull(pem);
		assertTrue(pem.startsWith("-----BEGIN CERTIFICATE-----"));
		assertTrue(pem.contains("-----END CERTIFICATE-----"));
	}

	@Test (expected = Exception.class)
	public void testGetDataShareurl_Success() {
		String certsChain = "certs_chain";
		String expectedUrl = "https://example.com/data-share";

		DataShareResponseDto response = new DataShareResponseDto();
		DataShareDto dataShare = new DataShareDto();
		dataShare.setUrl(expectedUrl);
		response.setDataShare(dataShare);
		when(restUtil.postApi(any(), anyList(), any(), any(), any(), any(), any())).thenReturn(response);

		String actualUrl = ReflectionTestUtils.invokeMethod(pserviceImpl,"getDataShareurl",certsChain);

		assertEquals(expectedUrl, actualUrl);
		verify(restUtil).postApi(any(), anyList(), any(), any(), any(), any(), any());
	}

	private Type getType() {
		Type type = new Type();
		type.setName("PartnerServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.PartnerServiceImpl");
		return type;
	}

	private static String urlSafeBase64DerFromGeneratedCert() throws Exception {
		byte[] der = derFromPem(SAMPLE_CERT_PEM);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(der);
	}

	private static final String SAMPLE_CERT_PEM =
			"-----BEGIN CERTIFICATE-----\n"
					+ "MIICwjCCAaqgAwIBAgIJAKV0MQ7/M2RHMA0GCSqGSIb3DQEBDAUAMA8xDTALBgNV\n"
					+ "BAMTBFRlc3QwHhcNMjYwNDE3MTAzMzIwWhcNMzYwNDE0MTAzMzIwWjAPMQ0wCwYD\n"
					+ "VQQDEwRUZXN0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy1cuDuJP\n"
					+ "H0ET89T3yxh0Pw0BkB9b/LeNKNIzPK9fMKcXtE9iNikjEPV4JN3rBH0jMmPRnQZ2\n"
					+ "BiB+nLlxdNrfr/FFcsFupE3YgDjaCf2vzRXvI9/ZMa1Vs1muDIxfHDO3yftFwcu7\n"
					+ "ZXDQgodtWzRwMvosBOR145xPXWfxPK2j0WYeB323wjwUBK/HlLHL51f3MZ6IIP1k\n"
					+ "7tSNdN/QS8E0W9BdghiMSHa+5avtJ4ajbdKquzZHR+ebDBxxn1CxZpVwUuW6U/FY\n"
					+ "23YVZdEZEHsXnoPIgJDodym1NbBQ2MRnvyx1oJ4cIneoljtlwlqRfX8hU98d08Rv\n"
					+ "SqnPpoBNEgfkfwIDAQABoyEwHzAdBgNVHQ4EFgQUT364fSxFU2JrqjO4OC2RRNnN\n"
					+ "N2UwDQYJKoZIhvcNAQEMBQADggEBAF8uX6hMdQrbOzw2d7WEIy9o21IqOZyCWRTo\n"
					+ "MLAf28goKV7+Ow0y44NElevl0bF2TEvr/U2ghbUtzuleyFMEVGohwpQ3ILIFfGdF\n"
					+ "WK/nKbzSw0h+AF1XQEocAhGSDtSshAKaln97gNu/2NGYKfNkTmD1wUY9GHIEL563\n"
					+ "tuAuyd8rQof6F0CXw+Cf/nsGQ8Jf5LTZ9tzIzwjsHUIgOMG+ShCEFG5YG3dDMZNC\n"
					+ "/tB2BeJRyfGwkJpykaQIWIYPDNPAjvAtcrzT21hUZa44poatfoxbC0jHikBDtI/8\n"
					+ "rNvfqQGsH/gzAXRoaiqS35F938gF3hGcr8rAeGhvKFI64WHRLtE=\n"
					+ "-----END CERTIFICATE-----\n";

	private static byte[] derFromPem(String pem) {
		String base64 = pem
				.replace("-----BEGIN CERTIFICATE-----", "")
				.replace("-----END CERTIFICATE-----", "")
				.replaceAll("\\s", "");
		return Base64.getDecoder().decode(base64);
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
		partner.setUserId("partner");
		partner.setPolicyGroupId("12345");
		partner.setIsActive(isActive);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
		partner.setPartnerTypeCode("Credential_Partner");
		partner.setApprovalStatus("Approved");
		partner.setCrBy("Partner");
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

	private PartnerRequestDto buildPartnerRegisterDto(String email, String partnerId, JsonNode additionalInfo)
			throws Exception {
		PartnerRequestDto dto = new PartnerRequestDto();
		dto.setPartnerId(partnerId);
		dto.setPolicyGroup("Telecom sector");
		dto.setOrganizationName("Org Inc");
		dto.setAddress("Registered address line one");
		dto.setContactNumber("9876543210");
		dto.setEmailId(email);
		dto.setPartnerType("Auth");
		dto.setLangCode(null);
		dto.setAdditionalInfo(additionalInfo);
		return dto;
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

	@Test
	public void getPartnerCertificatesDetailsTest() throws Exception {

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setCertificateAlias("abs");
		partner.setApprovalStatus("approved");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String certificate = "-----BEGIN CERTIFICATE-----\n" +
				"MIIFfTCCA2WgAwIBAgIUOVZNyD46U0OAEhaGC/Y7NXbu+OkwDQYJKoZIhvcNAQEL\n" +
				"BQAwTjELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAk1IMQswCQYDVQQHDAJQTjELMAkG\n" +
				"A1UECgwCQ0ExCzAJBgNVBAsMAkNBMQswCQYDVQQDDAJDQTAeFw0yNDA1MDkwNzI1\n" +
				"MDJaFw0yOTA1MDkwNzI1MDJaME4xCzAJBgNVBAYTAklOMQswCQYDVQQIDAJNSDEL\n" +
				"MAkGA1UEBwwCUE4xCzAJBgNVBAoMAkNBMQswCQYDVQQLDAJDQTELMAkGA1UEAwwC\n" +
				"Q0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCzdWD2DvhSnmLqU3fX\n" +
				"RT3z8ikS6qHxn5Hu/a2ijkuZxAZj0UCUJ83kM20NwocJDHT1qx6+yjdl+BECsgoI\n" +
				"ro9MXgFOsHCphyR5KiP4mY95qRlE03h7WBfr4wDn/6f5tCbqCcBqdXMAQxUp34D+\n" +
				"Pro0EwkXNulHNMTvz5hpoCEiGyfXUP48I4q2nb8rMXaplhqz+vAYgA4rsK6K9IUh\n" +
				"uJDxtZRHdIfxnvbfjxDbuPkN0ehOQ1uQrDVY6ENCIUxdgR/p94kZ+CNsD21c57gJ\n" +
				"2wYg+BceQn1rVSGnfpqMoogZCMUWFvaE4i91419VXxDLgeC/4Qw8n5onBY+dVHjW\n" +
				"04OolR2DqotFyaPlZiVdpUys6+KZ7fS9mwWEY0kqtLzcBeb4g4nPvObfKnqSmVMZ\n" +
				"DHRuAx6MG3oFZrnNuS6oIYGwLpoko6iqEiGohHsSxMulT43XOxoNgDq9noQc9SYv\n" +
				"tzdzijBRLAxNBDTB0rgZra27tLIFlqP1TpqZtM3ThOmPJQn6JG8WeiVWnmUkpmXX\n" +
				"6opGqhLWMM/u1n4fdf716h7340RbCPJoOpTPphYo/WedFQskqZvhTU6HMIj4JQAj\n" +
				"OVVwgtrDOdx051ps2hhiSU5tL4LmjLHIsfyoCSuHkzBhVMZ/jKFm8C4Or2RRG85A\n" +
				"wtzEANSxVZRjw6S1hsHsI+8m2QIDAQABo1MwUTAdBgNVHQ4EFgQUjDli1GMiclHK\n" +
				"igNm2kuKh48AON8wHwYDVR0jBBgwFoAUjDli1GMiclHKigNm2kuKh48AON8wDwYD\n" +
				"VR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAk6IWcDdBc1tngCaPNLhU\n" +
				"c3pXRdTjDuLHMxHRiP/7Vi3V2xcKRak5ZMzYAJK6YThp3Z04V9d5jJoi/CDhMuPK\n" +
				"RV1GmbdA7b24Jic2fQHWOJkgafT2Gx4yHmLo5ctSuDHPfSvzUgeghG0k3eNJgCai\n" +
				"Ctr+wvCRZGvvbl2JnJUcWiHBxH/PaWJ4Jd1T4UKmhlFhTw26TXQGHuW/UJwgh8OR\n" +
				"V8A+WeMXxKFsh38b8RnWVa6XdajIq9UAZvvd4Q16zjdnMWx/7zcIK5D1MDb/KmSJ\n" +
				"yho1LKRZx5YtSeI4FWs8dzZ0nCCiTe7TrnnhlXThJ6rXeo5AshtM4fGrvizaf4n3\n" +
				"7I9mJkqiccp1ml+2EcgsdX7HbnGE/R8VVbh3jUhWHuysLCiVSMbjnktCLWoXjSb9\n" +
				"JqOYF3yo6JQslQB0fQMyKmvsn/FplQBbU0PUrg9vpAg9nZlZf3UHO5z072pXD6ky\n" +
				"5pKjh+q0JOk00Eln9AoU6YuIyPBQ9mI3X8iYB5UhUBbgAPeg1pwWCWhdt40f0D5t\n" +
				"JkVnICy+Gh1ps8QPA6coEaajbIq14Uh6eYEwxFHPsxlbn7pzjoCJG2v7N8VwgfuL\n" +
				"DdGs4hFikdUAfBT/Diug/n9/ZgfdN6Ctf4U/SM65vZvfRqtLIoTIs4PcF3YtKK04\n" +
				"m0UA3Sxxre0vVWYO4GmmZUY=\n" +
				"-----END CERTIFICATE-----";

		PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = new PartnerCertDownloadResponeDto();
		partnerCertDownloadResponeDto.setCertificateData(certificate);
		partnerCertDownloadResponeDto.setTimestamp(LocalDateTime.now());
		when(partnerHelper.getCertificate(anyString(), anyString(), any())).thenReturn(partnerCertDownloadResponeDto);
		pserviceImpl.getPartnerCertificatesDetails(null);

		pserviceImpl.getPartnerCertificatesDetails(30);

		partnerList = new ArrayList<>();
		partner.setIsActive(false);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		pserviceImpl.getPartnerCertificatesDetails(null);
	}

	@Test
	public void getPartnerCertificatesDetailsTestException() throws Exception {
		pserviceImpl.getPartnerCertificatesDetails(null);
	}

	@Test
	public void getPartnerCertificatesDetailsTestException1() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("");
		partner.setCertificateAlias("");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		pserviceImpl.getPartnerCertificatesDetails(null);
	}

	@Test
	public void getPartnerCertificatesDetailsTestException2() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		pserviceImpl.getPartnerCertificatesDetails(null);
	}

	@Test
	public void getPartnerCertificatesDetailsTestException3() throws Exception {

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setCertificateAlias("abs");
		partner.setApprovalStatus("approved");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.getCertificate(anyString(), anyString(), any())).thenThrow(new ApiAccessibleException("test", "test"));
		pserviceImpl.getPartnerCertificatesDetails(null);
	}

	@Test
	public void getPartnersV3Test() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setPolicyGroupId("abc");
		partner.setApprovalStatus("approved");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findPartnersByUserIdAndStatusAndPartnerTypeAndPolicyGroupAvailable(any(),any(),any(),any())).thenReturn(partnerList);

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("abc");
		policyGroup.setDesc("desc");
		when(policyGroupRepository.findPolicyGroupById(any())).thenReturn(policyGroup);
		when(partnerHelper.validatePolicyGroup(any())).thenReturn(policyGroup);
		ResponseWrapperV2<List<PartnerDtoV3>> responseWrapper = pserviceImpl.getPartnersV3("approved", true, "Auth_Partner");
		assertNotNull(responseWrapper);
	}

	@Test
	public void getPartnersV3Test1() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setApprovalStatus("approved");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findPartnersByUserIdAndStatusAndPartnerTypeAndPolicyGroupAvailable(any(),any(),any(),any())).thenReturn(partnerList);

		ResponseWrapperV2<List<PartnerDtoV3>> responseWrapper = pserviceImpl.getPartnersV3("approved", null, "FTM_Provider");
		assertNotNull(responseWrapper);
	}

	@Test
	public void getPartnersV3Test2() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		pserviceImpl.getPartnersV3("approved", null, "FTM_Provider");
	}

	@Test
	public void getPartnersV3TestException() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setPolicyGroupId("abc");
		partner.setApprovalStatus("approved");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findPartnersByUserIdAndStatusAndPartnerTypeAndPolicyGroupAvailable(any(),any(),any(),any())).thenReturn(partnerList);

		PolicyGroup policyGroup = new PolicyGroup();
		when(policyGroupRepository.findPolicyGroupById(any())).thenReturn(policyGroup);
		ResponseWrapperV2<List<PartnerDtoV3>> responseWrapper = pserviceImpl.getPartnersV3("approved", true, "Auth_Partner");
		assertNotNull(responseWrapper);
	}

	@Test
	public void getPartnersV3Test_UserNotExist() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		pserviceImpl.getPartnersV3("approved", true, "Auth_Partner");
	}

	@Test
	public void getPartnersV3Test_MISPPartnerException() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "MISP_Partner");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		pserviceImpl.getPartnersV3("approved", true, "MISP_Partner");
	}

	@Test
	public void getPartnersV3Test_MISPPartner() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("MISP_Partner");
		partner.setPolicyGroupId("abc");
		partner.setApprovalStatus("approved");
		partnerList.add(partner);
		when(partnerRepository.findPartnersByStatusAndPartnerTypeAndPolicyGroupAvailable(any(),any(),any())).thenReturn(partnerList);

		when(policyGroupRepository.findPolicyGroupById(any())).thenReturn(null);
		when(partnerHelper.validatePolicyGroup(any())).thenThrow(new PartnerServiceException());
		pserviceImpl.getPartnersV3("approved", true, "MISP_Partner");
	}

	@Test
	public void getPartnersV3Test_PartnerAdmin_MissingPartnerType() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

		ResponseWrapperV2<List<PartnerDtoV3>> responseWrapper = pserviceImpl.getPartnersV3("approved", true, null);
		assertNotNull(responseWrapper);
		assertNotNull(responseWrapper.getErrors());
		assertFalse(responseWrapper.getErrors().isEmpty());
		assertEquals(ErrorCode.PARTNER_TYPE_MANDATORY_FOR_PARTNER_ADMIN.getErrorCode(),
				responseWrapper.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getPartnersV3Test_NonAdmin_PartnerTypeMismatch() throws Exception{
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "AUTH_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setApprovalStatus("approved");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		when(partnerRepository.findPartnersByUserIdAndStatusAndPartnerTypeAndPolicyGroupAvailable(
				anyString(), anyString(), anyString(), any())).thenReturn(Collections.emptyList());

		ResponseWrapperV2<List<PartnerDtoV3>> responseWrapper = pserviceImpl.getPartnersV3("approved", true, "Device_Provider");
		assertNotNull(responseWrapper);
		assertTrue(responseWrapper.getErrors() == null || responseWrapper.getErrors().isEmpty());
		assertNotNull(responseWrapper.getResponse());
		assertTrue(responseWrapper.getResponse().isEmpty());
	}

	@Test
	public void createPartnerTest_WithValidRequest() throws Exception {
		PartnerRequest prequest = new PartnerRequest();
		prequest.setAddress("blr");
		prequest.setContactNumber("8273283283");
		prequest.setEmailId("xyz@gmail.com");
		prequest.setOrganizationName("ABC");
		prequest.setPolicyGroup("policyGroup1");
		prequest.setPartnerId("auth123");
		prequest.setPartnerType("Auth");
		prequest.setLangCode("eng");

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		when(partnerRepository.findByEmailIdHash(anyString())).thenReturn(null);
		when(partnerRepository.findByEmailId(anyString())).thenReturn(null);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("pg1");
		policyGroup.setName("policyGroup1");
		policyGroup.setIsActive(true);
		when(policyGroupRepository.findByName(anyString())).thenReturn(policyGroup);

		Partner partner = new Partner();
		partner.setId("auth123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setName("ABC");
		partner.setApprovalStatus("InProgress");
		partner.setIsActive(false);
		partner.setPolicyGroupId("pg1");
		when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

		PartnerH partnerh = new PartnerH();
		PartnerHPK partnerHPK = new PartnerHPK();
		partnerHPK.setId("qwertyu");
		partnerHPK.setEffDtimes(new Date());
		partnerh.setId(partnerHPK);
		partnerh.setPartnerTypeCode("Auth_Partner");
		partnerh.setName("ABC");
		partnerh.setApprovalStatus("InProgress");
		partnerh.setIsActive(false);
		partnerh.setPolicyGroupId("pg1");
		when(partnerHRepository.save(any())).thenReturn(partnerh);

		pserviceImpl.createPartner(prequest);
	}

	@Test
	public void createPartnerTest_WithAlreadyExistEmail() throws Exception {
		PartnerRequest prequest = new PartnerRequest();
		prequest.setAddress("blr");
		prequest.setContactNumber("8273283283");
		prequest.setEmailId("xyz@gmail.com");
		prequest.setOrganizationName("ABC");
		prequest.setPolicyGroup("policyGroup1");
		prequest.setPartnerId("auth123");
		prequest.setPartnerType("Auth");
		prequest.setLangCode("eng");

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		Partner partner = new Partner();
		partner.setId("auth123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setName("ABC");
		partner.setApprovalStatus("InProgress");
		partner.setIsActive(false);
		partner.setPolicyGroupId("pg1");
		when(partnerRepository.findByEmailIdHash(anyString())).thenReturn(partner);
		pserviceImpl.createPartner(prequest);
	}

	@Test
	public void createPartnerTest_WithInvalidEmail() throws Exception {
		PartnerRequest prequest = new PartnerRequest();
		prequest.setAddress("blr");
		prequest.setContactNumber("8273283283");
		prequest.setEmailId("xyz");
		prequest.setOrganizationName("ABC");
		prequest.setPolicyGroup("policyGroup1");
		prequest.setPartnerId("auth123");
		prequest.setPartnerType("Auth");
		prequest.setLangCode("eng");

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		pserviceImpl.createPartner(prequest);
	}

	@Test
	public void createPartnerTest_WithNullRequest() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		pserviceImpl.createPartner(new PartnerRequest());
	}

	@Test
	public void checkPartnerExistsTest1() {
		PartnerExistsRequestDto partnerExistsRequestDto = new PartnerExistsRequestDto();
		partnerExistsRequestDto.setPartnerId("auth123");
		partnerExistsRequestDto.setEmailId("xyz@gmail.com");
		partnerExistsRequestDto.setPartnerType("Auth");

		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));

		Partner partner = new Partner();
		partner.setId("auth123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setName("ABC");
		partner.setApprovalStatus("InProgress");
		partner.setIsActive(false);
		partner.setPolicyGroupId("pg1");
		when(partnerRepository.findByEmailIdHash(anyString())).thenReturn(partner);
		pserviceImpl.checkPartnerExists(partnerExistsRequestDto);
	}

	@Test
	public void checkPartnerExistsTest2() {
		PartnerExistsRequestDto partnerExistsRequestDto = new PartnerExistsRequestDto();
		partnerExistsRequestDto.setPartnerId("auth123");
		partnerExistsRequestDto.setEmailId("xyz@gmail.com");
		partnerExistsRequestDto.setPartnerType("Auth");

		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));

		Partner partner = new Partner();
		partner.setId("auth123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setName("ABC");
		partner.setApprovalStatus("InProgress");
		partner.setIsActive(false);
		partner.setPolicyGroupId("pg1");
		when(partnerRepository.findByEmailIdHash(anyString())).thenReturn(null);
		when(partnerRepository.findByEmailId(anyString())).thenReturn(null);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		pserviceImpl.checkPartnerExists(partnerExistsRequestDto);
	}

	@Test
	public void checkPartnerExistsTest3() {
		PartnerExistsRequestDto partnerExistsRequestDto = new PartnerExistsRequestDto();
		partnerExistsRequestDto.setPartnerId("auth123");
		partnerExistsRequestDto.setEmailId("xyz@gmail.com");
		partnerExistsRequestDto.setPartnerType("Auth");

		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));

		when(partnerRepository.findByEmailIdHash(anyString())).thenReturn(null);
		when(partnerRepository.findByEmailId(anyString())).thenReturn(null);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		pserviceImpl.checkPartnerExists(partnerExistsRequestDto);
	}

	@Test
	public void checkPartnerExistsTest4() {
		PartnerExistsRequestDto partnerExistsRequestDto = new PartnerExistsRequestDto();
		partnerExistsRequestDto.setPartnerId("auth123");
		partnerExistsRequestDto.setEmailId("xyz@gmail.com");
		partnerExistsRequestDto.setPartnerType("invalid");

		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));
		pserviceImpl.checkPartnerExists(partnerExistsRequestDto);
	}

	@Test
	public void checkPartnerExistsTest5() {
		PartnerExistsRequestDto partnerExistsRequestDto = new PartnerExistsRequestDto();
		pserviceImpl.checkPartnerExists(partnerExistsRequestDto);
	}

	private DataShareResponseDto dataShareResponse(String url) {
		DataShareResponseDto response = new DataShareResponseDto();
		DataShareDto ds = new DataShareDto();
		ds.setUrl(url);
		response.setDataShare(ds);
		response.setErrors(null);
		return response;
	}

	@Test(expected = PartnerServiceException.class)
	public void submitCredentialTypesRequest_invalidRequest_throws() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		pserviceImpl.submitCredentialTypesRequest("p1", "pol1", null);
	}

	@Test(expected = PartnerServiceException.class)
	public void submitCredentialTypesRequest_parentNotFound_throws() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		when(partnerRepository.findById("p1")).thenReturn(Optional.of(createPartner(true)));
		when(partnerPolicyRequestRepository.findByPartnerIdAndReqId(eq("p1"), anyString())).thenReturn(null);

		CredentialTypeRequestDto req = new CredentialTypeRequestDto();
		req.setPartnerPolicyRequestId("req-1");
		req.setCredentialType(allowedCredentialTypes.split(",")[0]);
		pserviceImpl.submitCredentialTypesRequest("p1", "pol1", req);
	}

	@Test(expected = PartnerServiceException.class)
	public void submitCredentialTypesRequest_parentStatusNotInProgress_throws() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		when(partnerRepository.findById("p1")).thenReturn(Optional.of(createPartner(true)));

		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setPolicyId("pol1");
		parent.setIsDeleted(false);
		parent.setStatusCode("Approved");
		when(partnerPolicyRequestRepository.findByPartnerIdAndReqId("p1", "req-1")).thenReturn(parent);

		CredentialTypeRequestDto req = new CredentialTypeRequestDto();
		req.setPartnerPolicyRequestId("req-1");
		req.setCredentialType(allowedCredentialTypes.split(",")[0]);
		pserviceImpl.submitCredentialTypesRequest("p1", "pol1", req);
	}

	@Test(expected = PartnerServiceException.class)
	public void submitCredentialTypesRequest_duplicateRequest_throws() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		when(partnerRepository.findById("p1")).thenReturn(Optional.of(createPartner(true)));

		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setPolicyId("pol1");
		parent.setIsDeleted(false);
		parent.setStatusCode(PartnerConstants.IN_PROGRESS);
		when(partnerPolicyRequestRepository.findByPartnerIdAndReqId("p1", "req-1")).thenReturn(parent);
		when(partnerPolicyCredentialTypeRequestRepository.existsByPartnerPolicyRequestId("mapping-1")).thenReturn(true);

		CredentialTypeRequestDto req = new CredentialTypeRequestDto();
		req.setPartnerPolicyRequestId("req-1");
		req.setCredentialType(allowedCredentialTypes.split(",")[0]);
		pserviceImpl.submitCredentialTypesRequest("p1", "pol1", req);
	}

	@Test(expected = PartnerServiceException.class)
	public void submitCredentialTypesRequest_unableToGenerateUniqueId_throws() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		ReflectionTestUtils.setField(pserviceImpl, "maxRetries", 0);
		when(partnerRepository.findById("p1")).thenReturn(Optional.of(createPartner(true)));

		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setPolicyId("pol1");
		parent.setIsDeleted(false);
		parent.setStatusCode(PartnerConstants.IN_PROGRESS);
		when(partnerPolicyRequestRepository.findByPartnerIdAndReqId("p1", "req-1")).thenReturn(parent);
		when(partnerPolicyCredentialTypeRequestRepository.existsByPartnerPolicyRequestId("mapping-1")).thenReturn(false);
		when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType(anyString(), anyString())).thenReturn(null);
		when(partnerPolicyCredentialTypeRequestRepository.existsById(anyString())).thenReturn(true);

		CredentialTypeRequestDto req = new CredentialTypeRequestDto();
		req.setPartnerPolicyRequestId("req-1");
		req.setCredentialType(allowedCredentialTypes.split(",")[0]);
		pserviceImpl.submitCredentialTypesRequest("p1", "pol1", req);
	}

	@Test(expected = PartnerServiceException.class)
	public void submitCredentialTypesRequest_saveIntegrityViolation_throws() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		ReflectionTestUtils.setField(pserviceImpl, "maxRetries", 1);
		when(partnerRepository.findById("p1")).thenReturn(Optional.of(createPartner(true)));

		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setPolicyId("pol1");
		parent.setIsDeleted(false);
		parent.setStatusCode(PartnerConstants.IN_PROGRESS);
		when(partnerPolicyRequestRepository.findByPartnerIdAndReqId("p1", "req-1")).thenReturn(parent);
		when(partnerPolicyCredentialTypeRequestRepository.existsByPartnerPolicyRequestId("mapping-1")).thenReturn(false);
		when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType(anyString(), anyString())).thenReturn(null);
		when(partnerPolicyCredentialTypeRequestRepository.existsById(anyString())).thenReturn(false);
		doThrow(new DataIntegrityViolationException("dup")).when(partnerPolicyCredentialTypeRequestRepository).saveAndFlush(any());

		CredentialTypeRequestDto req = new CredentialTypeRequestDto();
		req.setPartnerPolicyRequestId("req-1");
		req.setCredentialType(allowedCredentialTypes.split(",")[0]);
		pserviceImpl.submitCredentialTypesRequest("p1", "pol1", req);
	}

	@Test
	public void submitCredentialTypesRequest_success_returnsMessage() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		ReflectionTestUtils.setField(pserviceImpl, "maxRetries", 2);
		when(partnerRepository.findById("p1")).thenReturn(Optional.of(createPartner(true)));

		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setPolicyId("pol1");
		parent.setIsDeleted(false);
		parent.setStatusCode(PartnerConstants.IN_PROGRESS);
		when(partnerPolicyRequestRepository.findByPartnerIdAndReqId("p1", "req-1")).thenReturn(parent);
		when(partnerPolicyCredentialTypeRequestRepository.existsByPartnerPolicyRequestId("mapping-1")).thenReturn(false);
		when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType(anyString(), anyString())).thenReturn(null);
		when(partnerPolicyCredentialTypeRequestRepository.existsById(anyString())).thenReturn(true, false);
		doReturn(new PartnerPolicyCredentialTypeRequest()).when(partnerPolicyCredentialTypeRequestRepository).saveAndFlush(any());

		CredentialTypeRequestDto req = new CredentialTypeRequestDto();
		req.setPartnerPolicyRequestId("req-1");
		req.setCredentialType((" " + allowedCredentialTypes.split(",")[0] + " "));

		String msg = pserviceImpl.submitCredentialTypesRequest("p1", "pol1", req);
		assertEquals("Credential type request submitted successfully.", msg);
	}

	@Test
	public void isPartnerAlreadyMapped_returnsExpected() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType("p1", "ct1")).thenReturn(null);
		boolean absent = ReflectionTestUtils.invokeMethod(target, "isPartnerAlreadyMapped", "p1", "ct1");
		assertFalse(absent);

		PartnerPolicyCredentialType existing = new PartnerPolicyCredentialType();
		when(partnerCredentialTypePolicyRepo.findByPartnerIdAndCrdentialType("p1", "ct2")).thenReturn(existing);
		boolean present = ReflectionTestUtils.invokeMethod(target, "isPartnerAlreadyMapped", "p1", "ct2");
		assertTrue(present);
	}

	@Test(expected = PartnerServiceException.class)
	public void isJSONValid_invalidJson_throws() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		ReflectionTestUtils.invokeMethod(target, "isJSONValid", "{not-json");
	}

	@Test
	public void isJSONValid_validJson_noThrow() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		ReflectionTestUtils.invokeMethod(target, "isJSONValid", "{\"k\":\"v\"}");
	}

	@Test
	public void registerPartner_whenLoggedInUserEmailAlreadyRegistered_throwsLoggedInUserNotAuthorized() throws Exception {
		String loggedInMail = "abc@gmail.com";
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setMail(loggedInMail);
		io.mosip.kernel.openid.bridge.model.AuthUserDetails bridgeAuthUser =
				new io.mosip.kernel.openid.bridge.model.AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(bridgeAuthUser);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerRepository.findByEmailIdHash(PartnerUtil.generateSHA256Hash(loggedInMail))).thenReturn(new Partner());

		PartnerRequestDto dto = buildPartnerRegisterDto("new-register-1@gmail.com", "regPidAuth1", null);
		try {
			pserviceImpl.registerPartner(dto);
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void registerPartner_whenRequestEmailAlreadyExists_throws() throws Exception {
		when(partnerRepository.findByEmailIdHash(PartnerUtil.generateSHA256Hash("abc@gmail.com"))).thenReturn(null);
		when(partnerRepository.findByEmailId("abc@gmail.com")).thenReturn(null);

		String dup = "dup-register@gmail.com";
		when(partnerRepository.findByEmailIdHash(PartnerUtil.generateSHA256Hash(dup))).thenReturn(new Partner());

		PartnerRequestDto dto = buildPartnerRegisterDto(dup, "regPidAuth2", null);
		try {
			pserviceImpl.registerPartner(dto);
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS_EXCEPTION.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void registerPartner_success_withAdditionalInfoJson() throws Exception {
		when(partnerRepository.findByEmailIdHash(PartnerUtil.generateSHA256Hash("abc@gmail.com"))).thenReturn(null);
		when(partnerRepository.findByEmailId("abc@gmail.com")).thenReturn(null);

		String regEmail = "fresh-register-99@gmail.com";
		when(partnerRepository.findByEmailIdHash(PartnerUtil.generateSHA256Hash(regEmail))).thenReturn(null);
		when(partnerRepository.findByEmailId(regEmail)).thenReturn(null);

		String partnerId = "freshPid99";
		when(partnerRepository.findById(partnerId)).thenReturn(Optional.empty());
		when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));
		PolicyGroup policyGroup = createPolicyGroup(Boolean.TRUE);
		when(policyGroupRepository.findByName("Telecom sector")).thenReturn(policyGroup);
		when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");

		PartnerRequestDto dto = buildPartnerRegisterDto(regEmail, partnerId, objectMapper.readTree("{\"x\":1}"));
		PartnerResponse response = pserviceImpl.registerPartner(dto);
		assertNotNull(response);
		assertEquals(partnerId, response.getPartnerId());
		verify(partnerRepository, atLeastOnce()).save(any(Partner.class));
	}

	@Test
	public void validatePartnerByEmail_plainEmailFallback_detectsDuplicate() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		String email = "fallback-dup@gmail.com";
		when(partnerRepository.findByEmailIdHash(PartnerUtil.generateSHA256Hash(email))).thenReturn(null);
		when(partnerRepository.findByEmailId(email)).thenReturn(new Partner());
		boolean ok = ReflectionTestUtils.invokeMethod(target, "validatePartnerByEmail", email);
		assertFalse(ok);
	}

	@Test
	@WithMockUser(roles = { "PARTNER" })
	public void getPartnerDetails_plainFields_whenNotEncrypted_andInactive() {
		when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		Partner p = createPartner(Boolean.FALSE);
		p.setEmailIdHash(null);
		p.setEmailId("plain-email@mail.com");
		p.setContactNo("plain-contact");
		p.setAddress("plain-addr");
		p.setPolicyGroupId(null);
		when(partnerHelper.getValidPartner(eq("12345"), anyBoolean())).thenReturn(p);

		RetrievePartnerDetailsResponse r = pserviceImpl.getPartnerDetails("12345");
		assertEquals("plain-email@mail.com", r.getEmailId());
		assertEquals(PartnerConstants.DEACTIVE, r.getStatus());
	}

	@Test(expected = PartnerServiceException.class)
	@WithMockUser(roles = { "PARTNER" })
	public void getPartnerDetails_whenPolicyGroupMissing_throws() {
		when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		Partner p = createPartner(Boolean.TRUE);
		p.setPolicyGroupId("missing-pg");
		when(partnerHelper.getValidPartner(eq("12345"), anyBoolean())).thenReturn(p);
		when(policyGroupRepository.findById("missing-pg")).thenReturn(Optional.empty());
		pserviceImpl.getPartnerDetails("12345");
	}

	@Test(expected = PartnerServiceException.class)
	public void validateLoggedInUserAuthorization_whenFilterRequiredAndIdMismatch_throws() {
		when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(true);
		pserviceImpl.validateLoggedInUserAuthorization("not-logged-in-partner-id");
	}

	@Test
	public void getSystemSupportedLanguageCodes_and_isInputStringContainsSpaces_viaReflection() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		ReflectionTestUtils.setField(target, "mandatoryLanguges", "eng");
		ReflectionTestUtils.setField(target, "optionalLanguges", "fra,hin");
		List<String> langs = pserviceImpl.getSystemSupportedLanguageCodes();
		assertEquals(3, langs.size());

		assertTrue(ReflectionTestUtils.invokeMethod(target, "isInputStringContainsSpaces", "has space"));
		assertFalse(ReflectionTestUtils.invokeMethod(target, "isInputStringContainsSpaces", "nospaces"));
	}

	@Test
	@WithMockUser(roles = { "PARTNER" })
	public void updatePartnerDetails_withAdditionalInfoAndNullEmailIdHash_encryptsEmail() throws Exception {
		when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		String partnerId = "12345";
		Partner part = createPartner(Boolean.TRUE);
		part.setEmailIdHash(null);
		part.setEmailId("plain-upd@gmail.com");
		when(partnerHelper.getValidPartner(eq(partnerId), anyBoolean())).thenReturn(part);
		when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");

		PartnerUpdateDto dto = new PartnerUpdateDto();
		dto.setAddress("new-address-line");
		dto.setContactNumber("9876543210");
		dto.setLogoUrl("https://logo");
		dto.setAdditionalInfo(objectMapper.readTree("{\"flag\":true}"));

		PartnerResponse res = pserviceImpl.updatePartnerDetails(dto, partnerId);
		assertNotNull(res);
		assertEquals(partnerId, res.getPartnerId());
		verify(keyManagerHelper, atLeastOnce()).encryptData(any());
	}

	@Test
	public void validateAndGetPolicyGroupById_whenMissing_throwsPolicyGroupDoesNotExist() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		when(policyGroupRepository.findById("missing-pg")).thenReturn(Optional.empty());
		try {
			ReflectionTestUtils.invokeMethod(target, "validateAndGetPolicyGroupById", "missing-pg");
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void validatePolicyGroupAndPolicy_happyPath_returnsPolicy() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		String pgId = "pg1";
		String polName = "pol1";

		PolicyGroup pg = new PolicyGroup();
		pg.setId(pgId);
		pg.setIsActive(true);

		AuthPolicy ap = new AuthPolicy();
		ap.setIsActive(true);
		ap.setValidToDate(LocalDateTime.now().plusDays(10));
		ap.setPolicyGroup(pg);

		when(authPolicyRepository.findByPolicyNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(polName))
				.thenReturn(List.of(ap));
		when(authPolicyRepository.findByPolicyGroupIdAndName(pgId, polName)).thenReturn(ap);

		AuthPolicy out = ReflectionTestUtils.invokeMethod(target, "validatePolicyGroupAndPolicy", pgId, polName);
		assertNotNull(out);
		assertEquals(ap, out);
	}

	@Test
	public void validatePolicyGroupAndPolicy_whenNoPolicies_throwsPolicyNotExist() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		when(authPolicyRepository.findByPolicyNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue("missing"))
				.thenReturn(Collections.emptyList());
		try {
			ReflectionTestUtils.invokeMethod(target, "validatePolicyGroupAndPolicy", "pg1", "missing");
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.POLICY_NOT_EXIST.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void validatePolicyGroupAndPolicy_whenPolicyGroupMappingMissing_throws() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		PolicyGroup otherPg = new PolicyGroup();
		otherPg.setId("other");
		otherPg.setIsActive(true);
		AuthPolicy ap = new AuthPolicy();
		ap.setPolicyGroup(otherPg);
		ap.setIsActive(true);
		ap.setValidToDate(LocalDateTime.now().plusDays(5));
		when(authPolicyRepository.findByPolicyNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue("p"))
				.thenReturn(List.of(ap));
		when(authPolicyRepository.findByPolicyGroupIdAndName("pg1", "p")).thenReturn(null);
		try {
			ReflectionTestUtils.invokeMethod(target, "validatePolicyGroupAndPolicy", "pg1", "p");
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.POLICY_GROUP_POLICY_NOT_EXISTS.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void validatePolicyGroupAndPolicy_whenPolicyInactive_throws() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		PolicyGroup pg = new PolicyGroup();
		pg.setId("pg1");
		pg.setIsActive(true);
		AuthPolicy ap = new AuthPolicy();
		ap.setPolicyGroup(pg);
		ap.setIsActive(false);
		ap.setValidToDate(LocalDateTime.now().plusDays(5));
		when(authPolicyRepository.findByPolicyNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue("p"))
				.thenReturn(List.of(ap));
		when(authPolicyRepository.findByPolicyGroupIdAndName("pg1", "p")).thenReturn(ap);
		try {
			ReflectionTestUtils.invokeMethod(target, "validatePolicyGroupAndPolicy", "pg1", "p");
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void validatePolicyGroupAndPolicy_whenPolicyExpired_throws() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		PolicyGroup pg = new PolicyGroup();
		pg.setId("pg1");
		pg.setIsActive(true);
		AuthPolicy ap = new AuthPolicy();
		ap.setPolicyGroup(pg);
		ap.setIsActive(true);
		ap.setValidToDate(LocalDateTime.now().minusDays(1));
		when(authPolicyRepository.findByPolicyNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue("p"))
				.thenReturn(List.of(ap));
		when(authPolicyRepository.findByPolicyGroupIdAndName("pg1", "p")).thenReturn(ap);
		try {
			ReflectionTestUtils.invokeMethod(target, "validatePolicyGroupAndPolicy", "pg1", "p");
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.POLICY_EXPIRED_EXCEPTION.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void validatePolicyGroupAndPolicy_whenPolicyGroupInactive_throws() {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		PolicyGroup pg = new PolicyGroup();
		pg.setId("pg1");
		pg.setIsActive(false);
		AuthPolicy ap = new AuthPolicy();
		ap.setPolicyGroup(pg);
		ap.setIsActive(true);
		ap.setValidToDate(LocalDateTime.now().plusDays(5));
		when(authPolicyRepository.findByPolicyNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue("p"))
				.thenReturn(List.of(ap));
		when(authPolicyRepository.findByPolicyGroupIdAndName("pg1", "p")).thenReturn(ap);
		try {
			ReflectionTestUtils.invokeMethod(target, "validatePolicyGroupAndPolicy", "pg1", "p");
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(), e.getErrorCode());
		}
	}

	@Test
	public void createAndUpdateContactDetails_whenMaxRetriesForIdGeneration_throws() throws Exception {
		PartnerServiceImpl target = AopTestUtils.getTargetObject(pserviceImpl);
		ReflectionTestUtils.setField(target, "maxRetries", 0);

		AddContactRequestDto req = new AddContactRequestDto();
		req.setAddress("Addr");
		req.setContactNumber("1234567890");
		req.setEmailId("newcontact@gmail.com");
		req.setIs_Active(true);

		Partner active = createPartner(Boolean.TRUE);
		when(partnerRepository.findById(active.getId())).thenReturn(Optional.of(active));
		when(partnerContactRepository.findByPartnerAndEmailIdHash(anyString(), anyString())).thenReturn(null);
		when(partnerContactRepository.findByPartnerAndEmailId(anyString(), anyString())).thenReturn(null);
		when(keyManagerHelper.encryptData(any())).thenReturn("enc");
		when(partnerContactRepository.existsById(anyString())).thenReturn(true);

		try {
			pserviceImpl.createAndUpdateContactDetails(req, active.getId());
			fail("expected PartnerServiceException");
		} catch (PartnerServiceException e) {
			assertEquals(ErrorCode.UNABLE_TO_GENERATE_UNIQUE_ID.getErrorCode(), e.getErrorCode());
		}
	}
}
