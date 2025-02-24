package io.mosip.pms.device.authdevice.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pms.device.authdevice.service.FtpChipDetailService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.FoundationalTrustProviderErrorMessages;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.FtpCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertDownloadRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipDetailDto;
import io.mosip.pms.device.request.dto.FtpChipDetailStatusDto;
import io.mosip.pms.device.request.dto.FtpChipDetailUpdateDto;
import io.mosip.pms.device.response.dto.FTPSearchResponseDto;
import io.mosip.pms.device.response.dto.FtpCertDownloadResponeDto;
import io.mosip.pms.device.response.dto.FtpCertificateResponseDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.device.util.DeviceUtil;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.DataShareResponseDto;
import io.mosip.pms.partner.exception.PartnerServiceException;


@Component
@Transactional
public class FTPChipDetailServiceImpl implements FtpChipDetailService {
	
	private static final Logger LOGGER = PMSLogger.getLogger(FTPChipDetailServiceImpl.class);
	
	private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
	
	private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    
	private final static String LINE_SEPARATOR = "\n";
	
	@Autowired
	SearchHelper searchHelper;
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired
	RestUtil restUtil;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	FTPChipDetailRepository ftpChipDetailRepository;
	
	@Autowired
	PartnerServiceRepository partnerServiceRepository;
	
	@Autowired
	private ObjectMapper mapper;

	private static final String ERRORS = "errors";

	private static final String ERRORCODE = "errorCode";

	private static final String ERRORMESSAGE = "message";
	
	@Value("${pms.certs.datashare.subscriberId}")
	private String subscriberId;
	
	@Value("${pms.certs.datashare.policyId}")
	private String policyId;
	
	@Autowired
	private WebSubPublisher webSubPublisher;
	
