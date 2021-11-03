package io.mosip.pms.device.regdevice.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.FoundationalTrustProviderErrorMessages;
import io.mosip.pms.device.regdevice.entity.RegFTPChipDetail;
import io.mosip.pms.device.regdevice.entity.RegFoundationalTrustProvider;
import io.mosip.pms.device.regdevice.repository.RegFTPChipDetailRepository;
import io.mosip.pms.device.regdevice.repository.RegFoundationalTrustProviderRepository;
import io.mosip.pms.device.regdevice.service.RegFTPChipDetailService;
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


@Component
@Transactional
public class RegFTPChipDetailServiceImpl implements RegFTPChipDetailService {
	
	@PersistenceContext(unitName = "regDeviceEntityManagerFactory")
	private EntityManager entityManager;
	
	@Autowired
	SearchHelper searchHelper;
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired
	RestUtil restUtil;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	RegFTPChipDetailRepository ftpChipDetailRepository;
	
	@Autowired
	RegFoundationalTrustProviderRepository foundationalTrustProviderRepository; 
	
	@Autowired
	PartnerServiceRepository partnerServiceRepository; 
	
	@Autowired
	private PageUtils pageUtils;
	
	@Autowired
	private ObjectMapper mapper;

	private static final String ERRORS = "errors";

	private static final String ERRORCODE = "errorCode";

