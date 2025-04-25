package io.mosip.pms.device.authdevice.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.authdevice.entity.FtmDetailSummaryEntity;
import io.mosip.pms.device.authdevice.repository.FtmDetailsSummaryRepository;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
import io.mosip.pms.device.dto.FtmChipFilterDto;
import io.mosip.pms.device.response.dto.*;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.partner.util.PartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import io.mosip.pms.device.request.dto.DeactivateFtmRequestDto;
import io.mosip.pms.device.request.dto.FtpCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertDownloadRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipDetailDto;
import io.mosip.pms.device.request.dto.FtpChipDetailStatusDto;
import io.mosip.pms.device.request.dto.FtpChipDetailUpdateDto;
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
	PartnerServiceRepository partnerRepository;

	@Autowired
	FtmDetailsSummaryRepository ftmDetailsSummaryRepository;
	
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	PartnerHelper partnerHelper;

	private static final String ERRORS = "errors";

	private static final String ERRORCODE = "errorCode";

	private static final String ERRORMESSAGE = "message";

	public static final String VERSION = "1.0";

	public static final String APPROVED = "approved";

	public static final String DEACTIVATED = "deactivated";

	public static final String PENDING_APPROVAL = "pending_approval";

	public static final String BLANK_STRING = "";
	@Value("${pms.certs.datashare.subscriberId}")
	private String subscriberId;
	
	@Value("${pms.certs.datashare.policyId}")
	private String policyId;

	@Value("${mosip.pms.api.id.deactivate.ftm.patch}")
	private  String patchDeactivateFtm;

	@Value("${mosip.pms.api.id.original.ftm.certificate.get}")
	private  String getOriginalFtmCertificateId;

	@Value("${mosip.pms.api.id.partners.ftm.chip.details.get}")
	private  String getPartnersFtmChipDetailsId;

	@Value("${mosip.pms.api.id.ftm.chip.details.get}")
	private String getFtmChipDetailsId;

	@Value("${mosip.pms.ca.signed.partner.certificate.available}")
	private Boolean isCaSignedPartnerCertificateAvailable;

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
				PartnerUtil.trimAndReplace(chipDetails.getMake()), PartnerUtil.trimAndReplace(chipDetails.getModel()));
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
		if (!chipDetail.get().getApprovalStatus().equals(APPROVED) && !chipDetail.get().getApprovalStatus().equals(CommonConstant.PENDING_CERT_UPLOAD)){
			throw new RequestException(FoundationalTrustProviderErrorMessages.INVALID_FTP_CHIP_STATUS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.INVALID_FTP_CHIP_STATUS.getErrorMessage());
		}
		if (chipDetail.get().getApprovalStatus().equals(APPROVED) && !chipDetail.get().isActive()){
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_DEACTIVATED.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_DEACTIVATED.getErrorMessage());
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
		if(uploadApiResponse == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());			
		}
		if(uploadApiResponse.containsKey(ERRORS) && uploadApiResponse.get(ERRORS) != null) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse.get(ERRORS);
			if(!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			}else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		FtpCertificateResponseDto responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")), FtpCertificateResponseDto.class);
		FTPChipDetail updateObject = chipDetail.get();
		updateObject.setCertificateAlias(responseObject.getCertificateId());
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setApprovalStatus(CommonConstant.PENDING_APPROVAL);
		updateObject.setActive(false);
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
		FtpCertDownloadResponeDto responseObject = null;
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", chipDetail.get().getCertificateAlias());
		Map<String, Object> getApiResponse = restUtil.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);

		if(getApiResponse.get("response") == null && getApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse.get(ERRORS);
			if(!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			}else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if(getApiResponse.get("response") == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());			
		}
		responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")), FtpCertDownloadResponeDto.class);
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

	@Override
	public ResponseWrapperV2<FtmDetailResponseDto> deactivateFtm(String ftmId, DeactivateFtmRequestDto requestDto) {
		ResponseWrapperV2<FtmDetailResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			String status = requestDto.getStatus();
			if (Objects.isNull(status) || status.equals(BLANK_STRING) || !status.equals(PartnerConstants.DEACTIVATE)) {
				LOGGER.info(status + " : is Invalid Input Parameter, it should be (De-Activate)");
				throw new PartnerServiceException(ErrorCode.DEACTIVATE_STATUS_CODE.getErrorCode(),
						ErrorCode.DEACTIVATE_STATUS_CODE.getErrorMessage());
			}
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (partnerList.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			validateFtmId(ftmId);
			Optional<FTPChipDetail> ftmChipDetail = ftpChipDetailRepository.findById(ftmId);
			validateFtmChipDetail(ftmChipDetail);

			FTPChipDetail ftm = ftmChipDetail.get();

			boolean isAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
			if(!isAdmin){
				Partner partnerDetails = getAssociatedPartner(partnerList, ftm, userId);
				partnerHelper.checkIfPartnerIsNotActive(partnerDetails);
			}

			if (!ftm.getApprovalStatus().equals(APPROVED)) {
				LOGGER.error("Unable to deactivate FTM with id {}", ftm.getFtpChipDetailId());
				throw new PartnerServiceException(ErrorCode.FTM_NOT_APPROVED.getErrorCode(),
						ErrorCode.FTM_NOT_APPROVED.getErrorMessage());
			}
			if (ftm.getApprovalStatus().equals(APPROVED) && !ftm.isActive()) {
				LOGGER.error("Unable to deactivate FTM with id {}", ftm.getFtpChipDetailId());
				throw new PartnerServiceException(ErrorCode.FTM_ALREADY_DEACTIVATED.getErrorCode(),
						ErrorCode.FTM_ALREADY_DEACTIVATED.getErrorMessage());
			}
			FtmDetailResponseDto ftmDetailResponseDto = new FtmDetailResponseDto();

			ftm.setActive(false);
			ftm.setUpdDtimes(LocalDateTime.now());
			ftm.setUpdBy(getUserId());
			FTPChipDetail updatedDetail = ftpChipDetailRepository.save(ftm);
			ftmDetailResponseDto.setFtmId(updatedDetail.getFtpChipDetailId());
			ftmDetailResponseDto.setStatus(updatedDetail.getApprovalStatus());
			ftmDetailResponseDto.setActive(updatedDetail.isActive());

			responseWrapper.setResponse(ftmDetailResponseDto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In deactivateFtm method of FTPChipDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In deactivateFtm method of FTPChipDetailServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.DEACTIVATE_FTM_ERROR.getErrorCode();
			String errorMessage = ErrorCode.DEACTIVATE_FTM_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(patchDeactivateFtm);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<FtmCertificateDownloadResponseDto> getFtmCertificateData(String ftmId) {
		ResponseWrapperV2<FtmCertificateDownloadResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (partnerList.isEmpty()) {
				LOGGER.error("sessionId", "idType", "id", "User id does not exists.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			validateFtmId(ftmId);
			Optional<FTPChipDetail> ftmChipDetail = ftpChipDetailRepository.findById(ftmId);
			validateFtmChipDetail(ftmChipDetail);

			FTPChipDetail ftm = ftmChipDetail.get();

			boolean isAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
			if(!isAdmin){
				Partner partnerDetails = getAssociatedPartner(partnerList, ftm, userId);
				partnerHelper.checkIfPartnerIsNotActive(partnerDetails);
			}

			if (!(ftm.getApprovalStatus().equals(PENDING_APPROVAL) || ftm.getApprovalStatus().equals(APPROVED))) {
				LOGGER.error("Unable to download original FTM certificate with id {}", ftm.getFtpChipDetailId());
				throw new PartnerServiceException(ErrorCode.DOWNLOAD_CERTIFICATE_FTM_INVALID_STATUS.getErrorCode(),
						ErrorCode.DOWNLOAD_CERTIFICATE_FTM_INVALID_STATUS.getErrorMessage());
			}
			if (ftm.getApprovalStatus().equals(APPROVED) && !ftm.isActive()) {
				LOGGER.error("Unable to download original FTM certificate with id {}", ftm.getFtpChipDetailId());
				throw new PartnerServiceException(ErrorCode.DOWNLOAD_CERTIFICATE_FTM_DEACTIVATED_ERROR.getErrorCode(),
						ErrorCode.DOWNLOAD_CERTIFICATE_FTM_DEACTIVATED_ERROR.getErrorMessage());
			}

			// Download only if the FTM is approved or pending_approval status.
			FtmCertificateDownloadResponseDto responseObject = null;
			responseObject = partnerHelper.getCertificate(ftm.getCertificateAlias(), "pmp.partner.original.certificate.get.rest.uri", FtmCertificateDownloadResponseDto.class);
			partnerHelper.populateFtmCertificateExpiryState(responseObject);
			responseWrapper.setResponse(responseObject);
		} catch (ApiAccessibleException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getFtmCertificateData method of FTPChipDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getFtmCertificateData method of FTPChipDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getFtmCertificateData method of FTPChipDetailServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.UNABLE_TO_DOWNLOAD_ORIGINAL_FTM_CERTIFICATE.getErrorCode();
			String errorMessage = ErrorCode.UNABLE_TO_DOWNLOAD_ORIGINAL_FTM_CERTIFICATE.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getOriginalFtmCertificateId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<PageResponseV2Dto<FtmDetailSummaryDto>> getPartnersFtmChipDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, FtmChipFilterDto filterDto) {
		ResponseWrapperV2<PageResponseV2Dto<FtmDetailSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			PageResponseV2Dto pageResponseV2Dto = new PageResponseV2Dto();
			// Pagination
			Pageable pageable = PageRequest.of(pageNo, pageSize);

			// Fetch the FTM chip details
			Page<FtmDetailSummaryEntity> page = getFtmChipDetails(sortFieldName, sortType, pageNo, pageSize, filterDto, pageable);
			if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
				List<FtmDetailSummaryDto> ftmDetailSummaryDtoList = MapperUtils.mapAll(page.getContent(), FtmDetailSummaryDto.class);
				pageResponseV2Dto.setPageNo(pageNo);
				pageResponseV2Dto.setPageSize(pageSize);
				pageResponseV2Dto.setTotalResults(page.getTotalElements());
				pageResponseV2Dto.setData(ftmDetailSummaryDtoList);
			}
			responseWrapper.setResponse(pageResponseV2Dto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getPartnersFtmChipDetails method of FTPChipDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getPartnersFtmChipDetails method of FTPChipDetailServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.FTM_CHIP_DETAILS_LIST_FETCH_ERROR.getErrorCode();
			String errorMessage = ErrorCode.FTM_CHIP_DETAILS_LIST_FETCH_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getPartnersFtmChipDetailsId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private Page<FtmDetailSummaryEntity> getFtmChipDetails(String sortFieldName, String sortType, Integer pageNo,
														   Integer pageSize, FtmChipFilterDto filterDto, Pageable pageable) {
		//Sorting
		if (Objects.nonNull(sortFieldName) && Objects.nonNull(sortType)) {
			//sorting handling for the 'status' field
			if (sortFieldName.equals("status") && sortType.equalsIgnoreCase(PartnerConstants.ASC)) {
				return ftmDetailsSummaryRepository.
						getSummaryOfPartnersFtmDetailsByStatusAsc(filterDto.getPartnerId(), filterDto.getOrgName(),
								filterDto.getFtmId(), filterDto.getMake(), filterDto.getModel(), filterDto.getStatus(), pageable);
			} else if (sortFieldName.equals("status") && sortType.equalsIgnoreCase(PartnerConstants.DESC)) {
				return ftmDetailsSummaryRepository.
						getSummaryOfPartnersFtmDetailsByStatusDesc(filterDto.getPartnerId(), filterDto.getOrgName(),
								filterDto.getFtmId(), filterDto.getMake(), filterDto.getModel(), filterDto.getStatus(), pageable);
			}
			//Sorting for other fields
			Sort sort = partnerHelper.getSortingRequest(getSortColumn(partnerHelper.ftmAliasToColumnMap, sortFieldName), sortType);
			pageable = PageRequest.of(pageNo, pageSize, sort);
		}
		//Default
		return ftmDetailsSummaryRepository.getSummaryOfPartnersFtmDetails(filterDto.getPartnerId(), filterDto.getOrgName(),
				filterDto.getFtmId(), filterDto.getMake(), filterDto.getModel(), filterDto.getStatus(), pageable);
	}

	@Override
	public ResponseWrapperV2<List<FtmChipDetailsDto>> ftmChipDetail() {
		ResponseWrapperV2<List<FtmChipDetailsDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			List<FtmChipDetailsDto> ftmChipDetailsDtoList = new ArrayList<>();
			if (!partnerList.isEmpty()) {
				for (Partner partner : partnerList) {
					if (partnerHelper.checkIfPartnerIsFtmPartner(partner)) {
						partnerHelper.validatePartnerId(partner, userId);
						String partnerStatus = getPartnerStatus(partner);
						List<FTPChipDetail> ftpChipDetailList = ftpChipDetailRepository.findByProviderId(partner.getId());
						if(!ftpChipDetailList.isEmpty()) {
							for(FTPChipDetail ftpChipDetail: ftpChipDetailList) {
								FtmChipDetailsDto ftmChipDetailsDto = new FtmChipDetailsDto();
								// Get certificate data if available
								if (ftpChipDetail.getCertificateAlias() != null) {
									ftmChipDetailsDto.setIsCertificateAvailable(true);
									try {
										if (isCaSignedPartnerCertificateAvailable) {
											FtmCertificateDownloadResponseDto responseObject = partnerHelper.getCertificate(ftpChipDetail.getCertificateAlias(), "pmp.partner.original.certificate.get.rest.uri", FtmCertificateDownloadResponseDto.class);
											partnerHelper.populateFtmCertificateExpiryState(responseObject);
											ftmChipDetailsDto.setCertificateUploadDateTime(responseObject.getMosipSignedCertUploadDateTime());
											ftmChipDetailsDto.setCertificateExpiryDateTime(responseObject.getCaSignedCertExpiryDateTime());
											ftmChipDetailsDto.setIsCertificateExpired(responseObject.getIsCaSignedCertificateExpired());
										} else {
											FtpCertDownloadResponeDto responeDto = partnerHelper.getCertificate(ftpChipDetail.getCertificateAlias(), "pmp.partner.certificaticate.get.rest.uri", FtpCertDownloadResponeDto.class);
											X509Certificate cert = MultiPartnerUtil.decodeCertificateData(responeDto.getCertificateData());
											ftmChipDetailsDto.setCertificateUploadDateTime(cert.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
										}
									} catch (ApiAccessibleException ex) {
										if (ex.getErrorCode().equals("KER-PCM-012")) {
											LOGGER.info("Error from keymgr: " + ex.getMessage());
										} else {
											throw new ApiAccessibleException(ex.getErrorCode(), ex.getMessage());
										}
									}
								} else {
									ftmChipDetailsDto.setIsCertificateAvailable(false);
									ftmChipDetailsDto.setIsCertificateExpired(false);
								}
								ftmChipDetailsDto.setFtmId(ftpChipDetail.getFtpChipDetailId());
								ftmChipDetailsDto.setPartnerId(ftpChipDetail.getFtpProviderId());
								ftmChipDetailsDto.setPartnerStatus(partnerStatus);
								ftmChipDetailsDto.setMake(ftpChipDetail.getMake());
								ftmChipDetailsDto.setModel(ftpChipDetail.getModel());
								ftmChipDetailsDto.setStatus(ftpChipDetail.getApprovalStatus());
								ftmChipDetailsDto.setIsActive(ftpChipDetail.isActive());
								ftmChipDetailsDto.setCreatedDateTime(ftpChipDetail.getCrDtimes());
								ftmChipDetailsDtoList.add(ftmChipDetailsDto);
							}
						}
					}
				}
			}
			responseWrapper.setResponse(ftmChipDetailsDtoList);
		} catch (ApiAccessibleException ex) {
			LOGGER.info("sessionId", "idType", "id", "In ftmChipDetails method of FTPChipDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In ftmChipDetail method of FTPChipDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In ftmChipDetail method of FTPChipDetailServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.FTM_CHIP_DETAILS_LIST_FETCH_ERROR.getErrorCode();
			String errorMessage = ErrorCode.FTM_CHIP_DETAILS_LIST_FETCH_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getFtmChipDetailsId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	public String getPartnerStatus(Partner partner) {
		if(partner.getApprovalStatus().equals(APPROVED) && partner.getIsActive()) {
			return APPROVED;
		} else if (partner.getApprovalStatus().equals(APPROVED) && !partner.getIsActive()) {
			return DEACTIVATED;
		} else {
			return partner.getApprovalStatus();
		}
	}

	public String getSortColumn(Map<String, String> aliasToColumnMap, String alias) {
		return aliasToColumnMap.getOrDefault(alias, alias); // Return alias if no match found
	}

	public static void validateFtmId(String ftmId) {
		if (Objects.isNull(ftmId) || ftmId.equals(BLANK_STRING)) {
			LOGGER.info("sessionId", "idType", "id", "FTM id is null or empty.");
			throw new PartnerServiceException(ErrorCode.INVALID_FTM_ID.getErrorCode(),
					ErrorCode.INVALID_FTM_ID.getErrorMessage());
		}
	}

	public static void validateFtmChipDetail(Optional<FTPChipDetail> ftmChipDetail) {
		if (!ftmChipDetail.isPresent()) {
			LOGGER.error("FTM Details not exists");
			throw new PartnerServiceException(ErrorCode.FTM_NOT_EXISTS.getErrorCode(),
					ErrorCode.FTM_NOT_EXISTS.getErrorMessage());
		}
	}

	public Partner getAssociatedPartner(List<Partner> partnerList, FTPChipDetail ftm, String userId) {
		String ftmProviderId = ftm.getFtpProviderId();
		boolean ftmProviderExist = false;
		Partner partnerDetails = null;

		for (Partner partner : partnerList) {
			if (partner.getId().equals(ftmProviderId)) {
				validatePartnerId(partner, userId);
				ftmProviderExist = true;
				partnerDetails = partner;
				break;
			}
		}

		if (!ftmProviderExist) {
			LOGGER.info("sessionId", "idType", "id", "FTM is not associated with user.");
			throw new PartnerServiceException(ErrorCode.FTM_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
					ErrorCode.FTM_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
		}

		return partnerDetails;
	}

	public static void validatePartnerId(Partner partner, String userId) {
		if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
			LOGGER.info("Partner Id is null or empty for user id : " + userId);
			throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
		}
	}

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getUserId() {
		String userId = authUserDetails().getUserId();
		return userId;
	}
}
