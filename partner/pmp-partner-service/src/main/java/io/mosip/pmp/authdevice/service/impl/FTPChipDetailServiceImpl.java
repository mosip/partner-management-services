package io.mosip.pmp.authdevice.service.impl;

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
import io.mosip.pmp.authdevice.constants.FoundationalTrustProviderErrorMessages;
import io.mosip.pmp.authdevice.dto.DeviceSearchDto;
import io.mosip.pmp.authdevice.dto.FTPChipCertDownloadRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipCertificateRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailStatusDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.FTPSearchResponseDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.entity.FTPChipDetail;
import io.mosip.pmp.authdevice.entity.FoundationalTrustProvider;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pmp.authdevice.repository.FoundationalTrustProviderRepository;
import io.mosip.pmp.authdevice.service.FTPChipDetailService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;
import io.mosip.pmp.common.dto.PageResponseDto;
import io.mosip.pmp.common.helper.SearchHelper;
import io.mosip.pmp.common.util.MapperUtils;
import io.mosip.pmp.partner.constant.ApiAccessibleExceptionConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.dto.PartnerCertDownloadResponeDto;
import io.mosip.pmp.partner.dto.PartnerCertificateRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertificateResponseDto;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.exception.ApiAccessibleException;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.partner.util.PartnerUtil;
import io.mosip.pmp.partner.util.RestUtil;


@Component
@Transactional
public class FTPChipDetailServiceImpl implements FTPChipDetailService {
		
	@PersistenceContext(unitName = "authDeviceEntityManagerFactory")
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
	FTPChipDetailRepository ftpChipDetailRepository;
	
	@Autowired
	FoundationalTrustProviderRepository foundationalTrustProviderRepository; 
	
	@Autowired
	PartnerServiceRepository partnerServiceRepository;
	
	@Autowired
	private ObjectMapper mapper;

	private static final String ERRORS = "errors";

	private static final String ERRORCODE = "errorCode";

	private static final String ERRORMESSAGE = "message";
	
