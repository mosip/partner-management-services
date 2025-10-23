package io.mosip.pms.test.misp.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.mosip.pms.common.dto.*;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.partner.misp.dto.MISPDeactivateRequestDto;
import io.mosip.pms.partner.misp.dto.MISPFilterDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDtoV2;
import io.mosip.pms.partner.misp.dto.MISPRegenerateRequestDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.impl.InfraProviderServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
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
	MispLicenseV2Repository mispLicenseV2Repository;

	@Mock
	PolicyGroupRepository policyGroupRepository;

	@Mock
	MISPLicenseSummaryRepository mispLicenseSummaryRepository;
	
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
		ReflectionTestUtils.setField(infraProviderServiceImpl, "mispLicenseV2Repository", mispLicenseV2Repository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "webSubPublisher", webSubPublisher);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "searchHelper", searchHelper);
		ReflectionTestUtils.setField(infraProviderServiceImpl, "filterColumnValidator", filterColumnValidator);
		Mockito.doNothing().when(webSubPublisher).notify(any(), any(), any());
		Mockito.when(searchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		Mockito.when(filterColumnValidator.validate(any(), any(), any())).thenReturn(true);
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		Mockito.when(filterHelper.filterValuesWithCode(any(), any(), any(), any())).thenReturn(filtersData);
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

	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_04() throws Exception {
		String misp_Id = "1234";
		String licenseKey = "12345";

		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		List<MISPLicenseEntity> mispLicenseFromDb = new ArrayList<>();
		mispLicenseFromDb.add(mispLicenseEntity);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispLicenseFromDb);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_05() throws Exception {
		String misp_Id = "1234";

		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		partner.setPartnerTypeCode("Auth_Partner");
		Optional<Partner> opt_partner = Optional.of(partner);

		List<MISPLicenseEntity> mispEntityList = new ArrayList<>();
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispEntityList);

		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_06() {
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
		Mockito.when(authPolicyRepository.findById("1234")).thenReturn(Optional.empty());
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_07() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		partner.setPartnerTypeCode("MISP_Partner");
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId("1234");
		partnerPolicyRequest.setStatusCode("InProgress");
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("1234");
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<>();
		partnerPolicyRequests.add(partnerPolicyRequest);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(partnerPolicyRequests);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test
	public void approveInfraProvider_08() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		partner.setPartnerTypeCode("MISP_Partner");

		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<>();
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(partnerPolicyRequests);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void approveInfraProvider_09() {
		String misp_Id = "1234";
		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		partner.setPartnerTypeCode("MISP_Partner");
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId("1234");
		partnerPolicyRequest.setStatusCode("approved");

		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<>();
		partnerPolicyRequests.add(partnerPolicyRequest);
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(partnerPolicyRequests);

		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true]}");
		Mockito.when(authPolicyRepository.findById("1234")).thenReturn(Optional.of(authPolicy));
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
	}

	@Test
	public void updateInfraProviderTest() {
		String id = "123";
		String misp_id = "1234";
		String licenseKey = "12345";
		String status = "active";
		MISPLicenseEntityV2 mispLicenseEntity = new MISPLicenseEntityV2();
		MISPLicenseEntityPK mispLicenseEntityPK = new MISPLicenseEntityPK();
		mispLicenseEntityPK.setLicenseKey(licenseKey);
		mispLicenseEntityPK.setMispId(misp_id);
		mispLicenseEntity.setId(mispLicenseEntityPK);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		Mockito.when(mispLicenseV2Repository.findByPartnerIdAndLicenseKey(id, licenseKey)).thenReturn(mispLicenseEntity);
		infraProviderServiceImpl.updateInfraProvider(id, licenseKey, status);
	}

	@Test(expected = MISPServiceException.class)
	public void updateInfraProviderTest_01() {
		String id = "123";
		String misp_id = "1234";
		String licenseKey = "12345";
		String status = "abcdef";
		MISPLicenseEntityV2 mispLicenseEntity = new MISPLicenseEntityV2();
		MISPLicenseEntityPK mispLicenseEntityPK = new MISPLicenseEntityPK();
		mispLicenseEntityPK.setLicenseKey(licenseKey);
		mispLicenseEntityPK.setMispId(misp_id);
		mispLicenseEntity.setId(mispLicenseEntityPK);
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(1));
		Mockito.when(mispLicenseV2Repository.findByPartnerIdAndLicenseKey(id, licenseKey)).thenReturn(mispLicenseEntity);
		infraProviderServiceImpl.updateInfraProvider(id, licenseKey, status);
	}

	@Test(expected = MISPServiceException.class)
	public void updateInfraProviderTest_02() {
		String id = "123";
		String misp_id = "1234";
		String licenseKey = "12345";
		String status = "active";
		MISPLicenseEntityV2 mispLicenseEntity = new MISPLicenseEntityV2();
		MISPLicenseEntityPK mispLicenseEntityPK = new MISPLicenseEntityPK();
		mispLicenseEntityPK.setLicenseKey(licenseKey);
		mispLicenseEntityPK.setMispId(misp_id);
		mispLicenseEntity.setId(mispLicenseEntityPK);
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
	public void regenerateKeyTest_04() {
		String misp_Id = "1234";
		String licenseKey = "12345";

		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);

		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setPolicyId("45678");
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().minusYears(1));
		List<MISPLicenseEntity> mispValidLicense = new ArrayList<MISPLicenseEntity>();
		Mockito.when(mispLicenseRepository.findByMispIdandExpirydate(misp_Id)).thenReturn(mispValidLicense);

		List<MISPLicenseEntity> mispLicense = new ArrayList<MISPLicenseEntity>();
		mispLicense.add(mispLicenseEntity);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispLicense);

		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId("45678");
		partnerPolicyRequest.setStatusCode("approved");
		List<PartnerPolicyRequest> approvedPolicies = new ArrayList<>();
		approvedPolicies.add(partnerPolicyRequest);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(approvedPolicies);

		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(3));
		Mockito.when(mispLicenseRepository.save(mispLicenseEntity)).thenReturn(mispLicenseEntity);

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("45678");
		authPolicy.setName("45678");
		authPolicy.setDescr("description");
		authPolicy.setIsActive(true);
		authPolicy.setPolicyFileId("{\"key\":\"value\"}");
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Mockito.when(authPolicyRepository.findById("45678")).thenReturn(opt_authPolicy);

		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void regenerateKeyTest_05() {
		String misp_Id = "1234";
		String licenseKey = "12345";

		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);

		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setPolicyId("45678");
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().minusYears(1));
		List<MISPLicenseEntity> mispValidLicense = new ArrayList<MISPLicenseEntity>();
		Mockito.when(mispLicenseRepository.findByMispIdandExpirydate(misp_Id)).thenReturn(mispValidLicense);

		List<MISPLicenseEntity> mispLicense = new ArrayList<MISPLicenseEntity>();
		mispLicense.add(mispLicenseEntity);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispLicense);

		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId("45678");
		partnerPolicyRequest.setStatusCode("approved");
		List<PartnerPolicyRequest> approvedPolicies = new ArrayList<>();
		approvedPolicies.add(partnerPolicyRequest);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(approvedPolicies);

		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(3));
		Mockito.when(mispLicenseRepository.save(mispLicenseEntity)).thenReturn(mispLicenseEntity);

		Mockito.when(authPolicyRepository.findById("45678")).thenReturn(Optional.empty());

		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test(expected = MISPServiceException.class)
	public void regenerateKeyTest_06() {
		String misp_Id = "1234";
		String licenseKey = "12345";

		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);

		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setPolicyId("45678");
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().minusYears(1));
		List<MISPLicenseEntity> mispValidLicense = new ArrayList<MISPLicenseEntity>();
		Mockito.when(mispLicenseRepository.findByMispIdandExpirydate(misp_Id)).thenReturn(mispValidLicense);

		List<MISPLicenseEntity> mispLicense = new ArrayList<MISPLicenseEntity>();
		mispLicense.add(mispLicenseEntity);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispLicense);

		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId("45678");
		partnerPolicyRequest.setStatusCode("InProgress");
		List<PartnerPolicyRequest> approvedPolicies = new ArrayList<>();
		approvedPolicies.add(partnerPolicyRequest);
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(approvedPolicies);

		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(3));
		Mockito.when(mispLicenseRepository.save(mispLicenseEntity)).thenReturn(mispLicenseEntity);

		infraProviderServiceImpl.regenerateKey(misp_Id);
	}

	@Test
	public void regenerateKeyTest_07() {
		String misp_Id = "1234";
		String licenseKey = "12345";

		Partner partner = new Partner();
		partner.setId(misp_Id);
		partner.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);

		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setLicenseKey(licenseKey);
		mispLicenseEntity.setMispId(misp_Id);
		mispLicenseEntity.setPolicyId("45678");
		mispLicenseEntity.setIsActive(true);
		mispLicenseEntity.setValidToDate(LocalDateTime.now().minusYears(1));
		List<MISPLicenseEntity> mispValidLicense = new ArrayList<MISPLicenseEntity>();
		Mockito.when(mispLicenseRepository.findByMispIdandExpirydate(misp_Id)).thenReturn(mispValidLicense);

		List<MISPLicenseEntity> mispLicense = new ArrayList<MISPLicenseEntity>();
		mispLicense.add(mispLicenseEntity);
		Mockito.when(mispLicenseRepository.findByMispId(misp_Id)).thenReturn(mispLicense);

		List<PartnerPolicyRequest> approvedPolicies = new ArrayList<>();
		Mockito.when(partnerPolicyRequestRepository.findByPartnerId(misp_Id)).thenReturn(approvedPolicies);

		mispLicenseEntity.setValidToDate(LocalDateTime.now().plusYears(3));
		Mockito.when(mispLicenseRepository.save(mispLicenseEntity)).thenReturn(mispLicenseEntity);

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
    	filterDto.setText("test");
    	filterDto.setType("all");
		SearchFilter searchDto = new SearchFilter();
		FilterValueDto filterValueDto = new FilterValueDto();
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		filterValueDto.setFilters(filterDtos);
		Mockito.when(filterHelper.filterValuesWithCode(any(), any(), any(), any())).thenReturn(filtersData);
		infraProviderServiceImpl.filterValues(filterValueDto);
	}
	
	@Test
	public void filterValuesTest01() {		
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("licenseKey");
    	filterDto.setText("abc");
    	filterDto.setType("all");
		SearchFilter searchDto = new SearchFilter();
		FilterValueDto filterValueDto = new FilterValueDto();
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		filterValueDto.setFilters(filterDtos);
		Mockito.when(filterHelper.filterValuesWithCode(any(), any(), any(), any())).thenReturn(filtersData);
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

	@Test
	public void getAllMISPLicensesTest() {
		String sortFieldName = "status";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		MISPFilterDto filterDto = new MISPFilterDto();
		filterDto.setPartnerId("partner1");
		filterDto.setOrgName("abc");

		MISPLicenseSummaryEntity entity = new MISPLicenseSummaryEntity();
		entity.setPartnerId("partner1");
		Pageable pageable = PageRequest.of(0, 10);
		Page<MISPLicenseSummaryEntity> page = new PageImpl<>(List.of(entity), pageable, 1);
		when(mispLicenseSummaryRepository.getSummaryOfAllMispLicenseDetails(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any())).thenReturn(page);
		infraProviderServiceImpl.getAllMISPLicenses(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getAllMISPLicenses_NullPointerExceptionTest() {
		infraProviderServiceImpl.getAllMISPLicenses("status", "desc", 0, 8, null);
	}

	@Test
	public void getAllMISPLicenses_PartnerServiceExceptionTest() {
		MISPFilterDto filterDto = new MISPFilterDto();
		filterDto.setPartnerId("partner1");
		filterDto.setOrgName("abc");
		infraProviderServiceImpl.getAllMISPLicenses("status", null, 0, 8, filterDto);
	}

	private MISPLicenseRequestDtoV2 getMISPLicenseRequestDtoV2() {
		MISPLicenseRequestDtoV2 dto = new MISPLicenseRequestDtoV2();
		dto.setPartnerId("partner1");
		dto.setPolicyId("policy1");
		dto.setLicenseKeyName("license1");
		dto.setExpiryDate(LocalDate.now().plusDays(10));
		return dto;
	}

	private Optional<Partner> getPartner() {
		Partner partner = new Partner();
		partner.setId("partner1");
		partner.setIsActive(true);
		partner.setPartnerTypeCode("MISP_Partner");
		partner.setPolicyGroupId("group1");
		return Optional.of(partner);
	}

	private Optional<AuthPolicy> getAuthPolicy() {
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("policy1");
		authPolicy.setName("Policy 1");
		authPolicy.setDescr("description");
		authPolicy.setIsActive(true);
		authPolicy.setPolicyFileId("{\"key\":\"value\"}");
		return Optional.of(authPolicy);
	}

	private List<PartnerPolicyRequest> getApprovedPolicies(){
		List<PartnerPolicyRequest> approvedPolicies = new ArrayList<>();
		PartnerPolicyRequest ppr = new PartnerPolicyRequest();
		ppr.setStatusCode("approved");
		ppr.setPolicyId("policy1");
		ppr.setPartner(getPartner().get());
		approvedPolicies.add(ppr);
		return approvedPolicies;
	}

	private MISPLicenseEntityV2 getMISPLicenseEntityV2() {
		MISPLicenseEntityV2 entity = new MISPLicenseEntityV2();
		MISPLicenseEntityPK pk = new MISPLicenseEntityPK();
		pk.setMispId("partner1");
		pk.setLicenseKey("xxxxxx");
		entity.setId(pk);
		entity.setLicenseKeyName("license1");
		entity.setPolicyId("policy1");
		entity.setIsActive(true);
		entity.setValidToDate(LocalDateTime.now().plusDays(10));
		entity.setCreatedDateTime(LocalDateTime.now());
		return entity;
	}

	@Test
	public void generateMISPLicenseTest_WithPolicyId() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		when(mispLicenseV2Repository.save(any())).thenReturn(getMISPLicenseEntityV2());
		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());
	}

	@Test
	public void generateMISPLicenseTest_WithoutPolicyId() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		when(mispLicenseV2Repository.save(any())).thenReturn(getMISPLicenseEntityV2());

		MISPLicenseRequestDtoV2 requestDto = getMISPLicenseRequestDtoV2();
		requestDto.setPolicyId(null);
		infraProviderServiceImpl.generateMISPLicense(requestDto);
	}

	@Test
	public void generateMISPLicenseTest_WithoutPolicyFileId() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		AuthPolicy authPolicy = getAuthPolicy().get();
		authPolicy.setPolicyFileId(null);
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.of(authPolicy));
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		when(mispLicenseV2Repository.save(any())).thenReturn(getMISPLicenseEntityV2());
		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());
	}

	@Test
	public void generateMISPLicenseTest_WithPastExpiryDate() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		MISPLicenseRequestDtoV2 requestDto = getMISPLicenseRequestDtoV2();
		requestDto.setExpiryDate(LocalDate.now().minusDays(10));
		infraProviderServiceImpl.generateMISPLicense(requestDto);
	}

	@Test
	public void generateMISPLicenseTest_WithExistLicenseKeyName() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		mispLicenseFromDb.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());
	}

	@Test
	public void generateMISPLicenseTest_WithPolicyNotApproved() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());

		List<PartnerPolicyRequest> emptyList = new ArrayList<>();
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(emptyList);

		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());
	}

	@Test
	public void generateMISPLicenseTest_WithPolicyNotExist() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.empty());

		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());
	}

	@Test
	public void generateMISPLicenseTest_WithPartnerExceptions() {
		Partner partner = getPartner().get();

		// partner not active exception
		partner.setIsActive(false);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());

		// partner type exception
		partner.setPartnerTypeCode("TYPE");
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());

		// partner not exist exception
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		infraProviderServiceImpl.generateMISPLicense(getMISPLicenseRequestDtoV2());
	}

	@Test
	public void generateMISPLicenseTest_WithNullRequests() {
		MISPLicenseRequestDtoV2 requestDto = getMISPLicenseRequestDtoV2();

		// when expiry date is null
		requestDto.setExpiryDate(null);
		infraProviderServiceImpl.generateMISPLicense(requestDto);

		// when license key name is null
		requestDto.setLicenseKeyName(null);
		infraProviderServiceImpl.generateMISPLicense(requestDto);

		// when partner id is null
		requestDto.setPartnerId(null);
		infraProviderServiceImpl.generateMISPLicense(requestDto);
	}

	private PolicyGroup getPolicyGroup() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("group1");
		policyGroup.setName("Group 1");
		policyGroup.setDesc("description");
		policyGroup.setIsActive(true);
		return policyGroup;
	}

	@Test
	public void getMISPLicenseDetailsTest_WithValidInputs() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		licensesList.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(getPolicyGroup());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());

		infraProviderServiceImpl.getMISPLicenseDetails("partner1", "policy1", "license1");
	}

	@Test
	public void getMISPLicenseDetailsTest_WithNullPointerException() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		MISPLicenseEntityV2 license = getMISPLicenseEntityV2();
		MISPLicenseEntityPK pk = license.getId();
		pk.setLicenseKey(null);
		license.setId(pk);
		licensesList.add(license);
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(getPolicyGroup());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());

		infraProviderServiceImpl.getMISPLicenseDetails("partner1", "policy1", "license1");
	}

	@Test
	public void getMISPLicenseDetailsTest_WithPolicyNotExist() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		licensesList.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(getPolicyGroup());
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.empty());

		infraProviderServiceImpl.getMISPLicenseDetails("partner1", "policy1", "license1");
	}

	@Test
	public void getMISPLicenseDetailsTest_WithPolicyGroupNotExist() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		licensesList.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(null);

		infraProviderServiceImpl.getMISPLicenseDetails("partner1", "policy1", "license1");
	}

	@Test
	public void getMISPLicenseDetailsTest_WithMultipleLicenseKeys() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		licensesList.add(getMISPLicenseEntityV2());
		licensesList.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		infraProviderServiceImpl.getMISPLicenseDetails("partner1", "policy1", "license1");
	}

	@Test
	public void getMISPLicenseDetailsTest_WithLicenseKeyNotExist() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		infraProviderServiceImpl.getMISPLicenseDetails("partner1", "policy1", "license1");
	}

	@Test
	public void getMISPLicenseDetailsTest_WithPartnerNotExist() {
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		infraProviderServiceImpl.getMISPLicenseDetails("partner1", "policy1", "license1");
	}

	@Test
	public void getMISPLicenseDetailsTest_WithoutPartnerId() {
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		infraProviderServiceImpl.getMISPLicenseDetails(null, "policy1", "license1");
	}

	private MISPDeactivateRequestDto mispDeactivateRequestDto() {
		MISPDeactivateRequestDto dto = new MISPDeactivateRequestDto();
		dto.setPolicyId("policy1");
		dto.setLicenseKeyName("license1");
		dto.setStatus("De-Activate");
		return dto;
	}

	@Test
	public void deactivateMISPLicenseTest_WithValidRequests() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		licensesList.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		MISPLicenseEntityV2 updatedEntity = getMISPLicenseEntityV2();
		updatedEntity.setIsActive(false);
		when(mispLicenseV2Repository.save(any())).thenReturn(updatedEntity);

		infraProviderServiceImpl.deactivateMISPLicense("partner1", mispDeactivateRequestDto());
	}

	@Test
	public void deactivateMISPLicenseTest_WithNullPointerException() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		licensesList.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		MISPLicenseEntityV2 updatedEntity = new MISPLicenseEntityV2();
		when(mispLicenseV2Repository.save(any())).thenReturn(updatedEntity);

		infraProviderServiceImpl.deactivateMISPLicense("partner1", mispDeactivateRequestDto());
	}

	@Test
	public void deactivateMISPLicenseTest_WithAlreadyDeactivatedLicense() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		MISPLicenseEntityV2 entity = getMISPLicenseEntityV2();
		entity.setIsActive(false);
		licensesList.add(entity);
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		infraProviderServiceImpl.deactivateMISPLicense("partner1", mispDeactivateRequestDto());
	}

	@Test
	public void deactivateMISPLicenseTest_WithMultipleLicenseFound() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		licensesList.add(getMISPLicenseEntityV2());
		licensesList.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		infraProviderServiceImpl.deactivateMISPLicense("partner1", mispDeactivateRequestDto());
	}

	@Test
	public void deactivateMISPLicenseTest_WithLicenseKeyNotExist() {
		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> licensesList = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(licensesList);

		infraProviderServiceImpl.deactivateMISPLicense("partner1", mispDeactivateRequestDto());
	}

	@Test
	public void deactivateMISPLicenseTest_WithPartnerNotActive() {
		Partner partner = getPartner().get();
		partner.setIsActive(false);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		infraProviderServiceImpl.deactivateMISPLicense("partner1", mispDeactivateRequestDto());
	}

	@Test
	public void deactivateMISPLicenseTest_WithPartnerNotExist() {
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		infraProviderServiceImpl.deactivateMISPLicense("partner1", mispDeactivateRequestDto());
	}

	@Test
	public void deactivateMISPLicenseTest_WithInvalidRequests() {
		// invalid status
		MISPDeactivateRequestDto dto = mispDeactivateRequestDto();
		dto.setStatus(null);
		infraProviderServiceImpl.deactivateMISPLicense("partner1", dto);

		// null partner id
		infraProviderServiceImpl.deactivateMISPLicense(null, mispDeactivateRequestDto());
	}

	private MISPRegenerateRequestDto mispRegenerateRequestDto() {
		MISPRegenerateRequestDto dto = new MISPRegenerateRequestDto();
		dto.setPolicyId("policy1");
		dto.setLicenseKeyName("license1");
		dto.setExpiryDate(LocalDate.now().plusDays(10));
		return dto;
	}

	@Test
	public void regenerateMISPLicenseTest_WithValidRequests() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		when(mispLicenseV2Repository.save(any())).thenReturn(getMISPLicenseEntityV2());
		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithoutPolicyId() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), eq(null))).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(getPartner());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		when(mispLicenseV2Repository.save(any())).thenReturn(getMISPLicenseEntityV2());

		MISPRegenerateRequestDto requestDto = mispRegenerateRequestDto();
		requestDto.setPolicyId(null);
		infraProviderServiceImpl.regenerateMISPLicense("partner1", requestDto);
	}

	@Test
	public void regenerateMISPLicenseTest_WithoutPolicyFileId() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		AuthPolicy authPolicy = getAuthPolicy().get();
		authPolicy.setPolicyFileId(null);
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.of(authPolicy));
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		when(mispLicenseV2Repository.save(any())).thenReturn(getMISPLicenseEntityV2());
		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithPastExpiryDate() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		MISPRegenerateRequestDto requestDto = mispRegenerateRequestDto();
		requestDto.setExpiryDate(LocalDate.now().minusDays(10));
		infraProviderServiceImpl.regenerateMISPLicense("partner1", requestDto);
	}

	@Test
	public void regenerateMISPLicenseTest_WithExistingLicenseKeyName() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(getApprovedPolicies());

		List<MISPLicenseEntityV2> mispLicenseFromDb = new ArrayList<>();
		mispLicenseFromDb.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(anyString(), anyString(), anyString())).thenReturn(mispLicenseFromDb);

		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithPolicyNotApproved() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(getAuthPolicy());

		List<PartnerPolicyRequest> emptyList = new ArrayList<>();
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(emptyList);

		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithPolicyNotExist() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(getPartner());
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.empty());

		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithPatnerNotActive() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		Partner partner = getPartner().get();
		partner.setIsActive(false);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithPatnerNotExist() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		existingLicenses.add(getMISPLicenseEntityV2());
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);

		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithLicenseKeyNotFound() {
		List<MISPLicenseEntityV2> existingLicenses = new ArrayList<>();
		when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(existingLicenses);
		infraProviderServiceImpl.regenerateMISPLicense("partner1", mispRegenerateRequestDto());
	}

	@Test
	public void regenerateMISPLicenseTest_WithInvalidRequests() {
		// null expiry date
		MISPRegenerateRequestDto dto = mispRegenerateRequestDto();
		dto.setExpiryDate(null);
		infraProviderServiceImpl.regenerateMISPLicense("partner1", dto);

		// null license key name
		dto.setLicenseKeyName(null);
		infraProviderServiceImpl.regenerateMISPLicense("partner1", dto);

		// null partner id
		infraProviderServiceImpl.regenerateMISPLicense(null, dto);
	}
}
