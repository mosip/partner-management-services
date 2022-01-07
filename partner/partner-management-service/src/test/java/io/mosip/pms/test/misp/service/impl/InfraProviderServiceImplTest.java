package io.mosip.pms.test.misp.service.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.MispLicenseRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.impl.InfraProviderServiceImpl;

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
		MISPLicenseEntity mispLicenseEntity = new MISPLicenseEntity();
		mispLicenseEntity.setIsActive(true);
		Optional<Partner> opt_partner = Optional.of(partner);
		Mockito.when(partnerRepository.findById(misp_Id)).thenReturn(opt_partner);
		infraProviderServiceImpl.approveInfraProvider(misp_Id);
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
}