	@Override
	public IdDto createFtpChipDetails(FTPChipDetailDto chipDetails) {
		Partner partnerFromDb = partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(chipDetails.getFtpProviderId());
		if(partnerFromDb == null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());

		}
		FoundationalTrustProvider ftpProvider = foundationalTrustProviderRepository.findByIdAndIsActiveTrue(chipDetails.getFtpProviderId());
		FoundationalTrustProvider entity = new FoundationalTrustProvider();
		entity.setActive(true);
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setCrBy(authN.getName());
		}
		entity.setCrDtimes(LocalDateTime.now());
		entity.setId(partnerFromDb.getId());
		if(ftpProvider == null) {
			foundationalTrustProviderRepository.save(entity);
		}
		
		FTPChipDetail chipDetail = new FTPChipDetail();
		chipDetail.setCrBy(authN.getName());
		chipDetail.setActive(false);
		chipDetail.setCrDtimes(LocalDateTime.now());
		chipDetail.setFtpProviderId(chipDetails.getFtpProviderId());
		chipDetail.setFtpChipDetailId(PartnerUtil.generateId());
		chipDetail.setMake(chipDetails.getMake());
		chipDetail.setModel(chipDetails.getModel());
		chipDetail.setPartnerOrganizationName(partnerFromDb.getName());
		try {
			ftpChipDetailRepository.save(chipDetail);	
		}catch(Exception ex){
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_DETAILS_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_DETAILS_EXISTS.getErrorMessage() + ex.getMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_DETAILS_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_DETAILS_EXISTS.getErrorMessage() + ex.getMessage());	
		}

		IdDto response = new IdDto();
		response.setId(chipDetail.getFtpChipDetailId());
		return response;
	}

	@Override
	public IdDto updateFtpChipDetails(FTPChipDetailUpdateDto chipDetails) {
		Partner partnerFromDb = partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(chipDetails.getFtpProviderId());
		if(partnerFromDb == null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailUpdateDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());
		}
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(chipDetails.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailUpdateDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			
		}
		FTPChipDetail uniqueChipDetail = ftpChipDetailRepository.findByUniqueKey(chipDetails.getFtpProviderId(),
				chipDetails.getMake(), chipDetails.getModel());
		if(uniqueChipDetail != null && !chipDetail.get().getFtpChipDetailId().equals(uniqueChipDetail.getFtpChipDetailId())){
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailUpdateDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_MAKE_MODEL_EXISTS.getErrorMessage());		
			
		}
		Optional<FoundationalTrustProvider> ftpProvider = foundationalTrustProviderRepository.findById(partnerFromDb.getId());
		FoundationalTrustProvider entity = null;
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();		
		if(ftpProvider.isEmpty()) {
			entity = new FoundationalTrustProvider();
			entity.setCrDtimes(LocalDateTime.now());
			entity.setId(partnerFromDb.getId());
			//entity.setPartnerOrganizationName(partnerFromDb.getName());
			entity.setActive(true);
			entity.setCrBy(authN.getName());
		}else {
			entity = ftpProvider.get();
			//entity.setPartnerOrganizationName(partnerFromDb.getName());
			entity.setUpdDtimes(LocalDateTime.now());
			entity.setUpdBy(authN.getName());
		}
		foundationalTrustProviderRepository.save(entity);
		FTPChipDetail updateObject = chipDetail.get();		
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			updateObject.setUpdBy(authN.getName());
		}
		updateObject.setUpdDtimes(LocalDateTime.now());
		updateObject.setMake(chipDetails.getMake());
		updateObject.setModel(chipDetails.getModel());
		updateObject.setFtpProviderId(chipDetails.getFtpProviderId());
		ftpChipDetailRepository.save(updateObject);
		IdDto response = new IdDto();
		response.setId(updateObject.getFtpChipDetailId());
		return response;
	}

	@Override
	public String updateFtpChipDetailStatus(FTPChipDetailStatusDto chipDetails) {
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(chipDetails.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailStatusDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());
		}
		
		FTPChipDetail updateObject = chipDetail.get();
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
	public PartnerCertificateResponseDto uploadPartnerCertificate(FTPChipCertificateRequestDto ftpChipCertRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Partner partnerFromDb = partnerServiceRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(ftpChipCertRequestDto.getFtpProviderId());
		if(partnerFromDb == null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailUpdateDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_PROVIDER_NOT_EXISTS.getErrorMessage());
		}
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(ftpChipCertRequestDto.getFtpChipDeatilId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailUpdateDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			
		}
		if(!chipDetail.get().getFtpProviderId().equals(ftpChipCertRequestDto.getFtpProviderId())) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipDetailUpdateDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED.getErrorMessage());		
			
		}
		PartnerCertificateRequestDto certRequest = new PartnerCertificateRequestDto();
		certRequest.setCertificateData(ftpChipCertRequestDto.getCertificateData());
		certRequest.setOrganizationName(ftpChipCertRequestDto.getOrganizationName());
		certRequest.setPartnerDomain(ftpChipCertRequestDto.getPartnerDomain());
		certRequest.setPartnerId(chipDetail.get().getFtpChipDetailId());
		certRequest.setPartnerType(partnerFromDb.getPartnerTypeCode());
		RequestWrapper<PartnerCertificateRequestDto> request = new RequestWrapper<>();
		request.setRequest(certRequest);
		Map<String, Object> uploadApiResponse = restUtil.postApi(environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class);		
		PartnerCertificateResponseDto responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")), PartnerCertificateResponseDto.class);
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
		updateObject.setUpdDtimes(LocalDateTime.now());
		ftpChipDetailRepository.save(updateObject);
		return responseObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PartnerCertDownloadResponeDto getPartnerCertificate(FTPChipCertDownloadRequestDto certDownloadRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Optional<FTPChipDetail> chipDetail = ftpChipDetailRepository.findById(certDownloadRequestDto.getFtpChipDetailId());
		if(chipDetail.isEmpty()) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipCertDownloadRequestDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CHIP_ID_NOT_EXISTS.getErrorMessage());			
		}
		if(chipDetail.get().getCertificateAlias() == null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, FTPChipCertDownloadRequestDto.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode(),
							FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorMessage()),
					"AUT-003");
			throw new RequestException(FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorCode(),
					FoundationalTrustProviderErrorMessages.FTP_CERT_NOT_UPLOADED.getErrorMessage());		
	
		}
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", chipDetail.get().getCertificateAlias());
		Map<String, Object> getApiResponse = restUtil.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		PartnerCertDownloadResponeDto responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")), PartnerCertDownloadResponeDto.class);
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
		Page<E> page =searchHelper.search(entityManager,entity, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			 partners=MapperUtils.mapAll(page.getContent(), FTPSearchResponseDto.class);
		}
		pageDto.setData(partners);
		pageDto.setFromRecord(0);
		pageDto.setToRecord(page.getContent().size());
		pageDto.setTotalRecord(page.getContent().size());
		return pageDto;
	}
}
