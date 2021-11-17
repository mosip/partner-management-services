package io.mosip.pms.test.device.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
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
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.entity.FoundationalTrustProvider;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pms.device.authdevice.repository.FoundationalTrustProviderRepository;
import io.mosip.pms.device.authdevice.service.FtpChipDetailService;
import io.mosip.pms.device.authdevice.service.impl.FTPChipDetailServiceImpl;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.FtpChipCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipDetailDto;
import io.mosip.pms.device.request.dto.FtpChipDetailStatusDto;
import io.mosip.pms.device.request.dto.FtpChipDetailUpdateDto;

import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
@Transactional("authDeviceTransactionManager")
public class FTPChipDetailServiceTest {
	
	@InjectMocks
	private FtpChipDetailService ftpChipDetailService = new FTPChipDetailServiceImpl();
	
	@Mock
	SearchHelper searchHelper;
	
	@Mock
	PartnerServiceRepository partnerServiceRepository;
	
	@Mock
	FoundationalTrustProviderRepository foundationalTrustProviderRepository;
	
	@Mock
	RestUtil restUtil;
	
	@Mock
	FTPChipDetailRepository ftpChipDetailRepository;
	@Mock
	private Environment environment;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Mock
	private AuditUtil audit;
	
	private RequestWrapper<DeviceSearchDto> deviceRequestDto;
	Partner partner=new Partner();
	FtpChipDetailDto ftpChipDetailDto = new FtpChipDetailDto();
	DeviceSearchDto deviceSearchDto = new DeviceSearchDto();
	SearchFilter searchDto = new SearchFilter();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	FoundationalTrustProvider foundationalTrustProvider = new FoundationalTrustProvider();
	
	@Before
	public void setup() {
		ReflectionTestUtils.setField(ftpChipDetailService, "partnerServiceRepository", partnerServiceRepository);
		ReflectionTestUtils.setField(ftpChipDetailService, "foundationalTrustProviderRepository", foundationalTrustProviderRepository);
		ReflectionTestUtils.setField(ftpChipDetailService,"searchHelper",searchHelper);
		ReflectionTestUtils.setField(ftpChipDetailService,"restUtil",restUtil);
		ReflectionTestUtils.setField(ftpChipDetailService,"environment",environment);
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ftpChipDetailDto.setFtpProviderId("1234");
		ftpChipDetailDto.setIsItForRegistrationDevice(true);
		ftpChipDetailDto.setMake("make");
		ftpChipDetailDto.setModel("model");
		foundationalTrustProvider.setActive(true);
		foundationalTrustProvider.setCrBy("admin");
		foundationalTrustProvider.setId("1234");
		foundationalTrustProvider.setCrDtimes(LocalDateTime.now());
		foundationalTrustProvider.setDelDtimes(LocalDateTime.now());
		foundationalTrustProvider.setDeleted(false);
		foundationalTrustProvider.setUpdBy("system");
		foundationalTrustProvider.setUpdDtimes(LocalDateTime.now());
		
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
	
	@Test
	public void createFTPChipDetailtest() {
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		Mockito.when(foundationalTrustProviderRepository.findByIdAndIsActiveTrue(Mockito.anyString())).thenReturn(foundationalTrustProvider);
		ftpChipDetailService.createFtpChipDetails(ftpChipDetailDto);
	}
	
	@Test(expected = RequestException.class)
	public void createFTPChipDetailtest01() {
		Mockito.when(foundationalTrustProviderRepository.findByIdAndIsActiveTrue(Mockito.anyString())).thenReturn(foundationalTrustProvider);
		ftpChipDetailService.createFtpChipDetails(ftpChipDetailDto);
	}	
	
	@Test 
	public void updateFtpChipDetailsTest() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		FoundationalTrustProvider foundationalTrustProvider = new FoundationalTrustProvider();
		Optional<FoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		ftpChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test(expected = RequestException.class) 
	public void updateFtpChipDetailsTest01() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		FoundationalTrustProvider foundationalTrustProvider = new FoundationalTrustProvider();
		Optional<FoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		ftpChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test (expected = RequestException.class)
	public void updateFtpChipDetailsTest02() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		FoundationalTrustProvider foundationalTrustProvider = new FoundationalTrustProvider();
		Optional<FoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		ftpChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test (expected = RequestException.class)
	public void updateFtpChipDetailsTest03() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		FTPChipDetail newftp = new FTPChipDetail();
		newftp.setFtpChipDetailId("123");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		FoundationalTrustProvider foundationalTrustProvider = new FoundationalTrustProvider();
		Optional<FoundationalTrustProvider> opt_foundational = Optional.of(foundationalTrustProvider);
		Mockito.when(foundationalTrustProviderRepository.findById(Mockito.anyString())).thenReturn(opt_foundational);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(newftp);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		ftpChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test
	public void updateFtpChipDetailStatusTest() {
		FtpChipDetailStatusDto request = createUpdateStatusRequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		ftpChipDetailService.updateFtpChipDetailStatus(request);
	}
	
	@Test(expected = RequestException.class)
	public void updateFtpChipDetailStatusTest01() {
		FtpChipDetailStatusDto request = createUpdateStatusRequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetailService.updateFtpChipDetailStatus(request);
	}
	
	@Test
	public void getFTPChipDetailsTest() {
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("1234");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		ftpChipDetailService.getFtpChipDeatils("1234");
	}
	
	@Test
	public void getFTPChipDetailsTest_01() {
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("1234");
		ftpChipDetailService.getFtpChipDeatils("1234");
	}
	
	@Test
	public void searchFTPChipDetailsTest() throws Exception {
		objectMapper.writeValueAsString(deviceRequestDto);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("1234");
		ftpChipDetail.setFtpProviderId("1234");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(ftpChipDetail))).when(searchHelper).search(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.anyString());
		ftpChipDetailService.searchFTPChipDetails(FTPChipDetail.class, deviceSearchDto);	
	}
	
	@Ignore
	@Test
	public void uploadPartnerCertificateTest() throws Exception {
		FtpChipCertificateRequestDto request = createUploadrequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetail.setFtpProviderId("12345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Map<String, Object> uploadApiResponse = new HashMap<String, Object>();
		uploadApiResponse.put("response", 0);
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class)).thenReturn(uploadApiResponse);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		ftpChipDetailService.uploadPartnerCertificate(request);	
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
}
