package io.mosip.pms.test.regdevice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.device.regdevice.entity.RegFTPChipDetail;
import io.mosip.pms.device.regdevice.entity.RegFoundationalTrustProvider;
import io.mosip.pms.device.regdevice.repository.RegFTPChipDetailRepository;
import io.mosip.pms.device.regdevice.repository.RegFoundationalTrustProviderRepository;
import io.mosip.pms.device.regdevice.service.RegFTPChipDetailService;
import io.mosip.pms.device.regdevice.service.impl.RegFTPChipDetailServiceImpl;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.FtpChipCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipDetailDto;
import io.mosip.pms.device.request.dto.FtpChipDetailStatusDto;
import io.mosip.pms.device.request.dto.FtpChipDetailUpdateDto;
import io.mosip.pms.device.util.AuditUtil;

import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
@Transactional("regDevicePlatformTransactionManager")
public class RegFTPChipDetailServiceTest {
	
	@InjectMocks
	private RegFTPChipDetailService regFTPChipDetailService = new RegFTPChipDetailServiceImpl();
	
	@Mock
	SearchHelper searchHelper;
	
	@Mock
	PageUtils pageUtils;
	
	@Mock
	PartnerServiceRepository partnerServiceRepository;
	
	@Mock
	RegFoundationalTrustProviderRepository foundationalTrustProviderRepository;
	
	@Mock
	RegFTPChipDetailRepository ftpChipDetailRepository;
	
	@Mock
	private AuditUtil audit;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private RequestWrapper<DeviceSearchDto> deviceRequestDto;
	DeviceSearchDto deviceSearchDto = new DeviceSearchDto();
	SearchFilter searchDto = new SearchFilter();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	@Before
	public void setup() {
		ReflectionTestUtils.setField(regFTPChipDetailService, "searchHelper", searchHelper);
		ReflectionTestUtils.setField(regFTPChipDetailService, "pageUtils", pageUtils);
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		searchDto.setColumnName("model");
    	searchDto.setFromValue("");
    	searchDto.setToValue("");
    	searchDto.setType("all");
    	searchDto.setValue("b");
    	List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	searchDtos.add(searchDto);
    	searchSort.setSortField("model");
    	searchSort.setSortType("asc");
    	List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
    	searchDtos1.add(searchSort);
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
		deviceSearchDto.setPurpose(Purpose.REGISTRATION.toString());
		deviceSearchDto.setFilters(searchDtos);
		deviceSearchDto.setPagination(pagination);
		deviceSearchDto.setSort(searchDtos1);
		
	}
	@Ignore
	@Test
	public void createRegFTPChipDetailtest() {
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(createPartnerRequest());
		Mockito.when(foundationalTrustProviderRepository.findByIdAndIsActiveTrue(Mockito.anyString())).thenReturn(foundationalTrustProviderRequest());
		regFTPChipDetailService.createFtpChipDetails(ftpChipDeatilRequest());
	}
	
	@Test(expected = RequestException.class)
	public void createRegFTPChipDetailtest01() {
		Mockito.when(foundationalTrustProviderRepository.findByIdAndIsActiveTrue(Mockito.anyString())).thenReturn(foundationalTrustProviderRequest());
		regFTPChipDetailService.createFtpChipDetails(ftpChipDeatilRequest());
	}
	
	@Ignore
	@Test(expected = RequestException.class)
	public void createRegFTPChipDetailtest02() {
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(createPartnerRequest());
		Mockito.when(foundationalTrustProviderRepository.findByIdAndIsActiveTrue(Mockito.anyString())).thenReturn(foundationalTrustProviderRequest());
		Mockito.doReturn(createPartnerRequest()).when(ftpChipDetailRepository).save(Mockito.any());
		regFTPChipDetailService.createFtpChipDetails(ftpChipDeatilRequest());
	}
	@Ignore
	@Test 
	public void updateRegFtpChipDetailsTest() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		RegFTPChipDetail ftpChipDetail = new RegFTPChipDetail();
		ftpChipDetail.setId("12345");
		Optional<RegFTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		RegFoundationalTrustProvider foundationalTrustProvider = new RegFoundationalTrustProvider();
		Optional<RegFoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(createPartnerRequest());
		regFTPChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test (expected = RequestException.class)
	public void updateRegFtpChipDetailsTest01() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		RegFTPChipDetail ftpChipDetail = new RegFTPChipDetail();
		ftpChipDetail.setId("12345");
		Optional<RegFTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		RegFoundationalTrustProvider foundationalTrustProvider = new RegFoundationalTrustProvider();
		Optional<RegFoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		regFTPChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test (expected = RequestException.class)
	public void updateRegFtpChipDetailsTest02() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		RegFTPChipDetail ftpChipDetail = new RegFTPChipDetail();
		ftpChipDetail.setId("12345");
		RegFoundationalTrustProvider foundationalTrustProvider = new RegFoundationalTrustProvider();
		Optional<RegFoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(createPartnerRequest());
		regFTPChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test (expected = RequestException.class)
	public void updateRegFtpChipDetailsTest03() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		RegFTPChipDetail ftpChipDetail = new RegFTPChipDetail();
		ftpChipDetail.setId("12345");
		RegFTPChipDetail newftp = new RegFTPChipDetail();
		newftp.setId("123");
		Optional<RegFTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		RegFoundationalTrustProvider foundationalTrustProvider = new RegFoundationalTrustProvider();
		Optional<RegFoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(newftp);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(createPartnerRequest());
		regFTPChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	
	@Test
	public void updateRegFtpChipDetailStatusTest() {
		FtpChipDetailStatusDto request = createUpdateStatusRequest(true);
		RegFTPChipDetail ftpChipDetail = new RegFTPChipDetail();
		ftpChipDetail.setId("12345");
		Optional<RegFTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		regFTPChipDetailService.updateFtpChipDetailStatus(request);
	}
	
