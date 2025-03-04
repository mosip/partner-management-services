package io.mosip.pms.test.device.service.impl;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.request.dto.ErrorResponse;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.FtmDetailSummaryEntity;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.FtmDetailsSummaryRepository;
import io.mosip.pms.device.authdevice.service.impl.FTPChipDetailServiceImpl;
import io.mosip.pms.device.dto.FtmChipFilterDto;
import io.mosip.pms.device.response.dto.FtmDetailResponseDto;
import io.mosip.pms.device.response.dto.FtmDetailSummaryDto;
import io.mosip.pms.device.response.dto.FtpCertDownloadResponeDto;
import io.mosip.pms.device.response.dto.FtpCertificateResponseDto;
import io.mosip.pms.partner.dto.DataShareDto;
import io.mosip.pms.partner.dto.DataShareResponseDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
import io.mosip.pms.partner.response.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.partner.util.PartnerHelper;
import org.junit.Assert;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
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
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.PageResponseV2Dto;
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
import io.mosip.pms.device.constant.FoundationalTrustProviderErrorMessages;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.DeactivateFtmRequestDto;
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
	private FTPChipDetailServiceImpl ftpChipDetailService;

	@Mock
	SearchHelper searchHelper;

	@Mock
	PartnerHelper partnerHelper;

	@Mock
	PartnerServiceRepository partnerServiceRepository;

	@Mock
	RestUtil restUtil;

	@Mock
	FTPChipDetailRepository ftpChipDetailRepository;

	@Mock
	PartnerServiceRepository partnerRepository;

	@Mock
	FtmDetailsSummaryRepository ftmDetailsSummaryRepository;

	@Mock
	private Environment environment;

	@Mock
	private ObjectMapper objectMapper;

	@MockBean
	private WebSubPublisher webSubPublisher;

	@MockBean
	private AuditUtil auditUtil;

	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;

	@Mock
	private AuditUtil audit;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

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

	@Test (expected = PartnerServiceException.class)
	public void testUploadCertificate4() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty(any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("approved");
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
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(Optional.of(ftpChipDetail));

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
		Map<String, Object> mockedResponse = new HashMap<>();
		mockedResponse.put("status", "success");
		mockedResponse.put("response", "Certificate uploaded successfully");
		when(restUtil.postApi( any(), any(),any(), any(), any(),
				any(), any()))
				.thenReturn(mockedResponse);
		String mockedJsonString = "{\"certificateId\":\"12345\"}";

		FtpCertificateResponseDto mockedResponseObject = new FtpCertificateResponseDto();
		mockedResponseObject.setCertificateId("12345");

		when(objectMapper.writeValueAsString(any())).thenReturn(mockedJsonString);
		when(objectMapper.readValue(anyString(), eq(FtpCertificateResponseDto.class)))
				.thenReturn(mockedResponseObject);
		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto("ProviderId", "ChipId", true, "Certificate Data",
				"Organization Name", "Partner Domain"));
		verify(environment).getProperty(any());
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
	public void testUploadCertificate_InvalidFtmChipStatus() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty(any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("approved inactive");
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
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(Optional.of(ftpChipDetail));

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
		Map<String, Object> mockedResponse = new HashMap<>();
		mockedResponse.put("status", "success");
		mockedResponse.put("response", "Certificate uploaded successfully");
		when(restUtil.postApi( any(), any(),any(), any(), any(),
				any(), any()))
				.thenReturn(mockedResponse);
		String mockedJsonString = "{\"certificateId\":\"12345\"}";

		FtpCertificateResponseDto mockedResponseObject = new FtpCertificateResponseDto();
		mockedResponseObject.setCertificateId("12345");

		when(objectMapper.writeValueAsString(any())).thenReturn(mockedJsonString);
		when(objectMapper.readValue(anyString(), eq(FtpCertificateResponseDto.class)))
				.thenReturn(mockedResponseObject);
		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto("ProviderId", "ChipId", true, "Certificate Data",
				"Organization Name", "Partner Domain"));
	}

	@Test (expected = RequestException.class)
	public void testUploadCertificate_FtmChipDeactivated() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty(any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setActive(false);
		ftpChipDetail.setApprovalStatus("approved");
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
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(Optional.of(ftpChipDetail));

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
		Map<String, Object> mockedResponse = new HashMap<>();
		mockedResponse.put("status", "success");
		mockedResponse.put("response", "Certificate uploaded successfully");
		when(restUtil.postApi( any(), any(),any(), any(), any(),
				any(), any()))
				.thenReturn(mockedResponse);
		String mockedJsonString = "{\"certificateId\":\"12345\"}";

		FtpCertificateResponseDto mockedResponseObject = new FtpCertificateResponseDto();
		mockedResponseObject.setCertificateId("12345");

		when(objectMapper.writeValueAsString(any())).thenReturn(mockedJsonString);
		when(objectMapper.readValue(anyString(), eq(FtpCertificateResponseDto.class)))
				.thenReturn(mockedResponseObject);
		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto("ProviderId", "ChipId", true, "Certificate Data",
				"Organization Name", "Partner Domain"));
	}

	@Test (expected = ApiAccessibleException.class)
	public void testUploadCertificate_ApiNullResponse() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty(any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("approved");
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
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(Optional.of(ftpChipDetail));

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
		when(restUtil.postApi( any(), any(),any(), any(), any(),
				any(), any()))
				.thenReturn(null);

		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto("ProviderId", "ChipId", true, "Certificate Data",
				"Organization Name", "Partner Domain"));
	}

	@Test (expected = ApiAccessibleException.class)
	public void testUploadCertificate_ApiErrorResponse() throws IOException {
		doNothing().when(auditUtil)
				.auditRequest((String) any(), (String) any(), (String) any(), (String) any(), (String) any(), (String) any());
		when(environment.getProperty(any())).thenReturn("Property");
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setActive(true);
		ftpChipDetail.setApprovalStatus("approved");
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
		when(ftpChipDetailRepository.findById((String) any())).thenReturn(Optional.of(ftpChipDetail));

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

		Map<String, Object> uploadApiResponse = new HashMap<>();
		List<Map<String, Object>> certServiceErrorList = new ArrayList<>();

		Map<String, Object> error = new HashMap<>();
		error.put("errorCode", "MOCK_ERROR_CODE");
		error.put("message", "Mock error message");
		certServiceErrorList.add(error);

		uploadApiResponse.put("errors", certServiceErrorList);
		when(restUtil.postApi( any(), any(),any(), any(), any(),
				any(), any()))
				.thenReturn(uploadApiResponse);

		ftpChipDetailService.uploadCertificate(new FtpChipCertificateRequestDto("ProviderId", "ChipId", true, "Certificate Data",
				"Organization Name", "Partner Domain"));
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
		ReflectionTestUtils.invokeMethod(ftpChipDetailService,"notify","Cert Data", "Partner Domain");
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test (expected = Exception.class)
	public void testNotify2() {
		doThrow(new RequestException("An error occurred", "An error occurred")).when(webSubPublisher)
				.notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
		ReflectionTestUtils.invokeMethod(ftpChipDetailService,"notify","Cert Data", "Partner Domain");
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}

	@Test
	public void deactivateFtmTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("FTM_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("approved");
		ftpChipDetail.setActive(true);
		when(ftpChipDetailRepository.findById(anyString())).thenReturn(Optional.of(ftpChipDetail));
		when(ftpChipDetailRepository.save(any())).thenReturn(ftpChipDetail);
		ResponseWrapperV2<FtmDetailResponseDto> response = ftpChipDetailService.deactivateFtm("23456", requestDto);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getResponse());
	}

	@Test
	public void deactivateFtm_FtmNotApprovedTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("FTM_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("rejected");
		ftpChipDetail.setActive(true);
		when(ftpChipDetailRepository.findById(anyString())).thenReturn(Optional.of(ftpChipDetail));
		when(ftpChipDetailRepository.save(any())).thenReturn(ftpChipDetail);
		ResponseWrapperV2<FtmDetailResponseDto> response = ftpChipDetailService.deactivateFtm("23456", requestDto);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateFtm_FtmDeactivatedTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("FTM_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("approved");
		ftpChipDetail.setActive(false);
		when(ftpChipDetailRepository.findById(anyString())).thenReturn(Optional.of(ftpChipDetail));
		when(ftpChipDetailRepository.save(any())).thenReturn(ftpChipDetail);
		ResponseWrapperV2<FtmDetailResponseDto> response = ftpChipDetailService.deactivateFtm("23456", requestDto);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateFtm_InvalidStatusTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("FTM_Provider")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
		requestDto.setStatus("");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("approved");
		ftpChipDetail.setActive(true);
		when(ftpChipDetailRepository.findById(anyString())).thenReturn(Optional.of(ftpChipDetail));
		when(ftpChipDetailRepository.save(any())).thenReturn(ftpChipDetail);
		ResponseWrapperV2<FtmDetailResponseDto> response = ftpChipDetailService.deactivateFtm("23456", requestDto);
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void deactivateFtmTestException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		ftpChipDetailService.deactivateFtm(null, requestDto);
	}

	@Test
	public void deactivateFtmTestException1() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		ftpChipDetailService.deactivateFtm("23456", requestDto);
	}

	@Test
	public void deactivateFtmTestException2() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
		requestDto.setStatus("De-Activate");

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);


		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		when(ftpChipDetailRepository.findById(anyString())).thenReturn(Optional.of(ftpChipDetail));
		ftpChipDetailService.deactivateFtm("23456", requestDto);
	}

	@Test
	public void getFtmCertificateData_Test() throws Exception{
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("FTM_Provider")
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
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("approved");
		ftpChipDetail.setActive(true);
		ftpChipDetail.setCertificateAlias("xxxyyxxx");
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ftpChipDetail));
		when(partnerHelper.getCertificate(any(),any(),any())).thenReturn(new FtmCertificateDownloadResponseDto());
		OriginalCertDownloadResponseDto originalCertDownloadResponseDto = new OriginalCertDownloadResponseDto();
		originalCertDownloadResponseDto.setIsCaSignedCertificateExpired(false);
		originalCertDownloadResponseDto.setIsMosipSignedCertificateExpired(false);
		originalCertDownloadResponseDto.setCaSignedCertificateData("-----BEGIN CERTIFICATE-----\\n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\\nd+oxv9Lwk4FT0+WtHZAjMtnDf0l19aNdtImywaI=\\n-----END CERTIFICATE-----\\n");
		originalCertDownloadResponseDto.setMosipSignedCertificateData("-----BEGIN CERTIFICATE-----\\nMIIEizCCA3OgAwIBAgIIBkWQXN5LYV0wDQYJKoZIhvcNAQELBQAwdjELMAkGA1UE\\nBhMCSU4xCzAJBgNVBAgMAktBMRIwEAYDVQQHDAlCQU5HQUxPUkUxDTALBgNVBAoM\\nBElJVEIxGjAYBgNVBAsMEU1PU0lQLVRFQ0gtQ0VOVEVSMRswGQYDVQQDDBJ3d3cu\\nbW9zaXAuaW8gKFBNUykwHhcNMjQwOTE4MDgwNjU3WhcNMjUwOTE4MDgwNjU3WjBR\\nMQswCQYDVQQGEwJJTjELMAkGA1UECAwCTUgxCzAJBgNVBAcMAlBOMQwwCgYDVQQK\\nDANBQkMxDDAKBgNVBAsMA0FCQzEMMAoGA1UEAwwDQUJDMIICIjANBgkqhkiG9w0B\\nAQEFAAOCAg8AMIICCgKCAgEA1EGQeI5OMJlEThlZOWRPqTv2EHD/yAFvJaulKXMI\\nKKa3CWESBfl7wBh8odUBahaTdhJelpI5xLSLCVmHYDNGJUK/pT2An/QRUrsG5T3t\\nt4jI3ryCzwTnG6zkaYyrlWCaYXd4DrQ1d24Jh7bEoGhGY1WlatyqURKLI9z51Yn2\\n/6ebyxIvhTgsAwniEE9w4uWUWrcgQQI+uLFkfj3jrvbC9Ek0f9lmm6hQo4fI3trP\\nG2f4YbPuXabGQ+zwYwZE3szCMn7fPgZ+3sz7fnKXmrmlSme94pu8kSbdOQ1jxHuU\\n/W202/VACyFzmWTX6HUcBD0CcUxnyjU8QULEsBh177xdGNCJTRUmxyXn1yeO6L6D\\n1ojjoTwGt+ySogK8em5bCp2HRuCWMiJ0P3KsOBukzA87iZHBdm2wnJi1HON59vi3\\nPoQw04vd1BQmEUSkNmiuKAbeHr9LpHuSRiyaQ+SnUrouZ08L98A4ks7Y06oQSsXa\\n7OcL3U+lTCw+IlWb0ba9K5zDs1uDRP0az0rKnqAONVkXdU8tUXji583NTLfK0nBH\\nbeZ8oB2QWuQmccYZQqnGIKiQQHV9NXPwlPOO4HfdgLxXytOoByFawcFraPo0CbFf\\nWDkSoxco5zee7/12Tar0ecNcnnu0DTFghGcdGe4YBURxmhc7ajlnHM6nvrJMlDKR\\nfo0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUxAYPEcGciAk/\\n5FBXGmZ+z0u9uGIwDgYDVR0PAQH/BAQDAgKEMA0GCSqGSIb3DQEBCwUAA4IBAQAG\\n4TWJEafFhuAnXQXQQcF3bb3KvabpzDXCyaad7qNnojf2/e48SdLnURcHeiYFDr9a\\nrUdrrU6nBLE6NpTYD8WI+GHZ43SPeim0ACwz08+ZIXyvZt95DV1MCMKehd7YQKEQ\\n/AuvgRP/Z9FP2miZx+TH9GEU0KSAYLX8cMDNZhq3pzBihgHlzNeELDZnJ01kBWWJ\\nxVm1u9jwFDwOa11DGZ3zgEmNbh+3HhKf4zFurTFa88NaHsbvHXSadJMDBpuB42xR\\nAG6a2oUb0nNBg0QmxwAbo85JScNdWg6ncykQnROzGqpSICmIlL/E1Gl/Ti1hwrpP\\nu9J0ndrvDLo566k8h0Xr\\n-----END CERTIFICATE-----\\n");
		ResponseWrapperV2<OriginalCertDownloadResponseDto> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setResponse(originalCertDownloadResponseDto);
		ResponseWrapperV2<FtmCertificateDownloadResponseDto> response = ftpChipDetailService.getFtmCertificateData("23456");
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getResponse());
	}

	@Test
	public void getFtmCertificateData_FtmDeactivatedTest() throws Exception{
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("FTM_Provider")
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
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("approved");
		ftpChipDetail.setActive(false);
		ftpChipDetail.setCertificateAlias("xxxyyxxx");
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ftpChipDetail));
		OriginalCertDownloadResponseDto originalCertDownloadResponseDto = new OriginalCertDownloadResponseDto();
		originalCertDownloadResponseDto.setIsCaSignedCertificateExpired(false);
		originalCertDownloadResponseDto.setIsMosipSignedCertificateExpired(false);
		originalCertDownloadResponseDto.setCaSignedCertificateData("-----BEGIN CERTIFICATE-----\\nMIIF6TCCA9GgAwIBAgIBBDANBgkqhkiG9w0BAQsFADBXMQswCQYDVQQGEwJJTjEL\\nMAkGA1UECAwCTUgxCzAJBgNVBAcMAlBOMQ4wDAYDVQQKDAVTVUJDQTEOMAwGA1UE\\nCwwFU1VCQ0ExDjAMBgNVBAMMBVNVQkNBMB4XDTI0MDUwOTA3MjYyNVoXDTI3MDIw\\nMzA3MjYyNVowUTELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAk1IMQswCQYDVQQHDAJQ\\nTjEMMAoGA1UECgwDQUJDMQwwCgYDVQQLDANBQkMxDDAKBgNVBAMMA0FCQzCCAiIw\\nDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBANRBkHiOTjCZRE4ZWTlkT6k79hBw\\n/8gBbyWrpSlzCCimtwlhEgX5e8AYfKHVAWoWk3YSXpaSOcS0iwlZh2AzRiVCv6U9\\ngJ/0EVK7BuU97beIyN68gs8E5xus5GmMq5VgmmF3eA60NXduCYe2xKBoRmNVpWrc\\nqlESiyPc+dWJ9v+nm8sSL4U4LAMJ4hBPcOLllFq3IEECPrixZH494672wvRJNH/Z\\nZpuoUKOHyN7azxtn+GGz7l2mxkPs8GMGRN7MwjJ+3z4Gft7M+35yl5q5pUpnveKb\\nvJEm3TkNY8R7lP1ttNv1QAshc5lk1+h1HAQ9AnFMZ8o1PEFCxLAYde+8XRjQiU0V\\nJscl59cnjui+g9aI46E8BrfskqICvHpuWwqdh0bgljIidD9yrDgbpMwPO4mRwXZt\\nsJyYtRzjefb4tz6EMNOL3dQUJhFEpDZorigG3h6/S6R7kkYsmkPkp1K6LmdPC/fA\\nOJLO2NOqEErF2uznC91PpUwsPiJVm9G2vSucw7Nbg0T9Gs9Kyp6gDjVZF3VPLVF4\\n4ufNzUy3ytJwR23mfKAdkFrkJnHGGUKpxiCokEB1fTVz8JTzjuB33YC8V8rTqAch\\nWsHBa2j6NAmxX1g5EqMXKOc3nu/9dk2q9HnDXJ57tA0xYIRnHRnuGAVEcZoXO2o5\\nZxzOp76yTJQykX6NAgMBAAGjgcUwgcIwCQYDVR0TBAIwADARBglghkgBhvhCAQEE\\nBAMCBaAwMwYJYIZIAYb4QgENBCYWJE9wZW5TU0wgR2VuZXJhdGVkIENsaWVudCBD\\nZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQUxAYPEcGciAk/5FBXGmZ+z0u9uGIwHwYDVR0j\\nBBgwFoAUUvEvmGjR/9h5/+S8PkEHChVY8/gwDgYDVR0PAQH/BAQDAgXgMB0GA1Ud\\nJQQWMBQGCCsGAQUFBwMCBggrBgEFBQcDBDANBgkqhkiG9w0BAQsFAAOCAgEATadk\\nTQgQLO20uZOChKltcTWnI2VZMu6F4TCb/nQ/JKpZ+vD40qzrB5nHN3yy+nb1rUTE\\ngdW20jEups55xHT/vYIIUIuzydOLGAcfoRTjavNBgCxEyE8iiqhC2QdRylPm24AP\\nmVoGthTZpoJoV2K5nbVc5IqHbWRI3v5mEcvczQunWW0/HMpOJLZl2D3Ohptb7q5L\\nceaVuCUaWxGhqFEcWA7NpzZYTE36EBSvjlK277XrjFZltup7K0Z8l9qGw5A/HqEZ\\nUGq15Ak2mgDPW1pvJds9hWcnOpiObfbgtYIGKSKLz1DbQXuwoVneieTyU16ZKwNi\\ni/VpIn7CFL4ew0OXepVy7kbG8OUrHhpycsriQ8iq2RfmcZJv3iS4IsuUnrx05wXL\\nTidX7r8NPMY8BIzeUL54M97AG845KofGtZImKvbC8xxRnAgkW0ZX9+6NLc4ZsTQg\\nfFjnRzNkWZpgfMvtANYruZufUmfvH7vpwKofBGy/Mk91e4CXiGj/CAlBf1U8VKEc\\nbo7u4b2gWagmuAistlRdeLNvU7hKqNFjpf22wX5s96P+PawyS34PDeULKyo4a1ZK\\nhS6rI9mRNb+GmlAlWtOBdsBk8Trqhtj6qdO+QreDYXoN3UY5jikjM8JDuPArupXC\\nd+oxv9Lwk4FT0+WtHZAjMtnDf0l19aNdtImywaI=\\n-----END CERTIFICATE-----\\n");
		originalCertDownloadResponseDto.setMosipSignedCertificateData("-----BEGIN CERTIFICATE-----\\nMIIEizCCA3OgAwIBAgIIBkWQXN5LYV0wDQYJKoZIhvcNAQELBQAwdjELMAkGA1UE\\nBhMCSU4xCzAJBgNVBAgMAktBMRIwEAYDVQQHDAlCQU5HQUxPUkUxDTALBgNVBAoM\\nBElJVEIxGjAYBgNVBAsMEU1PU0lQLVRFQ0gtQ0VOVEVSMRswGQYDVQQDDBJ3d3cu\\nbW9zaXAuaW8gKFBNUykwHhcNMjQwOTE4MDgwNjU3WhcNMjUwOTE4MDgwNjU3WjBR\\nMQswCQYDVQQGEwJJTjELMAkGA1UECAwCTUgxCzAJBgNVBAcMAlBOMQwwCgYDVQQK\\nDANBQkMxDDAKBgNVBAsMA0FCQzEMMAoGA1UEAwwDQUJDMIICIjANBgkqhkiG9w0B\\nAQEFAAOCAg8AMIICCgKCAgEA1EGQeI5OMJlEThlZOWRPqTv2EHD/yAFvJaulKXMI\\nKKa3CWESBfl7wBh8odUBahaTdhJelpI5xLSLCVmHYDNGJUK/pT2An/QRUrsG5T3t\\nt4jI3ryCzwTnG6zkaYyrlWCaYXd4DrQ1d24Jh7bEoGhGY1WlatyqURKLI9z51Yn2\\n/6ebyxIvhTgsAwniEE9w4uWUWrcgQQI+uLFkfj3jrvbC9Ek0f9lmm6hQo4fI3trP\\nG2f4YbPuXabGQ+zwYwZE3szCMn7fPgZ+3sz7fnKXmrmlSme94pu8kSbdOQ1jxHuU\\n/W202/VACyFzmWTX6HUcBD0CcUxnyjU8QULEsBh177xdGNCJTRUmxyXn1yeO6L6D\\n1ojjoTwGt+ySogK8em5bCp2HRuCWMiJ0P3KsOBukzA87iZHBdm2wnJi1HON59vi3\\nPoQw04vd1BQmEUSkNmiuKAbeHr9LpHuSRiyaQ+SnUrouZ08L98A4ks7Y06oQSsXa\\n7OcL3U+lTCw+IlWb0ba9K5zDs1uDRP0az0rKnqAONVkXdU8tUXji583NTLfK0nBH\\nbeZ8oB2QWuQmccYZQqnGIKiQQHV9NXPwlPOO4HfdgLxXytOoByFawcFraPo0CbFf\\nWDkSoxco5zee7/12Tar0ecNcnnu0DTFghGcdGe4YBURxmhc7ajlnHM6nvrJMlDKR\\nfo0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUxAYPEcGciAk/\\n5FBXGmZ+z0u9uGIwDgYDVR0PAQH/BAQDAgKEMA0GCSqGSIb3DQEBCwUAA4IBAQAG\\n4TWJEafFhuAnXQXQQcF3bb3KvabpzDXCyaad7qNnojf2/e48SdLnURcHeiYFDr9a\\nrUdrrU6nBLE6NpTYD8WI+GHZ43SPeim0ACwz08+ZIXyvZt95DV1MCMKehd7YQKEQ\\n/AuvgRP/Z9FP2miZx+TH9GEU0KSAYLX8cMDNZhq3pzBihgHlzNeELDZnJ01kBWWJ\\nxVm1u9jwFDwOa11DGZ3zgEmNbh+3HhKf4zFurTFa88NaHsbvHXSadJMDBpuB42xR\\nAG6a2oUb0nNBg0QmxwAbo85JScNdWg6ncykQnROzGqpSICmIlL/E1Gl/Ti1hwrpP\\nu9J0ndrvDLo566k8h0Xr\\n-----END CERTIFICATE-----\\n");
		ResponseWrapperV2<OriginalCertDownloadResponseDto> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setResponse(originalCertDownloadResponseDto);
		ResponseWrapperV2<FtmCertificateDownloadResponseDto> response = ftpChipDetailService.getFtmCertificateData("23456");
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void getFtmCertificateData_InvalidFtmStatusTest() throws Exception{
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("FTM_Provider")
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
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("xxxxx");
		ftpChipDetail.setActive(false);
		ftpChipDetail.setCertificateAlias("xxxyyxxx");
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ftpChipDetail));
		OriginalCertDownloadResponseDto originalCertDownloadResponseDto = new OriginalCertDownloadResponseDto();
		originalCertDownloadResponseDto.setIsCaSignedCertificateExpired(false);
		originalCertDownloadResponseDto.setIsMosipSignedCertificateExpired(false);
		originalCertDownloadResponseDto.setCaSignedCertificateData("-----BEGIN CERTIFICATE-----\\n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\\nd+oxv9Lwk4FT0+WtHZAjMtnDf0l19aNdtImywaI=\\n-----END CERTIFICATE-----\\n");
		originalCertDownloadResponseDto.setMosipSignedCertificateData("-----BEGIN CERTIFICATE-----\\nMIIEizCCA3OgAwIBAgIIBkWQXN5LYV0wDQYJKoZIhvcNAQELBQAwdjELMAkGA1UE\\nBhMCSU4xCzAJBgNVBAgMAktBMRIwEAYDVQQHDAlCQU5HQUxPUkUxDTALBgNVBAoM\\nBElJVEIxGjAYBgNVBAsMEU1PU0lQLVRFQ0gtQ0VOVEVSMRswGQYDVQQDDBJ3d3cu\\nbW9zaXAuaW8gKFBNUykwHhcNMjQwOTE4MDgwNjU3WhcNMjUwOTE4MDgwNjU3WjBR\\nMQswCQYDVQQGEwJJTjELMAkGA1UECAwCTUgxCzAJBgNVBAcMAlBOMQwwCgYDVQQK\\nDANBQkMxDDAKBgNVBAsMA0FCQzEMMAoGA1UEAwwDQUJDMIICIjANBgkqhkiG9w0B\\nAQEFAAOCAg8AMIICCgKCAgEA1EGQeI5OMJlEThlZOWRPqTv2EHD/yAFvJaulKXMI\\nKKa3CWESBfl7wBh8odUBahaTdhJelpI5xLSLCVmHYDNGJUK/pT2An/QRUrsG5T3t\\nt4jI3ryCzwTnG6zkaYyrlWCaYXd4DrQ1d24Jh7bEoGhGY1WlatyqURKLI9z51Yn2\\n/6ebyxIvhTgsAwniEE9w4uWUWrcgQQI+uLFkfj3jrvbC9Ek0f9lmm6hQo4fI3trP\\nG2f4YbPuXabGQ+zwYwZE3szCMn7fPgZ+3sz7fnKXmrmlSme94pu8kSbdOQ1jxHuU\\n/W202/VACyFzmWTX6HUcBD0CcUxnyjU8QULEsBh177xdGNCJTRUmxyXn1yeO6L6D\\n1ojjoTwGt+ySogK8em5bCp2HRuCWMiJ0P3KsOBukzA87iZHBdm2wnJi1HON59vi3\\nPoQw04vd1BQmEUSkNmiuKAbeHr9LpHuSRiyaQ+SnUrouZ08L98A4ks7Y06oQSsXa\\n7OcL3U+lTCw+IlWb0ba9K5zDs1uDRP0az0rKnqAONVkXdU8tUXji583NTLfK0nBH\\nbeZ8oB2QWuQmccYZQqnGIKiQQHV9NXPwlPOO4HfdgLxXytOoByFawcFraPo0CbFf\\nWDkSoxco5zee7/12Tar0ecNcnnu0DTFghGcdGe4YBURxmhc7ajlnHM6nvrJMlDKR\\nfo0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUxAYPEcGciAk/\\n5FBXGmZ+z0u9uGIwDgYDVR0PAQH/BAQDAgKEMA0GCSqGSIb3DQEBCwUAA4IBAQAG\\n4TWJEafFhuAnXQXQQcF3bb3KvabpzDXCyaad7qNnojf2/e48SdLnURcHeiYFDr9a\\nrUdrrU6nBLE6NpTYD8WI+GHZ43SPeim0ACwz08+ZIXyvZt95DV1MCMKehd7YQKEQ\\n/AuvgRP/Z9FP2miZx+TH9GEU0KSAYLX8cMDNZhq3pzBihgHlzNeELDZnJ01kBWWJ\\nxVm1u9jwFDwOa11DGZ3zgEmNbh+3HhKf4zFurTFa88NaHsbvHXSadJMDBpuB42xR\\nAG6a2oUb0nNBg0QmxwAbo85JScNdWg6ncykQnROzGqpSICmIlL/E1Gl/Ti1hwrpP\\nu9J0ndrvDLo566k8h0Xr\\n-----END CERTIFICATE-----\\n");
		ResponseWrapperV2<OriginalCertDownloadResponseDto> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setResponse(originalCertDownloadResponseDto);
		ResponseWrapperV2<FtmCertificateDownloadResponseDto> response = ftpChipDetailService.getFtmCertificateData("23456");
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void getFtmCertificateData_userIdNotExistsTest() throws Exception{
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerRepository.findByUserId(anyString())).thenReturn(new ArrayList<>());
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("rejected");
		ftpChipDetail.setActive(false);
		ftpChipDetail.setCertificateAlias("xxxyyxxx");
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ftpChipDetail));

		ResponseWrapperV2<FtmCertificateDownloadResponseDto> response = ftpChipDetailService.getFtmCertificateData("23456");
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getErrors());
	}

	@Test
	public void getFtmCertificateData_Test2() throws Exception{
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setName("abc");
		partner.setIsActive(false);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("23456");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setApprovalStatus("approved");
		ftpChipDetail.setActive(true);
		ftpChipDetail.setCertificateAlias("xxxyyxxx");
		Mockito.when(ftpChipDetailRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ftpChipDetail));

		ftpChipDetailService.getFtmCertificateData("23456");
	}

	@Test
	public void getPartnersFtmChipDetailsTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		FtmChipFilterDto filterDto = new FtmChipFilterDto();
		filterDto.setPartnerId("abc");
		filterDto.setMake("make");
		filterDto.setOrgName("ABC");
		ResponseWrapperV2<PageResponseV2Dto<FtmDetailSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		Page<FtmDetailSummaryEntity> page = null;
		when(ftmDetailsSummaryRepository.getSummaryOfPartnersFtmDetails(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any())).thenReturn(page);
		ftpChipDetailService.getPartnersFtmChipDetails(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getPartnersFtmChipDetailsTestException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ResponseWrapperV2<PageResponseV2Dto<FtmDetailSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		Page<FtmDetailSummaryEntity> page = null;
		when(ftmDetailsSummaryRepository.getSummaryOfPartnersFtmDetails(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any())).thenReturn(page);
		ftpChipDetailService.getPartnersFtmChipDetails(sortFieldName, sortType, pageNo, pageSize, null);
	}

	@Test
	public void ftmChipDetailTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setApprovalStatus("approved");
		partner.setIsActive(true);
		partner.setCertificateAlias("abs");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerHelper.checkIfPartnerIsFtmPartner(any())).thenReturn(true);
		doNothing().when(partnerHelper).validatePartnerId(any(), anyString());

		List<FTPChipDetail> ftpChipDetailList = new ArrayList<>();
		FTPChipDetail ftpChipDetail = new FTPChipDetail();
		ftpChipDetail.setFtpChipDetailId("xxx");
		ftpChipDetail.setFtpProviderId("123");
		ftpChipDetail.setMake("make");
		ftpChipDetail.setModel("model");
		ftpChipDetail.setApprovalStatus("approved");
		ftpChipDetail.setActive(true);
		ftpChipDetail.setCrDtimes(LocalDateTime.now());
		ftpChipDetail.setCertificateAlias("dff");
		ftpChipDetailList.add(ftpChipDetail);
		when(ftpChipDetailRepository.findByProviderId(anyString())).thenReturn(ftpChipDetailList);
		FtmCertificateDownloadResponseDto certificateDownloadResponseDto = new FtmCertificateDownloadResponseDto();
		String certificate = "-----BEGIN CERTIFICATE-----\n" +
				"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\n" +
				"m0UA3Sxxre0vVWYO4GmmZUY=\n" +
				"-----END CERTIFICATE-----";
		certificateDownloadResponseDto.setCaSignedCertificateData(certificate);
		certificateDownloadResponseDto.setMosipSignedCertificateData(certificate);
		certificateDownloadResponseDto.setIsCaSignedCertificateExpired(false);
		certificateDownloadResponseDto.setCaSignedCertUploadDateTime(LocalDateTime.now());
		certificateDownloadResponseDto.setCaSignedCertExpiryDateTime(LocalDateTime.now().plusYears(3));
		certificateDownloadResponseDto.setIsMosipSignedCertificateExpired(false);
		certificateDownloadResponseDto.setMosipSignedCertUploadDateTime(LocalDateTime.now());
		certificateDownloadResponseDto.setMosipSignedCertExpiryDateTime(LocalDateTime.now().plusYears(1));
		when(partnerHelper.getCertificate(anyString(), anyString(), any())).thenReturn(certificateDownloadResponseDto);
		doNothing().when(partnerHelper).populateFtmCertificateExpiryState(certificateDownloadResponseDto);
		ftpChipDetailService.ftmChipDetail();

		ftpChipDetailList = new ArrayList<>();
		ftpChipDetail.setCertificateAlias(null);
		ftpChipDetailList.add(ftpChipDetail);
		when(ftpChipDetailRepository.findByProviderId(anyString())).thenReturn(ftpChipDetailList);
		ftpChipDetailService.ftmChipDetail();

		partnerList = new ArrayList<>();
		partner.setId(null);
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerHelper.checkIfPartnerIsFtmPartner(any())).thenReturn(true);
		doNothing().when(partnerHelper).validatePartnerId(any(), anyString());
		ftpChipDetailService.ftmChipDetail();
	}

	@Test
	public void ftmChipDetailExceptionTest() throws Exception {
		ftpChipDetailService.ftmChipDetail();
	}
}
