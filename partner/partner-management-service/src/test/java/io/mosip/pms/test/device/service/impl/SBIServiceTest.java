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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.entity.DeviceDetailSBIPK;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterfaceHistory;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.authdevice.service.impl.SecureBiometricInterfaceServiceImpl;
import io.mosip.pms.device.constant.DeviceDetailExceptionsConstant;
import io.mosip.pms.device.constant.SecureBiometricInterfaceConstant;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
public class SBIServiceTest {
	@Autowired
	private ObjectMapper objectMapper;
	
	@Mock
	PageUtils pageUtils;
	
	@Mock
	SearchHelper searchHelper;
	
	@Mock
	FilterColumnValidator filterColumnValidator;
	
	@Mock
	FilterHelper filterHelper;
	
	@InjectMocks
	SecureBiometricInterfaceService secureBiometricInterfaceService=new SecureBiometricInterfaceServiceImpl();
	
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	@Mock
	DeviceDetailRepository deviceDetailRepository;
	
	@Mock
	AuditUtil auditUtil;
	
	@Mock
	SecureBiometricInterfaceRepository sbiRepository;
	
	@Mock
	SecureBiometricInterfaceHistoryRepository sbiHistoryRepository;
	
	@Mock
	PartnerServiceRepository partnerRepository;
	
	@Mock
	DeviceDetailSbiRepository deviceDetailSbiRepository;
	
	private RequestWrapper<DeviceSearchDto> deviceRequestDto;
	DeviceDetail deviceDetail=new DeviceDetail();
	SecureBiometricInterfaceCreateDto sbicreatedto = new SecureBiometricInterfaceCreateDto();
	SecureBiometricInterfaceUpdateDto sbidto = new SecureBiometricInterfaceUpdateDto();
	SecureBiometricInterface secureBiometricInterface=new SecureBiometricInterface();
	SecureBiometricInterfaceHistory secureBiometricInterfaceHistory=new SecureBiometricInterfaceHistory();
	FilterDto filterDto = new FilterDto();
	SearchFilter searchDto = new SearchFilter();
	DeviceSearchDto deviceSearchDto = new DeviceSearchDto();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	SearchFilter searchFilter = new SearchFilter();
	@Before
	public void setup() {
		secureBiometricInterfaceHistory.setApprovalStatus("pending");
		secureBiometricInterfaceHistory.setProviderId("1234");
		secureBiometricInterfaceHistory.setPartnerOrgName("airtel");
		secureBiometricInterfaceHistory.setSwBinaryHAsh("swb".getBytes());
		secureBiometricInterfaceHistory.setEffectDateTime(LocalDateTime.now());
		secureBiometricInterfaceHistory.setSwCreateDateTime(LocalDateTime.now());
		secureBiometricInterfaceHistory.setSwExpiryDateTime(LocalDateTime.now());
		secureBiometricInterfaceHistory.setActive(true);
		secureBiometricInterfaceHistory.setCrBy("110005");
		secureBiometricInterfaceHistory.setCrDtimes(LocalDateTime.now());
		secureBiometricInterfaceHistory.setUpdDtimes(LocalDateTime.now());
		secureBiometricInterfaceHistory.setUpdBy("110005");
		secureBiometricInterfaceHistory.setSwVersion("v1");
		secureBiometricInterfaceHistory.setId("1234");
		secureBiometricInterface.setApprovalStatus("pending");
		secureBiometricInterface.setSwBinaryHash("swb".getBytes());
		secureBiometricInterface.setSwCreateDateTime(LocalDateTime.now());
		secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now());
		secureBiometricInterface.setActive(true);
		secureBiometricInterface.setCrBy("110005");
		secureBiometricInterface.setCrDtimes(LocalDateTime.now());
		secureBiometricInterface.setUpdDtimes(LocalDateTime.now());
		secureBiometricInterface.setUpdBy("110005");
		secureBiometricInterface.setSwVersion("v1");
		secureBiometricInterface.setId("1234");

		sbidto.setSwBinaryHash("swb");
		sbidto.setSwCreateDateTime(LocalDateTime.now());
		sbidto.setSwExpiryDateTime(LocalDateTime.now());
		sbidto.setIsActive(true);
		sbidto.setSwVersion("v1");
		sbidto.setId("1234");
		//Search
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
    	deviceSearchDto.setPurpose(Purpose.REGISTRATION.toString());
    	deviceSearchDto.setSort(searchDtos1);
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
		//Filter
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
    	deviceSearchDto.setFilters(searchfilterDtos);
    	deviceSearchDto.setPagination(pagination);
    	deviceSearchDto.setPurpose(Purpose.REGISTRATION.toString());
    	deviceSearchDto.setSort(searchDtos1);
				
