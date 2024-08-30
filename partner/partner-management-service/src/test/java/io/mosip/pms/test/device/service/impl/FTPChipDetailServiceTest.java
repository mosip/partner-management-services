package io.mosip.pms.test.device.service.impl;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.request.dto.ErrorResponse;
import io.mosip.pms.device.response.dto.FtpCertDownloadResponeDto;
import io.mosip.pms.partner.dto.DataShareDto;
import io.mosip.pms.partner.dto.DataShareResponseDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
	private FTPChipDetailServiceImpl fTPChipDetailServiceImpl;

	@MockBean
	private WebSubPublisher webSubPublisher;

	@MockBean
	private AuditUtil auditUtil;
	
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
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificate.upload.rest.uri"), null, "", "",
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
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificate.upload.rest.uri"), null, "", "",
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
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificate.upload.rest.uri"), null, "", "",
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
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificate.upload.rest.uri"), null, "", "",
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
		Mockito.when(restUtil.postApi(environment.getProperty("pmp.partner.certificate.upload.rest.uri"), null, "", "",
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

	@Test (expected = RequestException.class)
	public void testGetDataShareurl1() {
		when(environment.getProperty((String) any())).thenReturn("Property");
		when(restUtil.postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any()))
				.thenThrow(new RequestException("An error occurred", "An error occurred"));
		ReflectionTestUtils.invokeMethod(ftpChipDetailService,"getDataShareurl","https://pmp.partner.testgetdata.share.url");
		verify(environment).getProperty((String) any());
		verify(restUtil).postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
	}

	@Test
	public void testGetDataShareurl2() {
		when(environment.getProperty((String) any())).thenReturn("Property");

		DataShareDto dataShareDto = new DataShareDto();
		dataShareDto.setPolicyId("policyId");
		dataShareDto.setSignature("certsChain");
		dataShareDto.setSubscriberId("42");
		dataShareDto.setTransactionsAllowed(7);
		dataShareDto.setUrl("https://pmp.partner.testgetdata.share.url");
		dataShareDto.setValidForInMinutes(1);

		DataShareResponseDto dataShareResponseDto = new DataShareResponseDto();
		dataShareResponseDto.setDataShare(dataShareDto);
		dataShareResponseDto.setErrors(new ArrayList<>());
		dataShareResponseDto.setId("id");
		dataShareResponseDto.setResponsetime("certsChain");
		dataShareResponseDto.setVersion("1.0");
		when(restUtil.postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any())).thenReturn(dataShareResponseDto);
		assertEquals("https://pmp.partner.testgetdata.share.url",
				ReflectionTestUtils.invokeMethod(ftpChipDetailService,"getDataShareurl","https://pmp.partner.testgetdata.share.url"));
		verify(environment).getProperty((String) any());
		verify(restUtil).postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetDataShareurl3() {
		when(environment.getProperty((String) any())).thenReturn("Property");
		when(restUtil.postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any())).thenReturn(null);
		ReflectionTestUtils.invokeMethod(ftpChipDetailService,"getDataShareurl","https://pmp.partner.testgetdata.share.url");
		verify(environment).getProperty((String) any());
		verify(restUtil).postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetDataShareurl4() {
		when(environment.getProperty((String) any())).thenReturn("");

		DataShareDto dataShareDto = new DataShareDto();
		dataShareDto.setPolicyId("policyId");
		dataShareDto.setSignature("Signature");
		dataShareDto.setSubscriberId("id");
		dataShareDto.setTransactionsAllowed(1);
		dataShareDto.setUrl("https://pmp.partner.testgetdata.share.url");
		dataShareDto.setValidForInMinutes(1);

		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setErrorCode("An error occurred");
		errorResponse.setMessage("Not all who wander are lost");

		ArrayList<ErrorResponse> errorResponseList = new ArrayList<>();
		errorResponseList.add(errorResponse);
		DataShareResponseDto dataShareResponseDto = mock(DataShareResponseDto.class);
		when(dataShareResponseDto.getDataShare()).thenReturn(dataShareDto);
		when(dataShareResponseDto.getErrors()).thenReturn(errorResponseList);
		when(restUtil.postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any())).thenReturn(dataShareResponseDto);
		ReflectionTestUtils.invokeMethod(ftpChipDetailService,"getDataShareurl","https://pmp.partner.testgetdata.share.url");
		verify(environment).getProperty((String) any());
		verify(restUtil).postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
		verify(dataShareResponseDto, atLeast(1)).getErrors();
	}

	@Test (expected = IllegalArgumentException.class)
	public void testGetPartnerCertFromChain() throws Exception {

		ReflectionTestUtils.invokeMethod(ftpChipDetailService, "getPartnerCertFromChain", "Cert Chain");
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetPartnerCertFromChain2() throws Exception {

		ReflectionTestUtils.invokeMethod(ftpChipDetailService, "getPartnerCertFromChain", "PartnerServiceImpl");
	}

	@Test (expected = NullPointerException.class)
	public void testGetPartnerCertFromChain3() throws Exception {

		ReflectionTestUtils.invokeMethod(ftpChipDetailService, "getPartnerCertFromChain", "");
	}

	@Test (expected = RequestException.class)
	public void testUploadCertificate() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());

		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("Approval Status");
		ftpChipDetail.setCertificateAlias("Certificate Alias");
		ftpChipDetail.setCrBy("Cr By");
		ftpChipDetail.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDeleted(true);
		ftpChipDetail.setFtpChipDetailId("ChipId");
		ftpChipDetail.setFtpProviderId("ProviderId");
		ftpChipDetail.setMake("Make");
		ftpChipDetail.setModel("Model");
		ftpChipDetail.setPartnerOrganizationName("Partner Organization Name");
		ftpChipDetail.setUpdBy("Upd By");
		ftpChipDetail.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<FTPChipDetail> ofResult = Optional.of(ftpChipDetail);
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(ofResult);

		Partner partner = new Partner();
		partner.setAdditionalInfo("Additional Info");
		partner.setAddress("abc");
		partner.setApprovalStatus("Approval Status");
		partner.setCertificateAlias("Certificate Alias");
		partner.setContactNo("Contact No");
		partner.setCrBy("Cr By");
		partner.setCrDtimes(mock(Timestamp.class));
		partner.setDelDtimes(mock(Timestamp.class));
		partner.setEmailId("emailId");
		partner.setId("Id");
		partner.setIsActive(true);
		partner.setIsDeleted(false);
		partner.setLangCode("eng");
		partner.setLogoUrl("https://pms.net/partner");
		partner.setName("Name");
		partner.setPartnerPolicies(new ArrayList<>());
		partner.setPartnerPolicyRequests(new ArrayList<>());
		partner.setPartnerTypeCode("Partner Type Code");
		partner.setPolicyGroupId("PolicyGroupId");
		partner.setUpdBy("Upd By");
		partner.setUpdDtimes(mock(Timestamp.class));
		partner.setUserId("UserId");
		when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any()))
				.thenReturn(partner);
		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto());
		verify(auditUtil).auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(),
				(String) any());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(partnerServiceRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any());
	}

	@Test (expected = RequestException.class)
	public void testUploadCertificate2() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(ftpChipDetailRepository.findById((String) any()))
				.thenThrow(new RequestException("An error occurred", "An error occurred"));

		Partner partner = new Partner();
		partner.setAdditionalInfo("Additional Info");
		partner.setAddress("abc");
		partner.setApprovalStatus("Approval Status");
		partner.setCertificateAlias("Certificate Alias");
		partner.setContactNo("Contact No");
		partner.setCrBy("Cr By");
		partner.setCrDtimes(mock(Timestamp.class));
		partner.setDelDtimes(mock(Timestamp.class));
		partner.setEmailId("emailId");
		partner.setId("Id");
		partner.setIsActive(true);
		partner.setIsDeleted(false);
		partner.setLangCode("eng");
		partner.setLogoUrl("https://pms.net/partner");
		partner.setName("Name");
		partner.setPartnerPolicies(new ArrayList<>());
		partner.setPartnerPolicyRequests(new ArrayList<>());
		partner.setPartnerTypeCode("Partner Type Code");
		partner.setPolicyGroupId("PolicyGroupId");
		partner.setUpdBy("Upd By");
		partner.setUpdDtimes(mock(Timestamp.class));
		partner.setUserId("UserId");
		when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any()))
				.thenReturn(partner);
		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(partnerServiceRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any());
	}

	@Test (expected = RequestException.class)
	public void testUploadCertificate3() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(Optional.empty());
		FTPChipDetail ftpChipDetail = mock(FTPChipDetail.class);
		when(ftpChipDetail.getFtpProviderId()).thenReturn("Id");
		doNothing().when(ftpChipDetail).setActive(anyBoolean());
		doNothing().when(ftpChipDetail).setApprovalStatus((String) any());
		doNothing().when(ftpChipDetail).setCertificateAlias((String) any());
		doNothing().when(ftpChipDetail).setCrBy((String) any());
		doNothing().when(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDeleted(anyBoolean());
		doNothing().when(ftpChipDetail).setFtpChipDetailId((String) any());
		doNothing().when(ftpChipDetail).setFtpProviderId((String) any());
		doNothing().when(ftpChipDetail).setMake((String) any());
		doNothing().when(ftpChipDetail).setModel((String) any());
		doNothing().when(ftpChipDetail).setPartnerOrganizationName((String) any());
		doNothing().when(ftpChipDetail).setUpdBy((String) any());
		doNothing().when(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("Approval Status");
		ftpChipDetail.setCertificateAlias("Certificate Alias");
		ftpChipDetail.setCrBy("Cr By");
		ftpChipDetail.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDeleted(false);
		ftpChipDetail.setFtpChipDetailId("ChipId");
		ftpChipDetail.setFtpProviderId("ProviderId");
		ftpChipDetail.setMake("Make");
		ftpChipDetail.setModel("Model");
		ftpChipDetail.setPartnerOrganizationName("Partner Organization Name");
		ftpChipDetail.setUpdBy("Upd By");
		ftpChipDetail.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));

		Partner partner = new Partner();
		partner.setAdditionalInfo("Additional Info");
		partner.setAddress("abc");
		partner.setApprovalStatus("Approval Status");
		partner.setCertificateAlias("Certificate Alias");
		partner.setContactNo("9087654321");
		partner.setCrBy("Cr By");
		partner.setCrDtimes(mock(Timestamp.class));
		partner.setDelDtimes(mock(Timestamp.class));
		partner.setEmailId("emailId");
		partner.setId("Id");
		partner.setIsActive(true);
		partner.setIsDeleted(false);
		partner.setLangCode("eng");
		partner.setLogoUrl("https://pms.net/partner");
		partner.setName("Name");
		partner.setPartnerPolicies(new ArrayList<>());
		partner.setPartnerPolicyRequests(new ArrayList<>());
		partner.setPartnerTypeCode("Partner Type Code");
		partner.setPolicyGroupId("PolicyGroupId");
		partner.setUpdBy("Upd By");
		partner.setUpdDtimes(mock(Timestamp.class));
		partner.setUserId("UserId");
		when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any()))
				.thenReturn(partner);
		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto());
		verify(auditUtil).auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(),
				(String) any());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(ftpChipDetail).setActive(anyBoolean());
		verify(ftpChipDetail).setApprovalStatus((String) any());
		verify(ftpChipDetail).setCertificateAlias((String) any());
		verify(ftpChipDetail).setCrBy((String) any());
		verify(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDeleted(anyBoolean());
		verify(ftpChipDetail).setFtpChipDetailId((String) any());
		verify(ftpChipDetail).setFtpProviderId((String) any());
		verify(ftpChipDetail).setMake((String) any());
		verify(ftpChipDetail).setModel((String) any());
		verify(ftpChipDetail).setPartnerOrganizationName((String) any());
		verify(ftpChipDetail).setUpdBy((String) any());
		verify(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		verify(partnerServiceRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any());
	}

	@Test (expected = RequestException.class)
	public void testUploadCertificate4() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty((String) any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = mock(FTPChipDetail.class);
		when(ftpChipDetail.getFtpChipDetailId()).thenReturn("ChipId");
		when(ftpChipDetail.getFtpProviderId()).thenReturn("ProviderId");
		doNothing().when(ftpChipDetail).setActive(anyBoolean());
		doNothing().when(ftpChipDetail).setApprovalStatus((String) any());
		doNothing().when(ftpChipDetail).setCertificateAlias((String) any());
		doNothing().when(ftpChipDetail).setCrBy((String) any());
		doNothing().when(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDeleted(anyBoolean());
		doNothing().when(ftpChipDetail).setFtpChipDetailId((String) any());
		doNothing().when(ftpChipDetail).setFtpProviderId((String) any());
		doNothing().when(ftpChipDetail).setMake((String) any());
		doNothing().when(ftpChipDetail).setModel((String) any());
		doNothing().when(ftpChipDetail).setPartnerOrganizationName((String) any());
		doNothing().when(ftpChipDetail).setUpdBy((String) any());
		doNothing().when(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("Approval Status");
		ftpChipDetail.setCertificateAlias("Certificate Alias");
		ftpChipDetail.setCrBy("Cr By");
		ftpChipDetail.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDeleted(false);
		ftpChipDetail.setFtpChipDetailId("ChipId");
		ftpChipDetail.setFtpProviderId("ProviderId");
		ftpChipDetail.setMake("Make");
		ftpChipDetail.setModel("Model");
		ftpChipDetail.setPartnerOrganizationName("Partner Organization Name");
		ftpChipDetail.setUpdBy("Upd By");
		ftpChipDetail.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<FTPChipDetail> ofResult = Optional.of(ftpChipDetail);
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(ofResult);

		Partner partner = new Partner();
		partner.setAdditionalInfo("Additional Info");
		partner.setAddress("abc");
		partner.setApprovalStatus("Approval Status");
		partner.setCertificateAlias("Certificate Alias");
		partner.setContactNo("9087654321");
		partner.setCrBy("Cr By");
		partner.setCrDtimes(mock(Timestamp.class));
		partner.setDelDtimes(mock(Timestamp.class));
		partner.setEmailId("emailId");
		partner.setId("Id");
		partner.setIsActive(true);
		partner.setIsDeleted(false);
		partner.setLangCode("eng");
		partner.setLogoUrl("https://pms.net/partner");
		partner.setName("Name");
		partner.setPartnerPolicies(new ArrayList<>());
		partner.setPartnerPolicyRequests(new ArrayList<>());
		partner.setPartnerTypeCode("Partner Type Code");
		partner.setPolicyGroupId("PolicyGroupId");
		partner.setUpdBy("Upd By");
		partner.setUpdDtimes(mock(Timestamp.class));
		partner.setUserId("UserId");
		when(partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any()))
				.thenReturn(partner);
		when(restUtil.postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any()))
				.thenThrow(new RequestException("An error occurred", "An error occurred"));
		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto("ProviderId", "ChipId", true, "Certificate Data",
				"Organization Name", "Partner Domain"));
		verify(environment).getProperty((String) any());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(ftpChipDetail).getFtpChipDetailId();
		verify(ftpChipDetail).getFtpProviderId();
		verify(ftpChipDetail).setActive(anyBoolean());
		verify(ftpChipDetail).setApprovalStatus((String) any());
		verify(ftpChipDetail).setCertificateAlias((String) any());
		verify(ftpChipDetail).setCrBy((String) any());
		verify(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDeleted(anyBoolean());
		verify(ftpChipDetail).setFtpChipDetailId((String) any());
		verify(ftpChipDetail).setFtpProviderId((String) any());
		verify(ftpChipDetail).setMake((String) any());
		verify(ftpChipDetail).setModel((String) any());
		verify(ftpChipDetail).setPartnerOrganizationName((String) any());
		verify(ftpChipDetail).setUpdBy((String) any());
		verify(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		verify(partnerServiceRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue((String) any());
		verify(restUtil).postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
	}

	@Test (expected = RequestException.class)
	public void testGetCertificate() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty((String) any())).thenReturn("Property");

		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("Approval Status");
		ftpChipDetail.setCertificateAlias("Certificate Alias");
		ftpChipDetail.setCrBy("Cr By");
		ftpChipDetail.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDeleted(false);
		ftpChipDetail.setFtpChipDetailId("ChipId");
		ftpChipDetail.setFtpProviderId("ProviderId");
		ftpChipDetail.setMake("Make");
		ftpChipDetail.setModel("Model");
		ftpChipDetail.setPartnerOrganizationName("Partner Organization Name");
		ftpChipDetail.setUpdBy("Upd By");
		ftpChipDetail.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<FTPChipDetail> ofResult = Optional.of(ftpChipDetail);
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(ofResult);
		when(restUtil.getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any()))
				.thenThrow(new RequestException("An error occurred", "An error occurred"));
		ftpChipDetailService.getCertificate(new FtpChipCertDownloadRequestDto("ChipId"));
		verify(environment).getProperty((String) any());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(restUtil).getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any());
	}

	@Test (expected = RequestException.class)
	public void testGetCertificate2() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty((String) any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = mock(FTPChipDetail.class);
		when(ftpChipDetail.getCertificateAlias()).thenReturn(null);
		doNothing().when(ftpChipDetail).setActive(anyBoolean());
		doNothing().when(ftpChipDetail).setApprovalStatus((String) any());
		doNothing().when(ftpChipDetail).setCertificateAlias((String) any());
		doNothing().when(ftpChipDetail).setCrBy((String) any());
		doNothing().when(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDeleted(anyBoolean());
		doNothing().when(ftpChipDetail).setFtpChipDetailId((String) any());
		doNothing().when(ftpChipDetail).setFtpProviderId((String) any());
		doNothing().when(ftpChipDetail).setMake((String) any());
		doNothing().when(ftpChipDetail).setModel((String) any());
		doNothing().when(ftpChipDetail).setPartnerOrganizationName((String) any());
		doNothing().when(ftpChipDetail).setUpdBy((String) any());
		doNothing().when(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("Approval Status");
		ftpChipDetail.setCertificateAlias("Certificate Alias");
		ftpChipDetail.setCrBy("Cr By");
		ftpChipDetail.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDeleted(false);
		ftpChipDetail.setFtpChipDetailId("ChipId");
		ftpChipDetail.setFtpProviderId("ProviderId");
		ftpChipDetail.setMake("Make");
		ftpChipDetail.setModel("Model");
		ftpChipDetail.setPartnerOrganizationName("Partner Organization Name");
		ftpChipDetail.setUpdBy("Upd By");
		ftpChipDetail.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<FTPChipDetail> ofResult = Optional.of(ftpChipDetail);
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(ofResult);
		when(restUtil.getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any())).thenReturn("Api");
		ftpChipDetailService.getCertificate(new FtpChipCertDownloadRequestDto("ChipId"));
		verify(auditUtil).auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(),
				(String) any());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(ftpChipDetail).getCertificateAlias();
		verify(ftpChipDetail).setActive(anyBoolean());
		verify(ftpChipDetail).setApprovalStatus((String) any());
		verify(ftpChipDetail).setCertificateAlias((String) any());
		verify(ftpChipDetail).setCrBy((String) any());
		verify(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDeleted(anyBoolean());
		verify(ftpChipDetail).setFtpChipDetailId((String) any());
		verify(ftpChipDetail).setFtpProviderId((String) any());
		verify(ftpChipDetail).setMake((String) any());
		verify(ftpChipDetail).setModel((String) any());
		verify(ftpChipDetail).setPartnerOrganizationName((String) any());
		verify(ftpChipDetail).setUpdBy((String) any());
		verify(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
	}

	@Test (expected = RequestException.class)
	public void testGetCertificate4() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty((String) any())).thenReturn("Property");
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(Optional.empty());
		FTPChipDetail ftpChipDetail = mock(FTPChipDetail.class);
		when(ftpChipDetail.getCertificateAlias()).thenReturn("Certificate Alias");
		doNothing().when(ftpChipDetail).setActive(anyBoolean());
		doNothing().when(ftpChipDetail).setApprovalStatus((String) any());
		doNothing().when(ftpChipDetail).setCertificateAlias((String) any());
		doNothing().when(ftpChipDetail).setCrBy((String) any());
		doNothing().when(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDeleted(anyBoolean());
		doNothing().when(ftpChipDetail).setFtpChipDetailId((String) any());
		doNothing().when(ftpChipDetail).setFtpProviderId((String) any());
		doNothing().when(ftpChipDetail).setMake((String) any());
		doNothing().when(ftpChipDetail).setModel((String) any());
		doNothing().when(ftpChipDetail).setPartnerOrganizationName((String) any());
		doNothing().when(ftpChipDetail).setUpdBy((String) any());
		doNothing().when(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("Approval Status");
		ftpChipDetail.setCertificateAlias("Certificate Alias");
		ftpChipDetail.setCrBy("Cr By");
		ftpChipDetail.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDeleted(false);
		ftpChipDetail.setFtpChipDetailId("ChipId");
		ftpChipDetail.setFtpProviderId("ProviderId");
		ftpChipDetail.setMake("Make");
		ftpChipDetail.setModel("Model");
		ftpChipDetail.setPartnerOrganizationName("Partner Organization Name");
		ftpChipDetail.setUpdBy("Upd By");
		ftpChipDetail.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		when(restUtil.getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any())).thenReturn("Api");
		ftpChipDetailService.getCertificate(new FtpChipCertDownloadRequestDto("ChipId"));
		verify(auditUtil).auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(),
				(String) any());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(ftpChipDetail).setActive(anyBoolean());
		verify(ftpChipDetail).setApprovalStatus((String) any());
		verify(ftpChipDetail).setCertificateAlias((String) any());
		verify(ftpChipDetail).setCrBy((String) any());
		verify(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDeleted(anyBoolean());
		verify(ftpChipDetail).setFtpChipDetailId((String) any());
		verify(ftpChipDetail).setFtpProviderId((String) any());
		verify(ftpChipDetail).setMake((String) any());
		verify(ftpChipDetail).setModel((String) any());
		verify(ftpChipDetail).setPartnerOrganizationName((String) any());
		verify(ftpChipDetail).setUpdBy((String) any());
		verify(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
	}


	@Test (expected = IllegalArgumentException.class)
	@Ignore
	public void testGetCertificate5() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty((String) any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = mock(FTPChipDetail.class);
		when(ftpChipDetail.getCertificateAlias()).thenReturn("Certificate Alias");
		doNothing().when(ftpChipDetail).setActive(anyBoolean());
		doNothing().when(ftpChipDetail).setApprovalStatus((String) any());
		doNothing().when(ftpChipDetail).setCertificateAlias((String) any());
		doNothing().when(ftpChipDetail).setCrBy((String) any());
		doNothing().when(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		doNothing().when(ftpChipDetail).setDeleted(anyBoolean());
		doNothing().when(ftpChipDetail).setFtpChipDetailId((String) any());
		doNothing().when(ftpChipDetail).setFtpProviderId((String) any());
		doNothing().when(ftpChipDetail).setMake((String) any());
		doNothing().when(ftpChipDetail).setModel((String) any());
		doNothing().when(ftpChipDetail).setPartnerOrganizationName((String) any());
		doNothing().when(ftpChipDetail).setUpdBy((String) any());
		doNothing().when(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("Approval Status");
		ftpChipDetail.setCertificateAlias("Certificate Alias");
		ftpChipDetail.setCrBy("Cr By");
		ftpChipDetail.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		ftpChipDetail.setDeleted(false);
		ftpChipDetail.setFtpChipDetailId("ChipId");
		ftpChipDetail.setFtpProviderId("ProviderId");
		ftpChipDetail.setMake("Make");
		ftpChipDetail.setModel("Model");
		ftpChipDetail.setPartnerOrganizationName("Partner Organization Name");
		ftpChipDetail.setUpdBy("Upd By");
		ftpChipDetail.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<FTPChipDetail> ofResult = Optional.of(ftpChipDetail);
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(ofResult);

		FtpCertDownloadResponeDto ftpCertDownloadResponeDto = new FtpCertDownloadResponeDto();
		ftpCertDownloadResponeDto.setCertificateData("Certificate Data");
		ftpCertDownloadResponeDto.setTimestamp(LocalDateTime.of(1, 1, 1, 1, 1));
		when(objectMapper.readValue((String) any(), (Class<FtpCertDownloadResponeDto>) any()))
				.thenReturn(ftpCertDownloadResponeDto);
		when(objectMapper.writeValueAsString((Object) any())).thenReturn("42");
		when(restUtil.getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any()))
				.thenReturn(new HashMap<>());
		assertSame(ftpCertDownloadResponeDto,
				ftpChipDetailService.getCertificate(new FtpChipCertDownloadRequestDto("ChipId")));
		verify(environment).getProperty((String) any());
		verify(ftpChipDetailRepository).findById((String) any());
		verify(ftpChipDetail, atLeast(1)).getCertificateAlias();
		verify(ftpChipDetail).setActive(anyBoolean());
		verify(ftpChipDetail).setApprovalStatus((String) any());
		verify(ftpChipDetail).setCertificateAlias((String) any());
		verify(ftpChipDetail).setCrBy((String) any());
		verify(ftpChipDetail).setCrDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDelDtimes((LocalDateTime) any());
		verify(ftpChipDetail).setDeleted(anyBoolean());
		verify(ftpChipDetail).setFtpChipDetailId((String) any());
		verify(ftpChipDetail).setFtpProviderId((String) any());
		verify(ftpChipDetail).setMake((String) any());
		verify(ftpChipDetail).setModel((String) any());
		verify(ftpChipDetail).setPartnerOrganizationName((String) any());
		verify(ftpChipDetail).setUpdBy((String) any());
		verify(ftpChipDetail).setUpdDtimes((LocalDateTime) any());
		verify(objectMapper).readValue((String) any(), (Class<FtpCertDownloadResponeDto>) any());
		verify(objectMapper).writeValueAsString((Object) any());
		verify(restUtil).getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any());
	}

	@Test (expected = Exception.class)
	public void testNotify() {
		doNothing().when(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
		ReflectionTestUtils.invokeMethod(fTPChipDetailServiceImpl,"notify","Cert Data", "Partner Domain");
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test (expected = Exception.class)
	public void testNotify2() {
		doThrow(new RequestException("An error occurred", "An error occurred")).when(webSubPublisher)
				.notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
		ReflectionTestUtils.invokeMethod(fTPChipDetailServiceImpl,"notify","Cert Data", "Partner Domain");
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}
}
