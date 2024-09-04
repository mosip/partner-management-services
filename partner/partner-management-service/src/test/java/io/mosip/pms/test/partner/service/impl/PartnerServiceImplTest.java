package io.mosip.pms.test.partner.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.*;
import io.mosip.pms.partner.dto.DataShareDto;
import io.mosip.pms.partner.dto.DataShareResponseDto;
import io.mosip.pms.partner.dto.UploadCertificateRequestDto;
import io.mosip.pms.partner.request.dto.*;
import io.mosip.pms.partner.response.dto.*;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import io.mosip.pms.common.helper.FilterHelper;
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
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.service.impl.PartnerServiceImpl;
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
	UserDetailUtil userDetailUtil;
	@MockBean
	AuditUtil auditUtil;

	@Mock
	FilterHelper filterHelper;

	@Mock
	SecurityContext securityContext;

	@Mock
	Authentication authentication;

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
		ReflectionTestUtils.setField(pserviceImpl, "filterHelper", filterHelper);
		ReflectionTestUtils.setField(pserviceImpl, "restUtil", restUtil);
		ReflectionTestUtils.setField(pserviceImpl, "allowedCredentialTypes", allowedCredentialTypes);

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
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}

	@Test(expected = PartnerServiceException.class)
	public void getPartnerCertificate_Test() throws Exception{
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
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
		partnerCertDownloadRequestDto.setPartnerId("id");
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
		try {
			pserviceImpl.getPartnerCertificate(partnerCertDownloadRequestDto);
		}catch (PartnerServiceException e) {
			assertFalse(e.getErrorCode().equals(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode()));
		}
		Optional<Partner> getPartner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> policyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(getPartner);
		Mockito.when(policyGroupRepository.findById(getPartner.get().getPolicyGroupId())).thenReturn(policyGroup);

		PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = pserviceImpl.getPartnerCertificate(partnerCertDownloadRequestDto);
		assertNotNull(partnerCertDownloadResponeDto);
		assertEquals(partnerCertDownloadResponeDto.getCertificateData(), "12345");
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(),Mockito.any(),Mockito.any());
	}

	@Test(expected = PartnerServiceException.class)
	public void getOriginalPartnerCertificate_Test() throws Exception{
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
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
		partnerCertDownloadRequestDto.setPartnerId("id");
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
		try {
			pserviceImpl.getOriginalPartnerCertificate(partnerCertDownloadRequestDto);
		}catch (PartnerServiceException e) {
			assertFalse(e.getErrorCode().equals(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode()));
		}
		Optional<Partner> getPartner = Optional.of(createPartner(Boolean.TRUE));
		Optional<PolicyGroup> policyGroup = Optional.of(createPolicyGroup(Boolean.TRUE));
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(getPartner);
		Mockito.when(policyGroupRepository.findById(getPartner.get().getPolicyGroupId())).thenReturn(policyGroup);

		OriginalCertDownloadResponseDto originalCertDownloadResponseDto = pserviceImpl.getOriginalPartnerCertificate(partnerCertDownloadRequestDto);
		assertNotNull(originalCertDownloadResponseDto);
		assertEquals(originalCertDownloadResponseDto.getCaSignedCertificateData(), "12345");
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
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
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
	@WithUserDetails("partner")
	public void savePartnerTest() {
		MosipUserDto userDto = new MosipUserDto();
		userDto.setName("PARTNER");
		userDto.setMobile("partner@gmail.com");
		PolicyGroup policyGroup = createPolicyGroup(Boolean.TRUE);
		PartnerRequest partnerRequest = createPartnerRequest();
		Partner partner = new Partner();
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
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
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
		Optional<Partner> partner = Optional.empty();
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(partner);
		pserviceImpl.getPartnerDetails("12345");
	}

	@Test
	@WithMockUser(roles = {"PARTNER"})
	public void updatePartnerDetailsTest_S1() {
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(createPartner(true));

		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
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
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
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
	@WithMockUser(roles = {"PARTNER"})
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
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
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

	@Test (expected = PartnerServiceException.class)
	public void requestForPolicyMappingTest() {
		PartnerPolicyMappingRequest request = new PartnerPolicyMappingRequest();
		request.setPolicyName("policyName");
		request.setUseCaseDescription("Use cases Details");
		Optional<Partner> partner = Optional.of(createPartner(true));
		partner.get().setPartnerTypeCode("Auth_Partner");
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","policyName")).thenReturn(createAuthPolicy());
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId("12345","123")).thenReturn(List.of(createPartnerPolicyRequest("approved")));
		pserviceImpl.requestForPolicyMapping(request, "12345");
		partner.get().setPolicyGroupId(null);
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_GROUP.getErrorCode()));
		}
		partner.get().setPolicyGroupId("12345");
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId("12345","12345")).thenReturn(List.of(createPartnerPolicyRequest("approved")));
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_MAPPING_APPROVED.getErrorCode()));
		}
		partner.get().setPolicyGroupId("12345");
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId("12345","12345")).thenReturn(List.of(createPartnerPolicyRequest(PartnerConstants.IN_PROGRESS)));
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_MAPPING_INPROGRESS.getErrorCode()));
		}

		Mockito.when(partnerRepository.findById("12345")).thenReturn(Optional.empty());
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode()));
		}
		partner.get().setIsActive(false);
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		try {
			pserviceImpl.requestForPolicyMapping(request, "12345");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode()));
		}
		partner.get().setIsActive(true);
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
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
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		Mockito.when(partnerTypeRepository.findAll()).thenReturn(List.of(getPartnerType()));
		Mockito.when(policyGroupRepository.findByName("policygroupname")).thenReturn(createPolicyGroup(true));
		assertTrue(pserviceImpl.updatePolicyGroup("12345","policygroupname").equals("Success"));
		partner.get().setIsActive(true);
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		try {
			pserviceImpl.updatePolicyGroup("12345","policygroupname");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_MAP_ACTIVE_PARTNER.getErrorCode()));
		}
		partner.get().setIsActive(true);
		partner.get().setPartnerTypeCode("Auth_Partner");
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
		try {
			pserviceImpl.updatePolicyGroup("12345","policygroupname");
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_MAP_ACTIVE_PARTNER.getErrorCode()));
		}
		partner.get().setIsActive(false);
		partner.get().setPartnerTypeCode("Auth");
		Mockito.when(partnerRepository.findById("12345")).thenReturn(partner);
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

	@Test (expected = PartnerServiceException.class)
	public void testUploadPartnerCertificate_Success() throws IOException {
		PartnerCertificateResponseDto expectedResponse = new PartnerCertificateResponseDto();
		expectedResponse.setCertificateId("123");
		expectedResponse.setSignedCertificateData("cert_data");
		expectedResponse.setTimestamp(LocalDateTime.now());
		Map<String, Object> apiResponse = Collections.singletonMap("response", expectedResponse);
		when(restUtil.postApi(eq("https://localhost/v1/keymanager/uploadPartnerCertificate"), any(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class))).thenReturn(apiResponse);

		PartnerCertificateUploadRequestDto requestDto = new PartnerCertificateUploadRequestDto();
		requestDto.setPartnerId("Partner");
		requestDto.setPartnerDomain("Auth");
		requestDto.setCertificateData("cert_data");
		PartnerCertificateResponseDto actualResponse = pserviceImpl.uploadPartnerCertificate(requestDto);

		assertEquals(expectedResponse, actualResponse);
		verify(restUtil).postApi(eq("https://localhost/v1/keymanager/uploadPartnerCertificate"), null, eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class));
	}

	@Test (expected = Exception.class)
	public void testUploadCACertificate_Success() throws IOException {

		CACertificateResponseDto expectedResponse = new CACertificateResponseDto();
		expectedResponse.setStatus("Active");
		expectedResponse.setTimestamp(LocalDateTime.now());
		Map<String, Object> apiResponse = Collections.singletonMap("response", expectedResponse);
		when(restUtil.postApi(eq("https://localhost/v1/keymanager/uploadCACertificate"), any(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class))).thenReturn(apiResponse);

		CACertificateRequestDto requestDto = new CACertificateRequestDto();
		requestDto.setCertificateData("cert_data");
		requestDto.setPartnerDomain("Auth");
		CACertificateResponseDto actualResponse = pserviceImpl.uploadCACertificate(requestDto);

		assertEquals(expectedResponse, actualResponse);
		verify(restUtil).postApi(eq("https://localhost/v1/keymanager/uploadCACertificate"), null, eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class));
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

	@Test (expected = PartnerServiceException.class)
	public void testGetPartnerCertFromChain_Success() throws Exception {
		String certChain = "mock_certificate_chain";

		Certificate certificateMock = Mockito.mock(Certificate.class);

		Base64.Encoder base64EncoderMock = Mockito.mock(Base64.Encoder.class);
		when(base64EncoderMock.encode(any(byte[].class))).thenReturn("mock_encoded_certificate_data".getBytes());

		byte[] encodedCertificateData = "mock_encoded_certificate_data".getBytes();
		when(certificateMock.getEncoded()).thenReturn(encodedCertificateData);

		String result = ReflectionTestUtils.invokeMethod(pserviceImpl,"getPartnerCertFromChain",certChain);

		assertEquals("Expected result", "mock_certificate_data", result);
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