	@Override
	public IdDto createFtpChipDetails(FtpChipDetailDto chipDetails) {
		Partner partnerFromDb = partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(chipDetails.getFtpProviderId());
		if(partnerFromDb == null) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage()),
					"AUT-003", chipDetails.getFtpProviderId(), "partnerId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());

		}
		FTPChipDetail uniqueChipDetail = ftpChipDetailRepository.findByUniqueKey(chipDetails.getFtpProviderId(),
				chipDetails.getMake(), chipDetails.getModel());
		if(uniqueChipDetail != null){
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage()),
					"AUT-003", chipDetails.getFtpProviderId(), "partnerId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage());		
			
		}
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		FTPChipDetail chipDetail = new FTPChipDetail();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {			
			chipDetail.setCrBy(authN.getName());
		}		
		chipDetail.setActive(false);
		chipDetail.setDeleted(false);
		chipDetail.setCrDtimes(LocalDateTime.now());
		chipDetail.setFtpProviderId(chipDetails.getFtpProviderId());
		chipDetail.setFtpChipDetailId(DeviceUtil.generateId());
		chipDetail.setMake(chipDetails.getMake());
		chipDetail.setModel(chipDetails.getModel());
		chipDetail.setPartnerOrganizationName(partnerFromDb.getName());
		chipDetail.setApprovalStatus(CommonConstant.PENDING_CERT_UPLOAD);
		ftpChipDetailRepository.save(chipDetail);
		IdDto response = new IdDto();
		response.setId(chipDetail.getFtpChipDetailId());
		return response;
	}

	@Override
	public IdDto updateFtpChipDetails(FtpChipDetailUpdateDto chipDetails) {
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(chipDetails.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003", chipDetails.getFtpChipDetailId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			
		}
		FTPChipDetail uniqueChipDetail = ftpChipDetailRepository.findByUniqueKey(chipDetail.get().getFtpProviderId(),
				chipDetails.getMake(), chipDetails.getModel());
		if(uniqueChipDetail != null && !chipDetail.get().getFtpChipDetailId().equals(uniqueChipDetail.getFtpChipDetailId())){
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage()),
					"AUT-003", chipDetails.getFtpChipDetailId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage());		
			
		}
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		FTPChipDetail updateObject = chipDetail.get();		
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setUpdDtimes(LocalDateTime.now());
		updateObject.setMake(chipDetails.getMake());
		updateObject.setModel(chipDetails.getModel());
		ftpChipDetailRepository.save(updateObject);
		IdDto response = new IdDto();
		response.setId(updateObject.getFtpChipDetailId());
		return response;
	}

	@Override
	public String updateFtpChipDetailStatus(FtpChipDetailStatusDto chipDetails) {
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(chipDetails.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailStatusDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003", chipDetails.getFtpChipDetailId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());
		}
		if(chipDetail.get().getCertificateAlias() == null && chipDetails.getApprovalStatus()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailStatusDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorMessage()),
					"AUT-003", chipDetails.getFtpChipDetailId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorMessage());
		}
		FTPChipDetail updateObject = chipDetail.get();
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setUpdDtimes(LocalDateTime.now());
		updateObject.setActive(chipDetails.getApprovalStatus());
		updateObject.setApprovalStatus(
				chipDetails.getApprovalStatus() == true ? CommonConstant.APPROVED : CommonConstant.REJECTED);
		ftpChipDetailRepository.save(updateObject);
		return "Status updated successfully.";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FtpCertificateResponseDto uploadCertificate(FtpChipCertificateRequestDto ftpChipCertRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Partner partnerFromDb = partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(ftpChipCertRequestDto.getFtpProviderId());
		if(partnerFromDb == null) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage()),
					"AUT-003", ftpChipCertRequestDto.getFtpChipDeatilId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());
		}
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(ftpChipCertRequestDto.getFtpChipDeatilId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003", ftpChipCertRequestDto.getFtpChipDeatilId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			
		}
		if(!chipDetail.get().getFtpProviderId().equals(ftpChipCertRequestDto.getFtpProviderId())) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorMessage()),
					"AUT-003", ftpChipCertRequestDto.getFtpChipDeatilId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorMessage());		
			
		}
		FtpCertificateRequestDto certRequest = new FtpCertificateRequestDto();
		certRequest.setCertificateData(ftpChipCertRequestDto.getCertificateData());
		certRequest.setOrganizationName(ftpChipCertRequestDto.getOrganizationName());
		certRequest.setPartnerDomain(ftpChipCertRequestDto.getPartnerDomain());
		certRequest.setPartnerId(chipDetail.get().getFtpChipDetailId());
		certRequest.setPartnerType(partnerFromDb.getPartnerTypeCode());
		RequestWrapper<FtpCertificateRequestDto> request = new RequestWrapper<>();
		request.setRequest(certRequest);
		Map<String, Object> uploadApiResponse = restUtil.postApi(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class);		
		FtpCertificateResponseDto responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")), FtpCertificateResponseDto.class);
		if(responseObject == null && uploadApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse.get(ERRORS);
			if(!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			}else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if(responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());			
		}

		FTPChipDetail updateObject = chipDetail.get();
		updateObject.setCertificateAlias(responseObject.getCertificateId());
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setApprovalStatus(CommonConstant.PENDING_APPROVAL);
		updateObject.setUpdDtimes(LocalDateTime.now());
		ftpChipDetailRepository.save(updateObject);
		try {
			responseObject.setCertificateId(getPartnerCertFromChain(responseObject.getSignedCertificateData()));
		} catch (Exception ex) {
			LOGGER.error("Error occured while extracting the leaf cert", ex.getMessage());
			throw new PartnerServiceException(ErrorCode.P7B_CERTDATA_ERROR.getErrorCode(),
					ErrorCode.P7B_CERTDATA_ERROR.getErrorMessage());
		}
		notify(getDataShareurl(responseObject.getSignedCertificateData()), ftpChipCertRequestDto.getPartnerDomain());
		return responseObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FtpCertDownloadResponeDto getCertificate(FtpChipCertDownloadRequestDto certDownloadRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(certDownloadRequestDto.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipCertDownloadRequestDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003", certDownloadRequestDto.getFtpChipDetailId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			
		}
		if(chipDetail.get().getCertificateAlias() == null) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipCertDownloadRequestDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorMessage()),
					"AUT-003", certDownloadRequestDto.getFtpChipDetailId(), "ftpChipId");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorMessage());		
	
		}
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", chipDetail.get().getCertificateAlias());
		Map<String, Object> getApiResponse = restUtil.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		FtpCertDownloadResponeDto responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")), FtpCertDownloadResponeDto.class);
		if(responseObject == null && getApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse.get(ERRORS);
			if(!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			}else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if(responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());			
		}

		return responseObject;
	}

	@Override
	public FTPChipDetail getFtpChipDeatils(String ftpChipId) {
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(ftpChipId);
		if(chipDetail.isEmpty()) {
			return null;
		}
		return chipDetail.get();
	}

	@Override
	public <E> PageResponseDto<FTPSearchResponseDto> searchFTPChipDetails(Class<E> entity, DeviceSearchDto dto) {
		List<FTPSearchResponseDto> partners=new ArrayList<>();
		PageResponseDto<FTPSearchResponseDto> pageDto = new PageResponseDto<>();		
		Page<E> page =searchHelper.search(entity, dto, "ftpProviderId");
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			 partners=MapperUtils.mapAll(page.getContent(), FTPSearchResponseDto.class);
		}
		pageDto.setData(partners);
		pageDto.setFromRecord(0);
		pageDto.setToRecord(page.getContent().size());
		pageDto.setTotalRecord(page.getContent().size());
		return pageDto;
	}
	
	/**
	 * Method to extract the leaf certificate from complete chain of a certificate.
	 * @param certChain
	 * @return
	 * @throws Exception
	 */
	private String getPartnerCertFromChain(String certChain) throws Exception {
		byte[] p7bBytes = CryptoUtil.decodeURLSafeBase64(certChain);
		try (ByteArrayInputStream certStream = new ByteArrayInputStream(p7bBytes)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Collection<?> p7bCertList = cf.generateCertificates(certStream);
			List<Certificate> certList = new ArrayList<>();
			p7bCertList.forEach(cert -> {
				certList.add((Certificate) cert);
			});
			Base64.Encoder base64Encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());
			byte[] certificateData = certList.get(0).getEncoded();
			String encodedCertificateData = new String(base64Encoder.encode(certificateData));
			StringBuilder leafSignedCert = new StringBuilder();
			leafSignedCert.append(BEGIN_CERTIFICATE);
			leafSignedCert.append(LINE_SEPARATOR);
			leafSignedCert.append(encodedCertificateData);
			leafSignedCert.append(LINE_SEPARATOR);
			leafSignedCert.append(END_CERTIFICATE);
			return leafSignedCert.toString();
		} catch (CertificateException | IOException exp) {
			LOGGER.error("Error Parsing P7B Certificate data.", exp);
			throw new PartnerServiceException(ErrorCode.P7B_CERTDATA_PARSING_ERROR.getErrorCode(),
					ErrorCode.P7B_CERTDATA_PARSING_ERROR.getErrorMessage());
		}
	}
	
	/**
	 * 
	 * @param certsChain
	 * @return
	 */
	private String getDataShareurl(String certsChain) {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		String fileName = "certsChain";
		map.add("name", fileName);
		map.add("filename", fileName);
		ByteArrayResource contentsAsResource = new ByteArrayResource(certsChain.getBytes()) {
			@Override
			public String getFilename() {
				return fileName;
			}
		};
		map.add("file", contentsAsResource);
		List<String> pathSegments = new ArrayList<>();
		pathSegments.add(policyId);
		pathSegments.add(subscriberId);
		DataShareResponseDto response = restUtil.postApi(
				environment.getProperty("pmp.certificaticate.datashare.rest.uri"), pathSegments, "", "",
				MediaType.MULTIPART_FORM_DATA, map, DataShareResponseDto.class);
		if (response == null) {
			throw new PartnerServiceException(ErrorCode.DATASHARE_RESPONSE_NULL.getErrorCode(),
					ErrorCode.DATASHARE_RESPONSE_NULL.getErrorMessage());
		}
		if ((response.getErrors() != null && response.getErrors().size() > 0)) {
			throw new PartnerServiceException(response.getErrors().get(0).getErrorCode(),
					response.getErrors().get(0).getMessage());
		}
		System.out.println(response.getDataShare().getUrl());
		return response.getDataShare().getUrl();
	}
	
	/**
	 * 
	 * @param certData
	 * @param partnerDomain
	 */
	private void notify(String certData, String partnerDomain) {
		Type type = new Type();
		type.setName("PartnerServiceImpl");
		type.setNamespace("io.mosip.pms.device.authdevice.service.impl.FTPChipDetailServiceImpl");
		Map<String, Object> data = new HashMap<>();
		data.put("certChainDatashareUrl", certData);
		data.put("partnerDomain", partnerDomain);
		webSubPublisher.notify(EventType.CA_CERTIFICATE_UPLOADED, data, type);
	}
}
