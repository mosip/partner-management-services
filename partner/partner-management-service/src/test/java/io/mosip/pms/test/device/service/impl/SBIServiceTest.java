package io.mosip.pms.test.device.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.authdevice.entity.SbiSummaryEntity;
import io.mosip.pms.device.authdevice.repository.SbiSummaryRepository;
import io.mosip.pms.device.dto.SbiFilterDto;
import io.mosip.pms.device.response.dto.SbiDetailsResponseDto;
import io.mosip.pms.device.response.dto.SbiSummaryDto;
import io.mosip.pms.partner.dto.DeviceDto;
import io.mosip.pms.partner.util.PartnerHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
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
import io.mosip.pms.common.dto.PageResponseV2Dto;
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
import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterfaceHistory;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.repository.RegistrationDeviceSubTypeRepository;
import io.mosip.pms.device.authdevice.service.impl.SecureBiometricInterfaceServiceImpl;
import io.mosip.pms.device.constant.DeviceDetailExceptionsConstant;
import io.mosip.pms.device.constant.SecureBiometricInterfaceConstant;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.DeactivateSbiRequestDto;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
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
	SecureBiometricInterfaceServiceImpl secureBiometricInterfaceService;

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
	PolicyGroupRepository policyGroupRepository;

	@Mock
	DeviceDetailSbiRepository deviceDetailSbiRepository;

	@Mock
	RegistrationDeviceSubTypeRepository registrationDeviceSubTypeRepository;

	@Mock
	SbiSummaryRepository sbiSummaryRepository;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@Mock
	PartnerHelper partnerHelper;

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
		secureBiometricInterfaceHistory.setSwExpiryDateTime(LocalDateTime.now().plusYears(1));
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
		secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now().plusYears(1));
		secureBiometricInterface.setActive(true);
		secureBiometricInterface.setCrBy("110005");
		secureBiometricInterface.setCrDtimes(LocalDateTime.now());
		secureBiometricInterface.setUpdDtimes(LocalDateTime.now());
		secureBiometricInterface.setUpdBy("110005");
		secureBiometricInterface.setSwVersion("v1");
		secureBiometricInterface.setId("1234");

		sbidto.setSwBinaryHash("swb");
		sbidto.setSwCreateDateTime(LocalDateTime.now());
		sbidto.setSwExpiryDateTime(LocalDateTime.now().plusYears(1));
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
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now().plusYears(1));
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
		doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any());
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

	@Before
	public void setUp() throws Exception {
		setPrivateField(secureBiometricInterfaceService, "maxAllowedExpiryYear", 10);
		setPrivateField(secureBiometricInterfaceService, "maxAllowedCreatedYear", 10);
	}

	private void setPrivateField(Object targetObject, String fieldName, Object value) throws Exception {
		Field field = targetObject.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(targetObject, value);
	}

	@Test
    public void createSBITest() throws Exception {
		Mockito.when(sbiRepository.findByProviderIdAndSwVersion(Mockito.any(),Mockito.any())).thenReturn(List.of(secureBiometricInterface));
		Partner partner = new Partner();
		partner.setId("1234");
		partner.setName("abc");
		Mockito.when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString())).thenReturn(partner);
		List<SecureBiometricInterface> sbiList = new ArrayList<>();
		Mockito.when(sbiRepository.findByProviderIdAndSwVersion(anyString(), anyString())).thenReturn(sbiList);
		secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
    }

	@Test
	public void createSBITest05() throws Exception {
		Mockito.when(sbiRepository.findByProviderIdAndSwVersion(Mockito.any(),Mockito.any())).thenReturn(List.of(secureBiometricInterface));
		Mockito.when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString())).thenReturn(null);
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		} catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorCode()));
		}
	}

	@Test
	public void createSBITest06() throws Exception {
		Mockito.when(sbiRepository.findByProviderIdAndSwVersion(Mockito.any(),Mockito.any())).thenReturn(List.of(secureBiometricInterface));
		Partner partner = new Partner();
		partner.setId("1234");
		partner.setName("abc");
		Mockito.when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString())).thenReturn(partner);
		List<SecureBiometricInterface> sbiList = new ArrayList<>();
		sbiList.add(secureBiometricInterface);
		Mockito.when(sbiRepository.findByProviderIdAndSwVersion(anyString(), anyString())).thenReturn(sbiList);
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		} catch (RequestException e) {
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
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_BE_GREATERTHAN_TODAYSDATE.getErrorCode()));
		}
    }

	@Test
    public void createSBITest02() throws Exception {
		sbicreatedto.setSwCreateDateTime(LocalDateTime.now().plusDays(4));
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now().plusYears(3));
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_PAST_OR_TODAY.getErrorCode()));
		}
    }

	@Test
    public void createSBITest03() throws Exception {
		sbicreatedto.setSwCreateDateTime(LocalDateTime.now());
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now().plusYears(20));
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorCode()));
		}
    }

	@Test
	public void createSBITest04() throws Exception {
		sbicreatedto.setSwCreateDateTime(LocalDateTime.now().minusYears(20));
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now().plusYears(3));
		try {
			secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.CREATEDDATE_SHOULD_NOT_BE_LESS_THAN_TEN_YEARS.getErrorCode()));
		}
	}

	@Test
    public void updateSecureBiometricInterfaceTest() throws Exception {
		try {
			secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorCode()));
		}
    }

	@Test(expected=RequestException.class)
    public void updateSecureBiometricInterfaceNotFoundTest() throws Exception {
		Mockito.doReturn(null).when(sbiRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto);
    }

	@Test(expected=RequestException.class)
	public void updateSecureBiometricInterfaceExistTest() throws Exception {
		secureBiometricInterface.setProviderId("12345");
		Mockito.doReturn(secureBiometricInterface).when(sbiRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		List<SecureBiometricInterface> sbiList = new ArrayList<>();
		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setId("090");
		sbiList.add(sbi);
		Mockito.doReturn(sbiList).when(sbiRepository).findByProviderIdAndSwVersion(Mockito.anyString(), Mockito.anyString());
		secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto);
	}

	@Test
	public void updateSecureBiometricInterfaceStatusTest_Approve() {
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("Activate"));
	}

	@Test
	public void updateSecureBiometricInterfaceStatusTest_Reject() {
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("De-activate"));
	}

	@Test(expected = RequestException.class)
	public void updateSecureBiometricInterfaceStatusTest_sbiAlreadyApprovedException() {
		secureBiometricInterface.setApprovalStatus("approved");
		secureBiometricInterface.setActive(true);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(anyString())).thenReturn(secureBiometricInterface);
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("De-activate"));
	}

	@Test(expected = RequestException.class)
	public void updateSecureBiometricInterfaceStatusTest_sbiAlreadyRejectedException() {
		secureBiometricInterface.setApprovalStatus("rejected");
		secureBiometricInterface.setActive(false);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(anyString())).thenReturn(secureBiometricInterface);
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("De-activate"));
	}

	@Test(expected = RequestException.class)
	public void updateSecureBiometricInterfaceStatusTest_Status_Exception() {
		secureBiometricInterfaceService.updateSecureBiometricInterfaceStatus(statusUpdateRequest("De-Activate"));
	}

	@Test(expected = RequestException.class)
	public void updateSecureBiometricInterfaceStatusTest_DeviceDetail_Exception() {
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
	public void mapDeviceDetailAndSbiTest07() {
		DeviceDetailSBIMappingDto request = new DeviceDetailSBIMappingDto();
		request.setDeviceDetailId("deviceDetailId");
		request.setSbiId("sbiid");
		deviceDetail.setDeviceProviderId("12345");
		secureBiometricInterface.setProviderId("12345");
		secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now().minusDays(10));
		Mockito.when(deviceDetailSbiRepository.findByDeviceDetailAndSbi(request.getDeviceDetailId(),request.getSbiId())).thenReturn(null);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getDeviceDetailId())).thenReturn(deviceDetail);
		Mockito.when(sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(request.getSbiId())).thenReturn(secureBiometricInterface);
		try {
			secureBiometricInterfaceService.mapDeviceDetailAndSbi(request);
		}catch(RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(SecureBiometricInterfaceConstant.SBI_EXPIRED.getErrorCode()));
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

	@Test
	public void addDeviceToSbiTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeviceDetailDto requestDto = new DeviceDetailDto();
		requestDto.setId(null);
		requestDto.setDeviceTypeCode("Finger");
		requestDto.setDeviceSubTypeCode("Slap");
		requestDto.setMake("make");
		requestDto.setModel("model");
		requestDto.setDeviceProviderId("123");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		RegistrationDeviceSubType registrationDeviceSubType = new RegistrationDeviceSubType();
		registrationDeviceSubType.setCode("Slap");
		registrationDeviceSubType.setDeviceTypeCode("Finger");
		when(registrationDeviceSubTypeRepository.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString(), anyString())). thenReturn(registrationDeviceSubType);
		when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString())).thenReturn(partner);
		when(deviceDetailRepository.findUniqueDeviceDetail(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);

		DeviceDetail deviceDetail = new DeviceDetail();
		deviceDetail.setId("123456");
		deviceDetail.setDeviceProviderId("123");
		deviceDetail.setDeviceTypeCode("Finger");
		deviceDetail.setDeviceSubTypeCode("Slap");
		deviceDetail.setMake("make");
		deviceDetail.setModel("model");
		deviceDetail.setApprovalStatus("pending_approval");
		deviceDetail.setIsActive(false);
		when(deviceDetailRepository.save(any())).thenReturn(deviceDetail);
		when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(anyString(), anyString(), anyString())).thenReturn(null);

		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setActive(true);
		sbi.setApprovalStatus("approved");
		sbi.setProviderId("123");
		sbi.setSwCreateDateTime(LocalDateTime.now());
		sbi.setSwExpiryDateTime(LocalDateTime.now().plusDays(1));
		when(sbiRepository.findById(anyString())).thenReturn(Optional.of(sbi));
		when(deviceDetailRepository.findById(anyString())).thenReturn(Optional.of(deviceDetail));

		DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
		DeviceDetailSBIPK deviceDetailSBIPK = new DeviceDetailSBIPK();
		deviceDetailSBIPK.setSbiId("sbi123");
		deviceDetailSBIPK.setDeviceDetailId("123456");
		deviceDetailSBI.setId(deviceDetailSBIPK);
		deviceDetailSBI.setPartnerName("abc");
		deviceDetailSBI.setProviderId("123");
		deviceDetailSBI.setIsActive(false);
		when(deviceDetailSbiRepository.save(any())).thenReturn(deviceDetailSBI);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");

		partnerList = new ArrayList<>();
		Partner partner1 = new Partner();
		partnerList.add(partner1);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");
	}

	@Test
	public void addDeviceToSbiTest1() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeviceDetailDto requestDto = new DeviceDetailDto();
		requestDto.setId(null);
		requestDto.setDeviceTypeCode("Finger");
		requestDto.setDeviceSubTypeCode("Slap");
		requestDto.setMake("make");
		requestDto.setModel("model");
		requestDto.setDeviceProviderId("123");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		RegistrationDeviceSubType registrationDeviceSubType = new RegistrationDeviceSubType();
		registrationDeviceSubType.setCode("Slap");
		registrationDeviceSubType.setDeviceTypeCode("Finger");
		when(registrationDeviceSubTypeRepository.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString(), anyString())). thenReturn(registrationDeviceSubType);
		when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString())).thenReturn(partner);

		DeviceDetail deviceDetail = new DeviceDetail();
		deviceDetail.setId("123456");
		deviceDetail.setDeviceProviderId("123");
		deviceDetail.setDeviceTypeCode("Finger");
		deviceDetail.setDeviceSubTypeCode("Slap");
		deviceDetail.setMake("make");
		deviceDetail.setModel("model");
		deviceDetail.setApprovalStatus("pending_approval");
		deviceDetail.setIsActive(false);
		when(deviceDetailRepository.findUniqueDeviceDetail(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(deviceDetail);
		when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(anyString(), anyString(), anyString())).thenReturn(null);

		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setActive(true);
		sbi.setApprovalStatus("approved");
		sbi.setProviderId("123");
		sbi.setSwCreateDateTime(LocalDateTime.now());
		sbi.setSwExpiryDateTime(LocalDateTime.now().plusDays(1));
		when(sbiRepository.findById(anyString())).thenReturn(Optional.of(sbi));
		when(deviceDetailRepository.findById(anyString())).thenReturn(Optional.of(deviceDetail));

		DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
		DeviceDetailSBIPK deviceDetailSBIPK = new DeviceDetailSBIPK();
		deviceDetailSBIPK.setSbiId("sbi123");
		deviceDetailSBIPK.setDeviceDetailId("123456");
		deviceDetailSBI.setId(deviceDetailSBIPK);
		deviceDetailSBI.setPartnerName("abc");
		deviceDetailSBI.setProviderId("123");
		deviceDetailSBI.setIsActive(false);
		when(deviceDetailSbiRepository.save(any())).thenReturn(deviceDetailSBI);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");
	}

	@Test
	public void addDeviceToSbiExceptionTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeviceDetailDto requestDto = new DeviceDetailDto();
		requestDto.setId(null);
		requestDto.setDeviceTypeCode("Finger");
		requestDto.setDeviceSubTypeCode("Slap");
		requestDto.setMake("make");
		requestDto.setModel("model");
		requestDto.setDeviceProviderId("123");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		RegistrationDeviceSubType registrationDeviceSubType = new RegistrationDeviceSubType();
		registrationDeviceSubType.setCode("Slap");
		registrationDeviceSubType.setDeviceTypeCode("Finger");
		when(registrationDeviceSubTypeRepository.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString(), anyString())). thenReturn(registrationDeviceSubType);
		when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString())).thenReturn(partner);
		when(deviceDetailRepository.findUniqueDeviceDetail(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(null);

		DeviceDetail deviceDetail = new DeviceDetail();
		deviceDetail.setId("123456");
		deviceDetail.setDeviceProviderId("123");
		deviceDetail.setDeviceTypeCode("Finger");
		deviceDetail.setDeviceSubTypeCode("Slap");
		deviceDetail.setMake("make");
		deviceDetail.setModel("model");
		deviceDetail.setApprovalStatus("pending_approval");
		deviceDetail.setIsActive(false);
		when(deviceDetailRepository.save(any())).thenReturn(deviceDetail);

		DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
		DeviceDetailSBIPK deviceDetailSBIPK = new DeviceDetailSBIPK();
		deviceDetailSBIPK.setSbiId("sbi123");
		deviceDetailSBIPK.setDeviceDetailId("123456");
		deviceDetailSBI.setId(deviceDetailSBIPK);
		deviceDetailSBI.setPartnerName("abc");
		deviceDetailSBI.setProviderId("123");
		deviceDetailSBI.setIsActive(false);
		when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(anyString(), anyString(), anyString())).thenReturn(deviceDetailSBI);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");
	}

	@Test
	public void addDeviceToSbiExceptionTest2() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeviceDetailDto requestDto = new DeviceDetailDto();
		requestDto.setId(null);
		requestDto.setDeviceTypeCode("Finger");
		requestDto.setDeviceSubTypeCode("Slap");
		requestDto.setMake("make");
		requestDto.setModel("model");
		requestDto.setDeviceProviderId("123");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		RegistrationDeviceSubType registrationDeviceSubType = new RegistrationDeviceSubType();
		registrationDeviceSubType.setCode("Slap");
		registrationDeviceSubType.setDeviceTypeCode("Finger");
		when(registrationDeviceSubTypeRepository.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString(), anyString())). thenReturn(registrationDeviceSubType);
		when(partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString())).thenReturn(null);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");

		when(registrationDeviceSubTypeRepository.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(anyString(), anyString())). thenReturn(null);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");
	}

	@Test
	public void addDeviceToSbiExceptionTest3() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeviceDetailDto requestDto = new DeviceDetailDto();
		requestDto.setId(null);
		requestDto.setDeviceTypeCode("Finger");
		requestDto.setDeviceSubTypeCode("Slap");
		requestDto.setMake("make");
		requestDto.setModel("model");
		requestDto.setDeviceProviderId("345");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");

		partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");
	}

	@Test(expected = Exception.class)
	public void addDeviceToSbiExceptionTest4() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeviceDetailDto requestDto = new DeviceDetailDto();
		requestDto.setId("123");
		requestDto.setDeviceTypeCode("Finger");
		requestDto.setDeviceSubTypeCode("Slap");
		requestDto.setMake("make");
		requestDto.setModel("model");
		requestDto.setDeviceProviderId("345");

		Mockito.when(partnerRepository.findByUserId(Mockito.anyString()))
				.thenThrow(new Exception("Simulated SQL exception") {});
		secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123");
	}

	@Test
	public void deleteDeviceDetailTest() throws Exception {
		String deviceDetailId = "12345";
		DeviceDetail deviceDetail = new DeviceDetail();
		when(deviceDetailRepository.findById(deviceDetailId)).thenReturn(Optional.of(deviceDetail));
		when(deviceDetailSbiRepository.findByDeviceDetailId(deviceDetailId)).thenReturn(Collections.emptyList());
		doNothing().when(deviceDetailRepository).deleteById(deviceDetailId);

		ReflectionTestUtils.invokeMethod(secureBiometricInterfaceService, "deleteDeviceDetail", deviceDetailId);

		verify(deviceDetailRepository).findById(deviceDetailId);
		verify(deviceDetailSbiRepository).findByDeviceDetailId(deviceDetailId);
		verify(deviceDetailRepository).deleteById(deviceDetailId);
	}

	@Test
	public void deactivateSbiTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Device_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setId("12345");
		sbi.setActive(true);
		sbi.setApprovalStatus("approved");
		sbi.setSwVersion("1.0.0");
		sbi.setProviderId("123");
		when(sbiRepository.findById(anyString())).thenReturn(Optional.of(sbi));
		List<DeviceDetail> deviceDetailList = new ArrayList<>();
		DeviceDetail deviceDetail = new DeviceDetail();
		deviceDetail.setId("23456");
		deviceDetail.setDeviceProviderId("123");
		deviceDetail.setApprovalStatus("approved");
		deviceDetail.setIsActive(true);
		deviceDetailList.add(deviceDetail);
		when(deviceDetailRepository.findApprovedDevicesBySbiId(anyString())).thenReturn(deviceDetailList);
		when(deviceDetailRepository.findPendingApprovalDevicesBySbiId(anyString())).thenReturn(deviceDetailList);
		when(deviceDetailRepository.save(any())).thenReturn(deviceDetail);
		when(sbiRepository.save(any())).thenReturn(sbi);
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi("12345", requestDto);

		Assert.assertNotNull(response);
	}

	@Test
	public void deactivateSbiTest_InvalidSbiIdException() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi(null, requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateSbiTest_InvalidStatusException() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("jkjkj");

		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi(null, requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateSbiTest_Exception() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(sbiRepository.findById(any())).thenReturn(Optional.of(new SecureBiometricInterface()));
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi("123", requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateSbiTest_UserIdNotExistsException() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi("23456", requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateSbiTest_SbiNotApprovedException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Device_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setId("12345");
		sbi.setActive(true);
		sbi.setApprovalStatus("rejected");
		sbi.setSwVersion("1.0.0");
		sbi.setProviderId("123");
		when(sbiRepository.findById(anyString())).thenReturn(Optional.of(sbi));
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi("12345", requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateSbiTest_SbiAlreadyDeactivatedException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Device_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setId("12345");
		sbi.setActive(false);
		sbi.setApprovalStatus("approved");
		sbi.setSwVersion("1.0.0");
		sbi.setProviderId("123");
		when(sbiRepository.findById(anyString())).thenReturn(Optional.of(sbi));
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi("12345", requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateSbiTest_SbiNotExistsException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Device_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(sbiRepository.findById(anyString())).thenReturn(Optional.empty());
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi("12345", requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateSbiTest_sbiProviderNotBelongsToUserException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Device_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setUserId("123");
		partner.setId("123");
		partner.setPartnerTypeCode("Device_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setId("12345");
		sbi.setActive(true);
		sbi.setApprovalStatus("approved");
		sbi.setSwVersion("1.0.0");
		sbi.setProviderId("987");
		when(sbiRepository.findById(anyString())).thenReturn(Optional.of(sbi));
		ResponseWrapperV2<SbiDetailsResponseDto> response = secureBiometricInterfaceService.deactivateSbi("12345", requestDto);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void getAllSbiDetailsTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Device_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPolicyGroupId("policyGroup123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("policyGroup123");
		policyGroup.setName("policyGrp");
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);

		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		SbiFilterDto filterDto = new SbiFilterDto();
		filterDto.setPartnerId("mosip123");
		filterDto.setOrgName("abc");
		filterDto.setSbiVersion("1.0.0");

		SbiSummaryEntity entity = new SbiSummaryEntity();
		entity.setSbiId("1");
		Pageable pageable = PageRequest.of(0, 10);
		Page<SbiSummaryEntity> page = new PageImpl<>(List.of(entity), pageable, 1);
		when(sbiSummaryRepository.getSummaryOfSbiDetails(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any())).thenReturn(page);
		secureBiometricInterfaceService.getAllSbiDetails(sortFieldName, sortType, pageNo, pageSize, filterDto);

		secureBiometricInterfaceService.getAllSbiDetails(sortFieldName, sortType, pageNo, pageSize, null);

		partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		secureBiometricInterfaceService.getAllSbiDetails(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getAllDevicesForSbiTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Partner partner = new Partner();
		partner.setId("providerId");
		partner.setPartnerTypeCode("Device_Provider");
		List<Partner> partnerList = Collections.singletonList(partner);
		when(partnerRepository.findByUserId(any())).thenReturn(partnerList);

		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setProviderId("providerId");
		when(sbiRepository.findById(any())).thenReturn(Optional.of(sbi));

		DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
		DeviceDetailSBIPK deviceDetailSBIPK = new DeviceDetailSBIPK();
		deviceDetailSBIPK.setDeviceDetailId("123");
		deviceDetailSBIPK.setSbiId("1213");
		deviceDetailSBI.setId(deviceDetailSBIPK);
		List<DeviceDetailSBI> deviceDetailSBIList = Collections.singletonList(deviceDetailSBI);
		when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiId(anyString(), anyString())).thenReturn(deviceDetailSBIList);

		DeviceDetail deviceDetail = new DeviceDetail();
		deviceDetail.setId("deviceDetailId");
		deviceDetail.setDeviceTypeCode("type");
		deviceDetail.setDeviceSubTypeCode("subtype");
		deviceDetail.setDeviceProviderId("providerId");
		deviceDetail.setMake("make");
		deviceDetail.setModel("model");
		deviceDetail.setApprovalStatus("approved");
		deviceDetail.setIsActive(true);
		deviceDetail.setCrDtimes(LocalDateTime.now());
		when(deviceDetailRepository.findByIdAndDeviceProviderId(any(), any())).thenReturn(Optional.of(deviceDetail));

		ResponseWrapperV2<List<DeviceDto>> responseWrapper = secureBiometricInterfaceService.getAllDevicesForSbi("123");

		assertNotNull(responseWrapper);
	}

	@Test
	public void getAllDevicesForSbiTest_InvalidPartnerType() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Partner partner = new Partner();
		partner.setId("providerId");
		partner.setPartnerTypeCode("Auth_partner");
		List<Partner> partnerList = Collections.singletonList(partner);
		when(partnerRepository.findByUserId(any())).thenReturn(partnerList);

		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setProviderId("providerId");
		when(sbiRepository.findById(any())).thenReturn(Optional.of(sbi));
		ResponseWrapperV2<List<DeviceDto>> responseWrapper = secureBiometricInterfaceService.getAllDevicesForSbi("123");

		assertNotNull(responseWrapper);
	}

	@Test
	public void getAllDevicesForSbi_UserIdNotExistsTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerRepository.findByUserId(any())).thenReturn(Collections.emptyList());

		ResponseWrapperV2<List<DeviceDto>> responseWrapper = secureBiometricInterfaceService.getAllDevicesForSbi("123");

		assertNotNull(responseWrapper);
		assertNotNull(responseWrapper.getErrors());
	}

	@Test
	public void getAllDevicesForSbi_SbiNotExistsTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Partner partner = new Partner();
		partner.setId("providerId");
		List<Partner> partnerList = Collections.singletonList(partner);
		when(partnerRepository.findByUserId(any())).thenReturn(partnerList);

		when(sbiRepository.findById(any())).thenReturn(Optional.empty());

		ResponseWrapperV2<List<DeviceDto>> responseWrapper = secureBiometricInterfaceService.getAllDevicesForSbi("123");
		assertNotNull(responseWrapper);
		assertNotNull(responseWrapper.getErrors());
	}

	@Test
	public void getAllDevicesForSbi_PartnerIdNotAssociatedWithUserTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Partner partner = new Partner();
		partner.setId("wrongProviderId");
		List<Partner> partnerList = Collections.singletonList(partner);
		when(partnerRepository.findByUserId(any())).thenReturn(partnerList);

		SecureBiometricInterface sbi = new SecureBiometricInterface();
		sbi.setProviderId("providerId");
		Optional<SecureBiometricInterface> secureBiometricInterface = Optional.of(sbi);
		when(sbiRepository.findById(any())).thenReturn(secureBiometricInterface);

		ResponseWrapperV2<List<DeviceDto>> responseWrapper = secureBiometricInterfaceService.getAllDevicesForSbi("123");

		assertNotNull(responseWrapper);
		assertNotNull(responseWrapper.getErrors());
	}

	@Test
	public void getAllDevicesForSbi_Exception() {

		Partner partner = new Partner();
		partner.setId("providerId");
		when(partnerRepository.findByUserId(anyString())).thenThrow(new RuntimeException("Test exception"));

		ResponseWrapperV2<List<DeviceDto>> responseWrapper = secureBiometricInterfaceService.getAllDevicesForSbi("123");

		assertNotNull(responseWrapper);
		assertNotNull(responseWrapper.getErrors());
	}

	@Test
	public void testGetSbiDetails() throws NoSuchFieldException, IllegalAccessException {
		Page<SbiSummaryEntity> page = null;
		when(sbiSummaryRepository.getSummaryOfSbiDetailsByStatusAsc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any())).thenReturn(page);
		when(sbiSummaryRepository.getSummaryOfSbiDetailsByStatusDesc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any())).thenReturn(page);
		when(sbiSummaryRepository.getSummaryOfSbiDetailsByExpiryStatusAsc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any())).thenReturn(page);
		when(sbiSummaryRepository.getSummaryOfSbiDetailsByExpiryStatusDesc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any())).thenReturn(page);
		when(sbiSummaryRepository.getSummaryOfSbiDetailsByDevicesCountAsc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any())).thenReturn(page);
		when(sbiSummaryRepository.getSummaryOfSbiDetailsByDevicesCountDesc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any())).thenReturn(page);
		when(sbiSummaryRepository.getSummaryOfSbiDetails(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any())).thenReturn(page);

		// Create and set the mock map
		Map<String, String> mockMap = new HashMap<>();
		mockMap.put("partnerId", "providerId");
		mockMap.put("orgName", "partnerOrgName");
		mockMap.put("partnerType", "p.partnerTypeCode");
		mockMap.put("sbiId", "id");
		mockMap.put("sbiVersion", "swVersion");
		mockMap.put("sbiCreatedDateTime", "swCreateDateTime");
		mockMap.put("sbiExpiryDateTime", "swExpiryDateTime");
		mockMap.put("status", "approvalStatus");
		mockMap.put("createdDateTime", "crDtimes");
		mockMap.put("sbiExpiryStatus", "sbiExpiryStatus");
		mockMap.put("countOfAssociatedDevices", "countOfAssociatedDevices");

		Field field = PartnerHelper.class.getDeclaredField("sbiAliasToColumnMap");
		field.setAccessible(true);
		field.set(partnerHelper, mockMap);

		// Mock sorting request
		Sort mockSort = Sort.by(Sort.Order.asc("crDtimes"));
		when(partnerHelper.getSortingRequest(any(), any())).thenReturn(mockSort);

		// Prepare parameters
		String sortFieldName = "status";
		String sortType = "asc";
		Integer pageNo = 0;
		Integer pageSize = 10;
		SbiFilterDto filterDto = new SbiFilterDto();
		List<String> partnerIdList = new ArrayList<>();
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		// Test different cases
		String[] sortKeys = {
				"status_asc", "status_desc",
				"sbiExpiryStatus_asc", "sbiExpiryStatus_desc",
				"countOfAssociatedDevices_asc", "countOfAssociatedDevices_desc",
				"default"
		};

		for (String sortKey : sortKeys) {
			if (sortKey.equals("default")) {
				sortFieldName = "someField";
				sortType = "asc";
			} else {
				String[] parts = sortKey.split("_");
				sortFieldName = parts[0];
				sortType = parts[1];
			}

			Page result = (Page) ReflectionTestUtils.invokeMethod(
					secureBiometricInterfaceService, "getSbiDetails", sortFieldName, sortType, pageNo, pageSize, filterDto, pageable, partnerIdList, false);

			// Verify repository calls based on sort key
			switch (sortKey) {
				case "status_asc":
					verify(sbiSummaryRepository, times(1))
							.getSummaryOfSbiDetailsByStatusAsc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any());
					break;
				case "status_desc":
					verify(sbiSummaryRepository, times(1))
							.getSummaryOfSbiDetailsByStatusDesc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any());
					break;
				case "sbiExpiryStatus_asc":
					verify(sbiSummaryRepository, times(1))
							.getSummaryOfSbiDetailsByExpiryStatusAsc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any());
					break;
				case "sbiExpiryStatus_desc":
					verify(sbiSummaryRepository, times(1))
							.getSummaryOfSbiDetailsByExpiryStatusDesc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any());
					break;
				case "countOfAssociatedDevices_asc":
					verify(sbiSummaryRepository, times(1))
							.getSummaryOfSbiDetailsByDevicesCountAsc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any());
					break;
				case "countOfAssociatedDevices_desc":
					verify(sbiSummaryRepository, times(1))
							.getSummaryOfSbiDetailsByDevicesCountDesc(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any());
					break;
				default:
					verify(sbiSummaryRepository, times(1))
							.getSummaryOfSbiDetails(any(), any(), any(), any(), any(), any(), anyList(), anyBoolean(), any());
					break;
			}

			// Reset mocks for the next iteration
			reset(sbiSummaryRepository);
		}
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}
}
