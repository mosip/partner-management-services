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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.SearchHelper;
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
	
	@InjectMocks
	SecureBiometricInterfaceService secureBiometricInterfaceService=new SecureBiometricInterfaceServiceImpl();
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
    }

	@Test
    public void updateDeviceDetailTest() throws Exception {
		assertTrue(secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto).getId().equals("1234"));
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
}
