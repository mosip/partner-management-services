package io.mosip.pms.test.device.service.impl;

import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;

import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.DeviceFilterValueDto;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.RegistrationDeviceSubTypeRepository;
import io.mosip.pms.device.authdevice.service.DeviceDetailService;
import io.mosip.pms.device.authdevice.service.impl.DeviceDetailServiceImpl;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeviceDetailUpdateDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
public class DeviceDetailServiceTest {

	@Mock
	SearchHelper searchHelper;

	@Mock
	FilterHelper filterHelper;

	@Mock
	PageUtils pageUtils;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;

	@Mock
	FilterColumnValidator filterColumnValidator;

	@InjectMocks
	DeviceDetailService deviceDetaillService = new DeviceDetailServiceImpl();

	@Mock
	AuditUtil auditUtil;

	@Mock
	DeviceDetailRepository deviceDetailRepository;

	@Mock
	RegistrationDeviceSubTypeRepository registrationDeviceSubTypeRepository;

	@Mock
	PartnerServiceRepository partnerRepository;

	private RequestWrapper<DeviceSearchDto> deviceRequestDto;

	DeviceDetail deviceDetail = new DeviceDetail();
	Partner partner = new Partner();
	RegistrationDeviceSubType registrationDeviceSubType = new RegistrationDeviceSubType();
	DeviceDetailDto deviceDetailDto = new DeviceDetailDto();
	DeviceDetailUpdateDto deviceDetailUpdateDto = new DeviceDetailUpdateDto();
	DeviceFilterValueDto deviceFilterDto = new DeviceFilterValueDto();
	FilterDto filterDto = new FilterDto();
	SearchFilter searchDto = new SearchFilter();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	SearchFilter searchFilter = new SearchFilter();
	DeviceSearchDto deviceSearchDto = new DeviceSearchDto();