	@Test (expected = RequestException.class)
	public void updateRegFtpChipDetailStatusTest01() {
		FtpChipDetailStatusDto request = createUpdateStatusRequest(true);
		RegFTPChipDetail ftpChipDetail = new RegFTPChipDetail();
		ftpChipDetail.setId("12345");
		regFTPChipDetailService.updateFtpChipDetailStatus(request);
	}
	
	@Test
	public void searchRegFTPChipDetailsTest() throws Exception {
		objectMapper.writeValueAsString(deviceRequestDto);
		RegFTPChipDetail RegFTPChip = new RegFTPChipDetail();
		RegFTPChip.setFtpProviderId("1234");
		RegFTPChip.setId("1234");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(RegFTPChip))).when(searchHelper).search(Mockito.any(),Mockito.any(),Mockito.any());
		regFTPChipDetailService.searchFTPChipDetails(RegFTPChipDetail.class, deviceSearchDto);
	}

	private FtpChipDetailDto ftpChipDeatilRequest() {
		FtpChipDetailDto ftpChipDetailDto = new FtpChipDetailDto();
		ftpChipDetailDto.setFtpProviderId("1234");
		ftpChipDetailDto.setIsItForRegistrationDevice(true);
		ftpChipDetailDto.setMake("make");
		ftpChipDetailDto.setModel("model");
		return ftpChipDetailDto;
	}

	private RegFoundationalTrustProvider foundationalTrustProviderRequest() {
		RegFoundationalTrustProvider foundationalTrustProvider = new RegFoundationalTrustProvider();
		foundationalTrustProvider.setActive(true);
		foundationalTrustProvider.setCrBy("admin");
		foundationalTrustProvider.setId("1234");
		foundationalTrustProvider.setCrDtimes(LocalDateTime.now());
		foundationalTrustProvider.setDelDtimes(LocalDateTime.now());
		foundationalTrustProvider.setDeleted(false);
		foundationalTrustProvider.setUpdBy("system");
		foundationalTrustProvider.setUpdDtimes(LocalDateTime.now());
		return foundationalTrustProvider;
	}
	
	private FtpChipDetailUpdateDto createUpdateRequest() {
		FtpChipDetailUpdateDto ChipDetailUpdateDto = new FtpChipDetailUpdateDto();
		ChipDetailUpdateDto.setFtpChipDetailId("12345");
		ChipDetailUpdateDto.setFtpProviderId("12345");
		ChipDetailUpdateDto.setIsItForRegistrationDevice(true);
		ChipDetailUpdateDto.setMake("make");
		ChipDetailUpdateDto.setModel("model");
		return ChipDetailUpdateDto;
	}
	
	private FtpChipDetailStatusDto createUpdateStatusRequest(Boolean isItForRegistrationDevice) {
		FtpChipDetailStatusDto ftpChipDetailStatusDto = new FtpChipDetailStatusDto();
		ftpChipDetailStatusDto.setApprovalStatus(true);
		ftpChipDetailStatusDto.setFtpChipDetailId("12345");
		ftpChipDetailStatusDto.setIsItForRegistrationDevice(isItForRegistrationDevice);
		return ftpChipDetailStatusDto;
	}
	
	@SuppressWarnings("unused")
	private FtpChipCertificateRequestDto createUploadrequest(Boolean isItForRegistrationDevice) {
		FtpChipCertificateRequestDto FtpChipCertificateRequestDto = new FtpChipCertificateRequestDto();
		FtpChipCertificateRequestDto.setCertificateData("abcd");
		FtpChipCertificateRequestDto.setFtpChipDeatilId("12345");
		FtpChipCertificateRequestDto.setFtpProviderId("12345");
		FtpChipCertificateRequestDto.setIsItForRegistrationDevice(isItForRegistrationDevice);
		FtpChipCertificateRequestDto.setOrganizationName("abcd");
		FtpChipCertificateRequestDto.setPartnerDomain("doamin");
		return FtpChipCertificateRequestDto;
	}
	
	private Partner createPartnerRequest() {
		Partner part = new Partner();
		part.setName("abcd");
		part.setId("12345");
		return part;
	}
}