	private static final String ERRORMESSAGE = "message";

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
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());			
		}
		RegFTPChipDetail uniqueChipDetail = ftpChipDetailRepository.findByUniqueKey(chipDetails.getFtpProviderId(),
				chipDetails.getMake(), chipDetails.getModel());
		if(uniqueChipDetail != null ){
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage());
		}
		RegFoundationalTrustProvider ftpProvider = foundationalTrustProviderRepository.findByIdAndIsActiveTrue(chipDetails.getFtpProviderId());
		RegFoundationalTrustProvider entity = new RegFoundationalTrustProvider();
		entity.setActive(true);
		entity.setDeleted(false);
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setCrBy(authN.getName());
		}
		entity.setCrDtimes(LocalDateTime.now());
		entity.setId(partnerFromDb.getId());
		entity.setPartnerOrganizationName(partnerFromDb.getName());
		if(ftpProvider == null) {
			foundationalTrustProviderRepository.save(entity);
		}
		
		RegFTPChipDetail chipDetail = new RegFTPChipDetail();
		chipDetail.setCrBy(authN.getName());
		chipDetail.setActive(false);
		chipDetail.setDeleted(false);
		chipDetail.setCrDtimes(LocalDateTime.now());
		chipDetail.setFtpProviderId(chipDetails.getFtpProviderId());
		chipDetail.setId(DeviceUtil.generateId());
		chipDetail.setMake(chipDetails.getMake());
		chipDetail.setModel(chipDetails.getModel());
		chipDetail.setPartnerOrganizationName(partnerFromDb.getName());
		ftpChipDetailRepository.save(chipDetail);		
		IdDto response = new IdDto();
		response.setId(chipDetail.getId());
		return response;
	}

	@Override
	public IdDto updateFtpChipDetails(FtpChipDetailUpdateDto chipDetails) {
		Partner partnerFromDb = partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(chipDetails.getFtpProviderId());
		if(partnerFromDb == null) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());			
		}
		Optional<RegFTPChipDetail> chipDetail = ftpChipDetailRepository.findById(chipDetails.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			

		}
		RegFTPChipDetail uniqueChipDetail = ftpChipDetailRepository.findByUniqueKey(chipDetails.getFtpProviderId(),
				chipDetails.getMake(), chipDetails.getModel());
		if(uniqueChipDetail != null && !chipDetail.get().getId().equals(uniqueChipDetail.getId())){
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailUpdateDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage());
		}
		Optional<RegFoundationalTrustProvider> ftpProvider = foundationalTrustProviderRepository.findById(partnerFromDb.getId());
		RegFoundationalTrustProvider entity = null;
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();		
		if(ftpProvider.isEmpty()) {
			entity = new RegFoundationalTrustProvider();
			entity.setCrDtimes(LocalDateTime.now());
			entity.setId(partnerFromDb.getId());
			entity.setPartnerOrganizationName(partnerFromDb.getName());
			entity.setActive(true);
			entity.setDeleted(false);
			entity.setCrBy(authN.getName());
		}else {
			entity = ftpProvider.get();
			entity.setPartnerOrganizationName(partnerFromDb.getName());
			entity.setUpdDtimes(LocalDateTime.now());
			entity.setUpdBy(authN.getName());
		}
		foundationalTrustProviderRepository.save(entity);		
		RegFTPChipDetail updateObject = chipDetail.get();		
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setUpdDtimes(LocalDateTime.now());
		updateObject.setMake(chipDetails.getMake());
		updateObject.setModel(chipDetails.getModel());
		updateObject.setFtpProviderId(chipDetails.getFtpProviderId());
		ftpChipDetailRepository.save(updateObject);
		IdDto response = new IdDto();
		response.setId(updateObject.getId());
		return response;
	}

	@Override
	public String updateFtpChipDetailStatus(FtpChipDetailStatusDto chipDetails) {
		Optional<RegFTPChipDetail> chipDetail = ftpChipDetailRepository.findById(chipDetails.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipDetailStatusDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());	
		}
		RegFTPChipDetail updateObject = chipDetail.get();
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setUpdDtimes(LocalDateTime.now());
		updateObject.setActive(chipDetails.getApprovalStatus());
		ftpChipDetailRepository.save(updateObject);
		return "Status updated successfully.";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FtpCertificateResponseDto uploadPartnerCertificate(FtpChipCertificateRequestDto ftpChipCertRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Partner partnerFromDb = partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(ftpChipCertRequestDto.getFtpProviderId());
		if(partnerFromDb == null) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipCertificateRequestDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());
		}
		Optional<RegFTPChipDetail> chipDetail = ftpChipDetailRepository.findById(ftpChipCertRequestDto.getFtpChipDeatilId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipCertificateRequestDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			
		}
		if(!chipDetail.get().getFtpProviderId().equals(ftpChipCertRequestDto.getFtpProviderId())) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipCertificateRequestDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorMessage());
		}
		FtpCertificateRequestDto certRequest = new FtpCertificateRequestDto();
		certRequest.setCertificateData(ftpChipCertRequestDto.getCertificateData());
		certRequest.setOrganizationName(ftpChipCertRequestDto.getOrganizationName());
		certRequest.setPartnerDomain(ftpChipCertRequestDto.getPartnerDomain());
		certRequest.setPartnerId(chipDetail.get().getId());
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
		RegFTPChipDetail updateObject = chipDetail.get();
		updateObject.setCertificateAlias(responseObject.getCertificateId());
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setUpdDtimes(LocalDateTime.now());
		ftpChipDetailRepository.save(updateObject);
		return responseObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FtpCertDownloadResponeDto getPartnerCertificate(FtpChipCertDownloadRequestDto certDownloadRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Optional<RegFTPChipDetail> chipDetail = ftpChipDetailRepository.findById(certDownloadRequestDto.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							DeviceConstant.FAILURE_CREATE, FtpChipCertDownloadRequestDto.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
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
					"AUT-003");
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
	public <E> PageResponseDto<FTPSearchResponseDto> searchFTPChipDetails(Class<E> entity, DeviceSearchDto dto) {
		List<FTPSearchResponseDto> partners=new ArrayList<>();
		PageResponseDto<FTPSearchResponseDto> pageDto = new PageResponseDto<>();		
		Page<E> page =searchHelper.search(entityManager,entity, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			 partners=MapperUtils.mapAll(page.getContent(), FTPSearchResponseDto.class);
			 pageDto = pageUtils.sortPage(partners, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		return pageDto;
	}
}