	@Before
	public void setup() {
		ReflectionTestUtils.setField(deviceDetaillService, "filterColumnValidator", filterColumnValidator);
		ReflectionTestUtils.setField(deviceDetaillService, "searchHelper", searchHelper);
		ReflectionTestUtils.setField(deviceDetaillService, "pageUtils", pageUtils);
		ReflectionTestUtils.setField(deviceDetaillService, "filterHelper", filterHelper);

		partner.setId("1234");
		registrationDeviceSubType.setCode("123");
		registrationDeviceSubType.setDeviceTypeCode("123");
		deviceDetailUpdateDto.setDeviceSubTypeCode("123");
		deviceDetailUpdateDto.setDeviceTypeCode("123");
		deviceDetailUpdateDto.setId("121");
		deviceDetailUpdateDto.setMake("make");
		deviceDetailUpdateDto.setModel("model");
		deviceDetailDto.setDeviceProviderId("1234");
		deviceDetailDto.setDeviceSubTypeCode("123");
		deviceDetailDto.setDeviceTypeCode("123");
		deviceDetailDto.setId("121");

		// Filter
		filterDto.setColumnName("model");
		filterDto.setText("");
		filterDto.setType("all");
		searchDto.setColumnName("model");
		searchDto.setFromValue("");
		searchDto.setToValue("");
		searchDto.setType("all");
		searchDto.setValue("b");
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		deviceFilterDto.setFilters(filterDtos);
		// deviceFilterDto.setDeviceProviderId("all");
		deviceFilterDto.setPurpose(Purpose.REGISTRATION);
		// Search
		searchSort.setSortField("model");
		searchSort.setSortType("asc");
		searchFilter.setColumnName("model");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("STARTSWITH");
		searchFilter.setValue("b");
		List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
		searchDtos1.add(searchSort);
		List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
		searchfilterDtos.add(searchFilter);
		deviceSearchDto.setFilters(searchfilterDtos);
		deviceSearchDto.setPagination(pagination);
		deviceSearchDto.setSort(searchDtos1);
		deviceSearchDto.setPurpose(Purpose.REGISTRATION.toString());
		pagination.setPageFetch(10);
		pagination.setPageStart(0);
		
		deviceDetailDto.setMake("make");
		deviceDetailDto.setModel("model");
		deviceDetail.setApprovalStatus("pending");
		deviceDetail.setDeviceProviderId("1234");
		deviceDetail.setDeviceSubTypeCode("123");
		deviceDetail.setDeviceTypeCode("123");
		deviceDetail.setId("121");
		deviceDetail.setIsActive(true);
		deviceDetail.setCrBy("110005");
		deviceDetail.setUpdBy("110005");
		deviceDetail.setCrDtimes(LocalDateTime.now());
		deviceDetail.setUpdDtimes(LocalDateTime.now());
		deviceDetail.setMake("make");
		deviceDetail.setModel("model");
		deviceDetail.setPartnerOrganizationName("pog");
		Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).save(Mockito.any(DeviceDetail.class));
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository)
				.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.doReturn(registrationDeviceSubType).when(registrationDeviceSubTypeRepository)
				.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString(),
						Mockito.anyString());
		Mockito.doReturn(partner).when(partnerRepository)
				.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());
	}

	@Test
	public void searchDeviceTypeTest() throws Exception {
		objectMapper.writeValueAsString(deviceRequestDto);
		DeviceDetail device = new DeviceDetail();
		device.setId("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(device))).when(searchHelper).search(Mockito.any(),
				Mockito.any(), Mockito.any());
		deviceDetaillService.searchDeviceType(DeviceDetail.class, deviceSearchDto);
	}

	@Test
	public void searchDeviceDetailTest() throws Exception {
		SearchFilter partnerOrganizationNameFilter = new SearchFilter();
		partnerOrganizationNameFilter.setColumnName("partnerOrganizationName");
		partnerOrganizationNameFilter.setValue("m");
		deviceSearchDto.getFilters().add(partnerOrganizationNameFilter);
		objectMapper.writeValueAsString(deviceSearchDto);
		DeviceDetail device = new DeviceDetail();
		device.setId("1001");
		Mockito.when(partnerRepository.findByName("m")).thenReturn(partner);
		Mockito.doReturn(new PageImpl<>(Arrays.asList(device))).when(searchHelper).search(Mockito.any(),
				Mockito.any(),Mockito.anyString());
		deviceDetaillService.searchDeviceDetails(DeviceDetail.class, deviceSearchDto);
	}
	
	@Test
	public void searchDeviceDetailTestS() throws Exception {
		SearchFilter partnerOrganizationNameFilter = new SearchFilter();
		partnerOrganizationNameFilter.setColumnName("partnerOrganizationName");
		partnerOrganizationNameFilter.setValue("m");
		deviceSearchDto.getFilters().add(partnerOrganizationNameFilter);
		objectMapper.writeValueAsString(deviceSearchDto);
		DeviceDetail device = new DeviceDetail();
		device.setId("1001");
		Mockito.when(partnerRepository.findByNameIgnoreCase("m")).thenReturn(List.of("1234"));
		Mockito.doReturn(new PageImpl<>(Arrays.asList(device))).when(searchHelper).search(Mockito.any(),
				Mockito.any(),Mockito.anyString());
		deviceDetaillService.searchDeviceDetails(DeviceDetail.class, deviceSearchDto);
	}
	

	@Test
	public void deviceFilterValuesTest() throws Exception {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		deviceDetaillService.deviceFilterValues(deviceFilterDto);
	}

	@Test
	public void deviceFilterValuesTest01() throws Exception {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		FilterData filterData = new FilterData("test","test");		
		List<FilterData> filtersData = new ArrayList<>();
		filtersData.add(filterData);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filtersData);
		deviceDetaillService.deviceFilterValues(deviceFilterDto);
	}

	@Test
	public void deviceFilterValuesTest1() throws Exception {
		deviceDetaillService.deviceFilterValues(deviceFilterDto);
	}

	@Test
	public void deviceTypeFilterValuesTest() throws Exception {		
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filtersData);
		deviceDetaillService.deviceTypeFilterValues(deviceFilterDto);
	}

	@Test
	public void deviceTypeFilterValuesTest1() throws Exception {
		deviceDetaillService.deviceTypeFilterValues(deviceFilterDto);		
	}

	@Test
	public void deviceSubTypeFilterValuesTest() throws Exception {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filtersData);
		deviceDetaillService.deviceSubTypeFilterValues(deviceFilterDto);
	}

	@Test
	public void deviceSubTypeFilterValuesTest1() throws Exception {
		deviceDetaillService.deviceSubTypeFilterValues(deviceFilterDto);
	}

	@Test
	public void createDeviceDetailTest() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		assertTrue(deviceDetaillService.createDeviceDetails(deviceDetailDto).getId().equals("121"));
	}

	@Test(expected = RequestException.class)
	public void createDeviceDetailNoPartnerTest() throws Exception {
		Mockito.doReturn(null).when(partnerRepository)
				.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());

		deviceDetaillService.createDeviceDetails(deviceDetailDto);
	}

	@Test(expected = RequestException.class)
	public void createDeviceDetailNoSubtypeTest() throws Exception {
		Mockito.doReturn(null).when(registrationDeviceSubTypeRepository)
				.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString(),
						Mockito.anyString());
		deviceDetaillService.createDeviceDetails(deviceDetailDto);
	}

	@Test(expected = RequestException.class)
	public void updateDeviceDetailNoSubtypeTest() throws Exception {
		Mockito.doReturn(null).when(registrationDeviceSubTypeRepository)
				.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString(),
						Mockito.anyString());
		deviceDetaillService.updateDeviceDetails(deviceDetailUpdateDto);
	}

	@Test(expected = RequestException.class)
	public void createDeviceDetailAlreadyExistsTest() throws Exception {
		Mockito.when(deviceDetailRepository.findUniqueDeviceDetail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString())).thenReturn(deviceDetail);
		deviceDetaillService.createDeviceDetails(deviceDetailDto);
	}

	@Test
	public void updateDeviceDetailTest() throws Exception {
		assertTrue(deviceDetaillService.updateDeviceDetails(deviceDetailUpdateDto).getId().equals("121"));
	}

	@Test(expected = RequestException.class)
	public void updateDeviceDetailNotFoundTest() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository)
				.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		deviceDetaillService.updateDeviceDetails(deviceDetailUpdateDto);
	}

	@Test
	public void updateDeviceDetailStatusTest_Approve() {
		deviceDetaillService.updateDeviceDetailStatus(statusUpdateRequest("Activate"));
	}

	@Test
	public void updateDeviceDetailStatusTest_Reject() {
		deviceDetaillService.updateDeviceDetailStatus(statusUpdateRequest("De-activate"));
	}

	@Test(expected = RequestException.class)
	public void updateDeviceDetailStatusTest_Status_Exception() {
		deviceDetaillService.updateDeviceDetailStatus(statusUpdateRequest("De-Activate"));
	}

	@Test(expected = RequestException.class)
	public void updateDeviceDetailStatusTest_DeviceDetail_Exception() {
		UpdateDeviceDetailStatusDto request = statusUpdateRequest("De-Activate");
		request.setId("34567");
		Mockito.doReturn(null).when(deviceDetailRepository)
				.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		deviceDetaillService.updateDeviceDetailStatus(request);
	}

	private UpdateDeviceDetailStatusDto statusUpdateRequest(String status) {
		UpdateDeviceDetailStatusDto request = new UpdateDeviceDetailStatusDto();
		request.setApprovalStatus(status);
		request.setId("121");
		return request;
	}
}
