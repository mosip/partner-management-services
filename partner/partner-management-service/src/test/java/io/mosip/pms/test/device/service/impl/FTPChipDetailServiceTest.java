package io.mosip.pms.test.device.service.impl;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

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
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pms.device.authdevice.service.FtpChipDetailService;
import io.mosip.pms.device.authdevice.service.impl.FTPChipDetailServiceImpl;
import io.mosip.pms.device.constant.FoundationalTrustProviderErrorMessages;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.FtpChipCertDownloadRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipDetailDto;
import io.mosip.pms.device.request.dto.FtpChipDetailStatusDto;
import io.mosip.pms.device.request.dto.FtpChipDetailUpdateDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
public class FTPChipDetailServiceTest {
	
	@InjectMocks
	private FtpChipDetailService ftpChipDetailService = new FTPChipDetailServiceImpl();
	
	@Mock
	SearchHelper searchHelper;
	
	@Mock
	PartnerServiceRepository partnerServiceRepository;
	
	@Mock
	RestUtil restUtil;
	
	@Mock
	FTPChipDetailRepository ftpChipDetailRepository;
	@Mock
	private Environment environment;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	@Mock
	private AuditUtil audit;
	
	private RequestWrapper<DeviceSearchDto> deviceRequestDto;
	Partner partner=new Partner();
	FtpChipDetailDto ftpChipDetailDto = new FtpChipDetailDto();
	DeviceSearchDto deviceSearchDto = new DeviceSearchDto();
	SearchFilter searchDto = new SearchFilter();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();	
	
	@Before
	public void setup() {
		ReflectionTestUtils.setField(ftpChipDetailService, "partnerServiceRepository", partnerServiceRepository);
		ReflectionTestUtils.setField(ftpChipDetailService,"searchHelper",searchHelper);
		ReflectionTestUtils.setField(ftpChipDetailService,"restUtil",restUtil);
		ReflectionTestUtils.setField(ftpChipDetailService,"environment",environment);
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ftpChipDetailDto.setFtpProviderId("1234");
		ftpChipDetailDto.setMake("make");
		ftpChipDetailDto.setModel("model");
		
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
		ftpChipDetailService.createFtpChipDetails(ftpChipDetailDto);
	}
	
	@Test(expected = RequestException.class)
	public void createFTPChipDetailtest01() {	
		ftpChipDetailService.createFtpChipDetails(ftpChipDetailDto);
	}
	
	@Test
	public void createFTPChipDetailTest02() {
		FTPChipDetail uniqueChipDetail = new FTPChipDetail();
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(uniqueChipDetail);
		try {
			ftpChipDetailService.createFtpChipDetails(ftpChipDetailDto);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode()));
		}
	}
	
	@Test 
	public void updateFtpChipDetailsTest() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);	
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		ftpChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test (expected = RequestException.class)
	public void updateFtpChipDetailsTest02() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");	
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ftpChipDetail);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		ftpChipDetailService.updateFtpChipDetails(chipDetails);
	}
	
	@Test 
	public void updateFtpChipDetailsTest03() {
		FtpChipDetailUpdateDto chipDetails = createUpdateRequest();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("3456");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);	
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		FTPChipDetail ftpChipDetail1 = new FTPChipDetail();
		ftpChipDetail1.setFtpChipDetailId("12345");		
		Mockito.when(ftpChipDetailRepository.findByUniqueKey(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(ftpChipDetail1);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);		
		try {
			ftpChipDetailService.updateFtpChipDetails(chipDetails);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode()));
		}
	}
	
	@Test
	public void updateFtpChipDetailStatusTest() {
		FtpChipDetailStatusDto request = createUpdateStatusRequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		try {
			ftpChipDetailService.updateFtpChipDetailStatus(request);
		}catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode()));
		}
	}
	
	@Test(expected = RequestException.class)
	public void updateFtpChipDetailStatusTest01() {
		FtpChipDetailStatusDto request = createUpdateStatusRequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetailService.updateFtpChipDetailStatus(request);
	}
	
	@Test
	public void updateFtpChipDetailStatusTest02() {
		FtpChipDetailStatusDto request = createUpdateStatusRequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetail.setCertificateAlias("certuploaded");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
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
		Mockito.doReturn(new PageImpl<>(Arrays.asList(ftpChipDetail))).when(searchHelper).search(Mockito.any(),Mockito.any(), Mockito.anyString());
		ftpChipDetailService.searchFTPChipDetails(FTPChipDetail.class, deviceSearchDto);	
	}	
	
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
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(null);
		try {
			ftpChipDetailService.uploadCertificate(request);
		} catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode()));
		}
	}
	
	@Test
	public void uploadPartnerCertificateTest01() throws Exception {
		FtpChipCertificateRequestDto request = createUploadrequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetail.setFtpProviderId("12345");		
		Map<String, Object> uploadApiResponse = new HashMap<String, Object>();
		uploadApiResponse.put("response", 0);
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class)).thenReturn(uploadApiResponse);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		try {
			ftpChipDetailService.uploadCertificate(request);
		} catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode()));
		}
	}
	
	@Test
	public void uploadPartnerCertificateTest02() throws Exception {
		FtpChipCertificateRequestDto request = createUploadrequest(true);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetail.setFtpProviderId("122345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Map<String, Object> uploadApiResponse = new HashMap<String, Object>();
		uploadApiResponse.put("response", 0);
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class)).thenReturn(uploadApiResponse);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		try {
			ftpChipDetailService.uploadCertificate(request);
		} catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode().equals(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorCode()));
		}
	}
	
	@Test
	public void getChipCertificate() throws Exception {
		FtpChipCertDownloadRequestDto request = new FtpChipCertDownloadRequestDto();
		request.setFtpChipDetailId("12345");		
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetail.setFtpProviderId("122345");
		Optional<FTPChipDetail> opt_ftp = Optional.of(ftpChipDetail);
		Map<String, Object> uploadApiResponse = new HashMap<String, Object>();
		uploadApiResponse.put("response", 0);
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class)).thenReturn(uploadApiResponse);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(opt_ftp);
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		try {
			ftpChipDetailService.getCertificate(request);
		} catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode()
					.equals(FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode()));
		}
	}
	
	@Test
	public void getChipCertificate01() throws Exception {
		FtpChipCertDownloadRequestDto request = new FtpChipCertDownloadRequestDto();
		request.setFtpChipDetailId("12345");		
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("12345");
		ftpChipDetail.setFtpProviderId("122345");
		Map<String, Object> uploadApiResponse = new HashMap<String, Object>();
		uploadApiResponse.put("response", 0);
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class)).thenReturn(uploadApiResponse);
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString())).thenReturn(partner);
		try {
			ftpChipDetailService.getCertificate(request);
		} catch (RequestException e) {
			assertTrue(e.getErrors().get(0).getErrorCode()
					.equals(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode()));
		}
	}
	
	private FtpChipDetailUpdateDto createUpdateRequest() {
		FtpChipDetailUpdateDto ChipDetailUpdateDto = new FtpChipDetailUpdateDto();
		ChipDetailUpdateDto.setFtpChipDetailId("12345");
		ChipDetailUpdateDto.setMake("make");
		ChipDetailUpdateDto.setModel("model");
		return ChipDetailUpdateDto;
	}
	
	private FtpChipDetailStatusDto createUpdateStatusRequest(Boolean isItForRegistrationDevice) {
		FtpChipDetailStatusDto ftpChipDetailStatusDto = new FtpChipDetailStatusDto();
		ftpChipDetailStatusDto.setApprovalStatus(true);
		ftpChipDetailStatusDto.setFtpChipDetailId("12345");
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