		sbicreatedto.setSwBinaryHash("swb");
		sbicreatedto.setSwCreateDateTime(LocalDateTime.now());
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now());
		sbicreatedto.setProviderId("1234");
		sbicreatedto.setSwVersion("v1");
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
    	deviceDetail.setIsActive(true);
    	Partner validPartner = new Partner();
    	validPartner.setId("1234");
    	validPartner.setName("partner");
		Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(secureBiometricInterface).when(sbiRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		Mockito.doReturn(secureBiometricInterface).when(sbiRepository).save(Mockito.any());
		Mockito.doReturn(secureBiometricInterfaceHistory).when(sbiHistoryRepository).save(Mockito.any());
		Mockito.doReturn(validPartner).when(partnerRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());
		}
	
	@Test
	public void sbiSearchTest() throws Exception{
		objectMapper.writeValueAsString(deviceRequestDto);
		DeviceDetail device = new DeviceDetail();
		device.setId("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(secureBiometricInterface))).when(searchHelper).search(Mockito.any(),Mockito.any(), Mockito.any());
		secureBiometricInterfaceService.searchSecureBiometricInterface(SecureBiometricInterface.class, deviceSearchDto);
	}
	
	@Test
	public void sbiSearchTest1() throws Exception{
		objectMapper.writeValueAsString(deviceRequestDto);
		DeviceDetail device = new DeviceDetail();
		device.setId("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(secureBiometricInterface))).when(searchHelper).search(Mockito.any(),Mockito.any(), Mockito.any());
		secureBiometricInterfaceService.searchSecureBiometricInterface(SecureBiometricInterface.class, deviceSearchDto);
	}
	
	@Test
    public void createSBITest() throws Exception {
		assertTrue(secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto).getId().equals("1234"));
		Mockito.when(sbiRepository.findByProviderIdAndSwVersion(Mockito.any(),Mockito.any())).thenReturn(List.of(secureBiometricInterface));
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorCode()));
		}
    }
	
	@Test
    public void createSBITest01() throws Exception {
		sbicreatedto.setSwCreateDateTime(LocalDateTime.now());
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now().minusDays(3));
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_LESSTHAN_EXPIRYDATE.getErrorCode()));
		}
    }
	
	@Test
    public void createSBITest02() throws Exception {
		sbicreatedto.setSwCreateDateTime(LocalDateTime.now().minusDays(4));
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now().minusDays(3));
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_BE_GREATERTHAN_TODAYSDATE.getErrorCode()));
		}
    }
	
	@Test
    public void createSBITest03() throws Exception {
		Mockito.when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(null);
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorCode()));
		}
    }

	@Test
    public void updateDeviceDetailTest() throws Exception {
		assertTrue(secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto).getId().equals("1234"));
		SecureBiometricInterface sbiFromDb = secureBiometricInterface;
		sbiFromDb.setId("789");
		Mockito.when(sbiRepository.findByProviderIdAndSwVersion(Mockito.any(),Mockito.any())).thenReturn(List.of(sbiFromDb));
		try {
			secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorCode()));
		}				
    }
	
	@Test(expected=RequestException.class)
    public void updateDeviceDetailNotFoundTest() throws Exception {
		Mockito.doReturn(null).when(sbiRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto);
    }
	
	@Test
	public void updateDeviceDetailStatusTest_Approve() {
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("Activate"));
	}
	
	@Test
	public void updateDeviceDetailStatusTest_Reject() {
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("De-activate"));
	}
	
	@Test(expected = RequestException.class)
	public void updateDeviceDetailStatusTest_Status_Exception() {
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("De-Activate"));
	}
	
	@Test(expected = RequestException.class)
	public void updateDeviceDetailStatusTest_DeviceDetail_Exception() {
		SecureBiometricInterfaceStatusUpdateDto request = statusUpdateRequest("De-Activate");
		request.setId("34567");
		Mockito.doReturn(null).when(sbiRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(request);
	}
	
	private SecureBiometricInterfaceStatusUpdateDto statusUpdateRequest(String status) {
		SecureBiometricInterfaceStatusUpdateDto request = new SecureBiometricInterfaceStatusUpdateDto();
		request.setApprovalStatus(status);
		request.setId("121");
		return request;
	}
	
	@Test
	public void mapDeviceDetailAndSbiTest01() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(new DeviceDetailSBI());
		assertTrue(secureBiometricInterfaceService.mapDeviceDetailAndSbi(request).equals("Mapping already exists"));
	}
	
	@Test
	public void mapDeviceDetailAndSbiTest02() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(null);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(request.getDeviceDetailId())).thenReturn(null);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorCode()));
		}
	}
	
	@Test
	public void mapDeviceDetailAndSbiTest03() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(null);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getSbiId())).thenReturn(null);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode()));
		}
	}
	
	@Test
	public void mapDeviceDetailAndSbiTest04() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");
		deviceDetail.setDeviceProviderId("12345");
		secureBiometricInterface.setProviderId("3456");
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(null);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getSbiId())).thenReturn(secureBiometricInterface);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.DD_SBI_PROVIDER_NOT_MATCHING.getErrorCode()));
		}
	}
	
	@Test
	public void mapDeviceDetailAndSbiTest06() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");
		deviceDetail.setDeviceProviderId("12345");
		secureBiometricInterface.setProviderId("3456");
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(null);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getSbiId())).thenReturn(secureBiometricInterface);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.DD_SBI_PROVIDER_NOT_MATCHING.getErrorCode()));
		}
	}
	
	@Test
	public void mapDeviceDetailAndSbiTest05() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");
		deviceDetail.setDeviceProviderId("12345");
		secureBiometricInterface.setProviderId("12345");
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(null);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(request.getSbiId())).thenReturn(secureBiometricInterface);
		assertTrue(secureBiometricInterfaceService.mapDeviceDetailAndSbi(request).equals("Success"));
		deviceDetail.setIsActive(false);
		deviceDetail.setApprovalStatus(CommonConstant.REJECTED);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(DeviceDetailExceptionsConstant.DEVICE_DETAIL_REJECTED.getErrorCode()));
		}
		
		deviceDetail.setIsActive(false);
		deviceDetail.setApprovalStatus(CommonConstant.PENDING_APPROVAL);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_APPROVED.getErrorCode()));
		}
		deviceDetail.setIsActive(true);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		secureBiometricInterface.setActive(false);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(request.getSbiId())).thenReturn(secureBiometricInterface);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.SBI_NOT_APPROVED.getErrorCode()));
		}
	}
	
	@Test
	public void deleteDeviceDetailAndSbiMappingTest01() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");		
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(null);
		try {
			secureBiometricInterfaceService.deleteDeviceDetailAndSbiMapping(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.DD_SBI_MAPPING_NOT_EXISTS.getErrorCode()));
		}
	}
	
	@Test
	public void deleteDeviceDetailAndSbiMappingTest02() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		DeviceDetailSBI validRecords = new DeviceDetailSBI();
		DeviceDetailSBIPK key = new DeviceDetailSBIPK();
		key.setDeviceDetailId("deviceDetailId");
		key.setSbiId("sbiid");
		validRecords.setId(key);		
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");		
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(validRecords);
		assertTrue(secureBiometricInterfaceService.deleteDeviceDetailAndSbiMapping(request).equals("Success"));	
	}
	
	@Test
	public void searchMappedDeviceDetailsTest01() {
		DeviceSearchDto searchDto = new DeviceSearchDto();
		SearchFilter filterSearch = new SearchFilter();
		filterSearch.setValue("Test");
		filterSearch.setColumnName("deviceDetailId");
		Partner validPartner = new Partner();
		validPartner.setId("partner");
		validPartner.setName("partnerName");
		List<SearchFilter> searchFilters = new ArrayList<>();
		searchFilters.add(filterSearch);
		searchDto.setFilters(searchFilters);
		DeviceDetailSBI validRecords = new DeviceDetailSBI();
		DeviceDetailSBIPK key = new DeviceDetailSBIPK();
		key.setDeviceDetailId("deviceDetailId");
		key.setSbiId("sbiid");
		validRecords.setId(key);
		Mockito.doReturn(new PageImpl<>(Arrays.asList(validRecords))).when(searchHelper).search(Mockito.any(),
				Mockito.any(),Mockito.anyString());
		secureBiometricInterfaceService.searchMappedDeviceDetails(DeviceDetailSBI.class, searchDto);				
	}
	
	@Test
	public void searchMappedDeviceDetailsTest02() {
		DeviceSearchDto searchDto = new DeviceSearchDto();
		SearchFilter filterSearch = new SearchFilter();
		filterSearch.setValue("Test");
		filterSearch.setColumnName("providerId");
		Partner validPartner = new Partner();
		validPartner.setId("partner");
		validPartner.setName("partnerName");
		List<SearchFilter> searchFilters = new ArrayList<>();
		searchFilters.add(filterSearch);
		searchDto.setFilters(searchFilters);
		DeviceDetailSBI validRecords = new DeviceDetailSBI();
		DeviceDetailSBIPK key = new DeviceDetailSBIPK();
		key.setDeviceDetailId("deviceDetailId");
		key.setSbiId("sbiid");
		validRecords.setId(key);
		Mockito.doReturn(new PageImpl<>(Arrays.asList(validRecords))).when(searchHelper).search(Mockito.any(),
				Mockito.any(),Mockito.anyString());
		DeviceDetail deviceDetail = new DeviceDetail();
		SecureBiometricInterface sbi = new SecureBiometricInterface();
		deviceDetail.setId("deviceDetailId");
		deviceDetail.setMake("001");
		deviceDetail.setModel("001");
		sbi.setActive(true);
		sbi.setId("sbiid");
		sbi.setProviderId("12345");
		Mockito.when(deviceDetailRepository.findAll()).thenReturn(List.of(deviceDetail));
		Mockito.when(sbiRepository.findAll()).thenReturn(List.of(sbi));
		secureBiometricInterfaceService.searchMappedDeviceDetails(DeviceDetailSBI.class, searchDto);				
	}
	
	@Test
	public void filterValuesTest() {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		List<FilterData> filtersData = new ArrayList<>();
		FilterData filterData = new FilterData("test","test");
		filtersData.add(filterData);
		FilterValueDto filterValueDto = new FilterValueDto();
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		filterValueDto.setFilters(filterDtos);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filtersData);
		secureBiometricInterfaceService.filterValues(filterValueDto);
	}
}
