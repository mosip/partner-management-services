package io.mosip.pms.test.misp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.mosip.pms.common.dto.*;
import io.mosip.pms.common.util.PageUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.MispLicenseRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.impl.InfraProviderServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InfraProviderServiceImplTest {

	@Autowired
	InfraProviderServiceImpl infraProviderServiceImpl;

	@Mock
	private WebSubPublisher webSubPublisher;

	@Mock
	PartnerServiceRepository partnerRepository;

	@Mock
	PageUtils pageUtils;
	
	@Mock
	private PartnerPolicyRequestRepository partnerPolicyRequestRepository; 
	
	@Mock
	private AuthPolicyRepository  authPolicyRepository; 
	

	@Mock
	MispLicenseRepository mispLicenseRepository;
	
	@Mock
	SearchHelper searchHelper;
	
	@Mock
	FilterColumnValidator filterColumnValidator;
	
	@Mock
	FilterHelper filterHelper;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(infraProviderServiceImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "mispLicenseRepository", mispLicenseRepository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "webSubPublisher", webSubPublisher);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "searchHelper", searchHelper);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "filterColumnValidator", filterColumnValidator);
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.when(searchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		Mockito.when(filterColumnValidator.validate(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filtersData);
	}

	@Test
	public void approveInfraProvider() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		partner.setPartnerTypeCode("MISP_Partner");
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId("1234");
		partnerPolicyRequest.setStatusCode("approved");
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("1234");
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<>();
		partnerPolicyRequests.add(partnerPolicyRequest);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);		
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(partnerPolicyRequests);
		Mockito.when(authPolicyRepository.findById("1234")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}
	
	private PolicyGroup policyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policyGroup.setIsActive(true);
		return policyGroup;
	}
	
	private List<AuthPolicy> getAuthPolicies(){
		AuthPolicy policy = new AuthPolicy();
		List<AuthPolicy> policies = new ArrayList<AuthPolicy>();
		policy.setPolicyGroup(policyGroupData());
		policy.setId("234");
		policy.setName("Test");
		policy.setDescr("Policy Desc");
		policy.setIsActive(true);
		policy.setValidToDate(LocalDateTime.now().plusDays(5));
		policy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		policies.add(policy);
		return policies;		
	}

	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_01() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_02() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(false);
		partner.setPartnerTypeCode("MISP_Partner");
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Ignore
	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_03() {
		String misp_Id = "1234";
		String licenseKey = "12345";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		partner.setPartnerTypeCode("MISP_Partner");
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		List<MISPLicenseEntity> mispEntityList = new ArrayList<>();
		mispEntityList.add(mispLicenseEntity);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispEntityList);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test
	public void updateInfraProviderTest() {
		String id = "123";
		String misp_id = "1234";
		String licenseKey = "12345";
		String status = "active";
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_id);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		Mockito.when(mispLicenseRepository.findByIdAndKey(id, licenseKey)).thenReturn(mispLicenseEntity);
		infraProviderServiceImpl.updateInfraProvider(id, licenseKey, status);
	}

	@Test(expected = MISPServiceException.class)
	public void updateInfraProviderTest_01() {
		String id = "123";
		String misp_id = "1234";
		String licenseKey = "12345";
		String status = "abcdef";
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_id);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		Mockito.when(mispLicenseRepository.findByIdAndKey(id, licenseKey)).thenReturn(mispLicenseEntity);
		infraProviderServiceImpl.updateInfraProvider(id, licenseKey, status);
	}

	@Test(expected = MISPServiceException.class)
	public void updateInfraProviderTest_02() {
		String id = "123";
		String misp_id = "1234";
		String licenseKey = "12345";
		String status = "active";
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_id);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		infraProviderServiceImpl.updateInfraProvider(id, licenseKey, status);
	}

	@Test
	public void regenerateKeyTest() {
		String misp_Id = "1234";
		String licenseKey = "12345";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		List<MISPLicenseEntity> mispLicense = new ArrayList<MISPLicenseEntity>();
		mispLicense.add(mispLicenseEntity);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		Mockito.when(mispLicenseRepository.findByMispIdandExpirydate(misp_Id)).thenReturn(mispLicense);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispLicense);
		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test
	public void regenerateKeyTest_001() {
		String misp_Id = "1234";
		String licenseKey = "12345";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(-1));
		List<MISPLicenseEntity> mispLicense = new ArrayList<MISPLicenseEntity>();
		mispLicense.add(mispLicenseEntity);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		Mockito.when(mispLicenseRepository.findByMispIdandExpirydate(misp_Id)).thenReturn(mispLicense);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispLicense);
		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void regenerateKeyTest_01() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void regenerateKeyTest_02() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void regenerateKeyTest_03() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(false);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test
	public void getAllInfraProviders() {
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		List<MISPLicenseEntity> licenses = new ArrayList<>();
		licenses.add(mispLicenseEntity);
		Mockito.when(mispLicenseRepository.findAll()).thenReturn(licenses);
		infraProviderServiceImpl.getInfraProvider();
	}

	@Test
	public void filterValuesTest() {		
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("licenseKey");
    	filterDto.setText("");
    	filterDto.setType("all");
		SearchFilter searchDto = new SearchFilter();
		FilterValueDto filterValueDto = new FilterValueDto();
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		filterValueDto.setFilters(filterDtos);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filtersData);
		infraProviderServiceImpl.filterValues(filterValueDto);
	}
	
	@Test
	public void filterValuesTest01() {		
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("licenseKey");
    	filterDto.setText("");
    	filterDto.setType("all");
		SearchFilter searchDto = new SearchFilter();
		FilterValueDto filterValueDto = new FilterValueDto();
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		filterValueDto.setFilters(filterDtos);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filtersData);
		infraProviderServiceImpl.filterValues(filterValueDto);
	}

	@Test
	public void testSearchWithSearchDto_thenSuccess() {
		SearchDto dto = new SearchDto();
		dto.setFilters(new ArrayList<>());
		dto.setPagination(new Pagination(0,10));
		dto.setSort(new ArrayList<>());

		List<MISPLicenseEntity> entities = new ArrayList<>();
		MISPLicenseEntity entity1 = new MISPLicenseEntity();
		entity1.setIsActive(true);
		entity1.setValidToDate(LocalDateTime.now().plusYears(1));
		entity1.setMispId("misp");

		MISPLicenseEntity entity2 = new MISPLicenseEntity();
		entity2.setIsActive(true);
		entity2.setValidToDate(LocalDateTime.now().plusYears(1));
		entity2.setMispId("mispId2");

		entities.add(entity1);
		entities.add(entity2);

		Page<MISPLicenseEntity> page = new PageImpl<>(entities);

		when(searchHelper.search(MISPLicenseEntity.class, dto, "mispId")).thenReturn(page);

		PageResponseDto<MISPLicenseEntity> result = infraProviderServiceImpl.search(dto);

		assertEquals(entities.size(), result.getData().size());
	}

	// ==================== Additional coverage tests (sonar coverage uplift) ====================

	@Test
	public void testRegenerateKey_NewLicense_NoPolicy_Success() {
		String mispId = "MISP1";
		Partner partner = new Partner();
		partner.setId(mispId);
		partner.setIsActive(true);
		when(partnerRepository.findById(mispId)).thenReturn(Optional.of(partner));
		when(mispLicenseRepository.findByMispIdandExpirydate(mispId)).thenReturn(new ArrayList<>());
		when(mispLicenseRepository.findByMispId(mispId)).thenReturn(Collections.singletonList(new MISPLicenseEntity()));
		when(partnerPolicyRequestRepository.findByPartnerId(mispId)).thenReturn(new ArrayList<>());

		MISPLicenseResponseDto response = infraProviderServiceImpl.regenerateKey(mispId);
		assertNotNull(response);
		assertNotNull(response.getLicenseKey());
		assertEquals(mispId, response.getProviderId());
		assertEquals("Active", response.getLicenseKeyStatus());
	}

	@Test
	public void testRegenerateKey_NewLicense_WithApprovedPolicy_Success() {
		String mispId = "MISP2";
		Partner partner = new Partner();
		partner.setId(mispId);
		partner.setIsActive(true);
		when(partnerRepository.findById(mispId)).thenReturn(Optional.of(partner));
		when(mispLicenseRepository.findByMispIdandExpirydate(mispId)).thenReturn(new ArrayList<>());
		when(mispLicenseRepository.findByMispId(mispId)).thenReturn(Collections.singletonList(new MISPLicenseEntity()));

		PartnerPolicyRequest policyReq = new PartnerPolicyRequest();
		policyReq.setPolicyId("POLICY1");
		policyReq.setStatusCode("approved");
		when(partnerPolicyRequestRepository.findByPartnerId(mispId)).thenReturn(Collections.singletonList(policyReq));

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("POLICY1");
		authPolicy.setName("Banking");
		authPolicy.setIsActive(true);
		authPolicy.setValidFromDate(LocalDateTime.now().minusDays(1));
		authPolicy.setValidToDate(LocalDateTime.now().plusYears(1));
		authPolicy.setPolicyFileId("{}");
		when(authPolicyRepository.findById("POLICY1")).thenReturn(Optional.of(authPolicy));

		MISPLicenseResponseDto response = infraProviderServiceImpl.regenerateKey(mispId);
		assertNotNull(response);
		assertNotNull(response.getLicenseKey());
		assertEquals(mispId, response.getProviderId());
	}

	@Test(expected = MISPServiceException.class)
	public void testRegenerateKey_PolicyNotApproved_ThrowsException() {
		String mispId = "MISP3";
		Partner partner = new Partner();
		partner.setId(mispId);
		partner.setIsActive(true);
		when(partnerRepository.findById(mispId)).thenReturn(Optional.of(partner));
		when(mispLicenseRepository.findByMispIdandExpirydate(mispId)).thenReturn(new ArrayList<>());
		when(mispLicenseRepository.findByMispId(mispId)).thenReturn(Collections.singletonList(new MISPLicenseEntity()));

		PartnerPolicyRequest policyReq = new PartnerPolicyRequest();
		policyReq.setPolicyId("POLICY1");
		policyReq.setStatusCode("pending");
		when(partnerPolicyRequestRepository.findByPartnerId(mispId)).thenReturn(Collections.singletonList(policyReq));

		infraProviderServiceImpl.regenerateKey(mispId);
	}

	@Test
	public void testRegenerateKey_ExistingValidLicense_Success() {
		String mispId = "MISP4";
		Partner partner = new Partner();
		partner.setId(mispId);
		partner.setIsActive(true);
		when(partnerRepository.findById(mispId)).thenReturn(Optional.of(partner));

		MISPLicenseEntity existingLicense = new MISPLicenseEntity();
		existingLicense.setLicenseKey("EXISTING_KEY");
		existingLicense.setValidToDate(LocalDateTime.now().plusYears(1));
		when(mispLicenseRepository.findByMispIdandExpirydate(mispId)).thenReturn(Collections.singletonList(existingLicense));
		when(mispLicenseRepository.findByMispId(mispId)).thenReturn(Collections.singletonList(existingLicense));
		when(partnerPolicyRequestRepository.findByPartnerId(mispId)).thenReturn(new ArrayList<>());

		MISPLicenseResponseDto response = infraProviderServiceImpl.regenerateKey(mispId);
		assertNotNull(response);
		assertEquals("EXISTING_KEY", response.getLicenseKey());
		assertEquals(mispId, response.getProviderId());
	}

	@Test(expected = MISPServiceException.class)
	public void testRegenerateKey_MispInactive_ThrowsException() {
		String mispId = "MISP5";
		Partner partner = new Partner();
		partner.setId(mispId);
		partner.setIsActive(false);
		when(partnerRepository.findById(mispId)).thenReturn(Optional.of(partner));
		infraProviderServiceImpl.regenerateKey(mispId);
	}

	@Test(expected = MISPServiceException.class)
	public void testRegenerateKey_NoLicenseAssociated_ThrowsException() {
		String mispId = "MISP6";
		Partner partner = new Partner();
		partner.setId(mispId);
		partner.setIsActive(true);
		when(partnerRepository.findById(mispId)).thenReturn(Optional.of(partner));
		when(mispLicenseRepository.findByMispIdandExpirydate(mispId)).thenReturn(new ArrayList<>());
		when(mispLicenseRepository.findByMispId(mispId)).thenReturn(new ArrayList<>());
		infraProviderServiceImpl.regenerateKey(mispId);
	}

	@Test
	public void testUpdateInfraProvider_FullSuccess() {
		MISPLicenseEntity entity = new MISPLicenseEntity();
		entity.setMispId("MISP7");
		entity.setLicenseKey("KEY7");
		entity.setValidToDate(LocalDateTime.now().plusYears(1));
		when(mispLicenseRepository.findByIdAndKey("MISP7", "KEY7")).thenReturn(entity);
		when(mispLicenseRepository.save(any())).thenReturn(entity);

		MISPLicenseResponseDto response = infraProviderServiceImpl.updateInfraProvider("MISP7", "KEY7", "active");
		assertNotNull(response);
		assertEquals("KEY7", response.getLicenseKey());
		assertEquals("active", response.getLicenseKeyStatus());
	}

	@Test(expected = MISPServiceException.class)
	public void testUpdateInfraProvider_InvalidStatus_ThrowsException() {
		infraProviderServiceImpl.updateInfraProvider("MISP8", "KEY8", "bogus-status");
	}

	@Test(expected = MISPServiceException.class)
	public void testUpdateInfraProvider_LicenseNotFound_ThrowsException() {
		when(mispLicenseRepository.findByIdAndKey(anyString(), anyString())).thenReturn(null);
		infraProviderServiceImpl.updateInfraProvider("MISP9", "KEY9", "active");
	}
}
