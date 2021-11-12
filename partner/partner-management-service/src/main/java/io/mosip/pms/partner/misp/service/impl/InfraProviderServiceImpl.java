package io.mosip.pms.partner.misp.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pms.common.constant.ConfigKeyConstants;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.MISPDataPublishDto;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.MISPLicenseKey;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.MispLicenseRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.exception.MISPErrorMessages;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.InfraServiceProviderService;

@Component
public class InfraProviderServiceImpl implements InfraServiceProviderService {	
	
	@Value("${mosip.kernel.idgenerator.misp.license-key-length}")
	private int licenseKeyLength;
	
	@Value("${mosip.pmp.misp.license.expiry.period.indays}")
	private int mispLicenseExpiryInDays;
	
	@Autowired
	MispLicenseRepository mispLicenseRepository;	
	
	@Autowired
	PartnerServiceRepository partnerRepository;
	
	@Autowired
	private WebSubPublisher webSubPublisher;
	
	@Autowired
	private Environment environment;
	
	public static final String APPROVED_STATUS = "approved";
	public static final String REJECTED_STATUS = "rejected";
	public static final String ACTIVE_STATUS = "active";
	public static final String NOTACTIVE_STATUS = "de-active";
	public static final String ACTIVE = "ACTIVE";
	public static final String NOTACTIVE = "NOT_ACTIVE";
	
	/**
	 * 
	 */
	@Override
	public MISPLicenseResponseDto approveInfraProvider(String mispId) {
		Optional<Partner> partnerFromDb = partnerRepository.findById(mispId);
		if(partnerFromDb.isEmpty()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorCode(),
					MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorMessage());
		}
		if(!partnerFromDb.get().getPartnerTypeCode().equalsIgnoreCase(environment.getProperty(ConfigKeyConstants.MISP_PARTNER_TYPE, "MISP_Partner"))) {
			throw new MISPServiceException(MISPErrorMessages.MISP_ID_NOT_VALID.getErrorCode(),
					MISPErrorMessages.MISP_ID_NOT_VALID.getErrorMessage());
		}
		if(!partnerFromDb.get().getIsActive()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_IS_INACTIVE.getErrorCode(),
					MISPErrorMessages.MISP_IS_INACTIVE.getErrorMessage());			
		}
		
		List<MISPLicenseEntity> mispLicenseFromDb = mispLicenseRepository.findByMispId(mispId);
		if(!mispLicenseFromDb.isEmpty()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_EXISTS.getErrorCode(),
					MISPErrorMessages.MISP_LICENSE_KEY_EXISTS.getErrorMessage());
		}		
		MISPLicenseEntity newLicenseKey =   generateLicense(mispId);
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		response.setLicenseKey(newLicenseKey.getMispLicenseUniqueKey().getLicense_key());
		response.setLicenseKeyExpiry(newLicenseKey.getValidToDate());
		response.setLicenseKeyStatus("Active");
		response.setProviderId(mispId);
		notify(MapperUtils.mapDataToPublishDto(newLicenseKey), EventType.MISP_LICENSE_GENERATED);
		return response;
	}
	
	/**
	 * 
	 * @return
	 */
	private String generate() {
		return RandomStringUtils.randomAlphanumeric(licenseKeyLength);
	}
	
	/**
	 * 
	 */
	@Override
	public MISPLicenseResponseDto updateInfraProvider(String id, String licenseKey, String status) {
		if(!(status.toLowerCase().equals(ACTIVE_STATUS) || 
				status.toLowerCase().equals(NOTACTIVE_STATUS))) {					
			throw new MISPServiceException(MISPErrorMessages.MISP_STATUS_CODE_EXCEPTION.getErrorCode(),
					MISPErrorMessages.MISP_STATUS_CODE_EXCEPTION.getErrorMessage());
		}
		
		MISPLicenseEntity mispLicenseFromDb = mispLicenseRepository.findByIdAndKey(id, licenseKey);
		if(mispLicenseFromDb == null) {
			throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage());
		}
		mispLicenseFromDb.setUpdatedBy(getUser());
		mispLicenseFromDb.setUpdatedDateTime(LocalDateTime.now());
		mispLicenseFromDb.setIsActive(status.toLowerCase().equals(ACTIVE_STATUS) ? true : false);
		mispLicenseRepository.save(mispLicenseFromDb);
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		response.setLicenseKey(mispLicenseFromDb.getMispLicenseUniqueKey().getLicense_key());
		response.setLicenseKeyExpiry(mispLicenseFromDb.getValidToDate());
		response.setLicenseKeyStatus(mispLicenseFromDb.getIsActive() ? ACTIVE_STATUS : NOTACTIVE_STATUS);
		response.setProviderId(mispLicenseFromDb.getMispLicenseUniqueKey().getMisp_id());
		notify(MapperUtils.mapDataToPublishDto(mispLicenseFromDb), EventType.MISP_LICENSE_UPDATED);
		return response;

	}

	/**
	 * 
	 */
	@Override
	public List<MISPLicenseEntity> getInfraProvider() {
        return mispLicenseRepository.findAll();
	}

	/**
	 * 
	 * @param mispId
	 * @return
	 */
	private MISPLicenseEntity generateLicense(String mispId) {
		MISPLicenseEntity entity = new MISPLicenseEntity();
		MISPLicenseKey entityKey = new MISPLicenseKey();
		entityKey.setLicense_key(generate());
		entityKey.setMisp_id(mispId);
		entity.setMispLicenseUniqueKey(entityKey);
		entity.setValidFromDate(LocalDateTime.now());
		entity.setValidToDate(LocalDateTime.now().plusDays(mispLicenseExpiryInDays));
		entity.setCreatedBy(getUser());
		entity.setCreatedDateTime(LocalDateTime.now());
		entity.setIsActive(true);
		entity.setIsDeleted(false);
		mispLicenseRepository.save(entity);
		return entity;
	}
	
	/**
	 * 
	 */
	@Override
	public MISPLicenseResponseDto regenerateKey(String mispId) {
		Optional<Partner> partnerFromDb = partnerRepository.findById(mispId);
		if(partnerFromDb.isEmpty()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorCode(),
					MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorMessage());
		}
		if(!partnerFromDb.get().getIsActive()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_IS_INACTIVE.getErrorCode(),
					MISPErrorMessages.MISP_IS_INACTIVE.getErrorMessage());
		}
		List<MISPLicenseEntity> mispLicenses = mispLicenseRepository.findByMispId(mispId);
		boolean isActiveLicenseExists = false;
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		for(MISPLicenseEntity licenseKey : mispLicenses) {
			if(licenseKey.getIsActive() && 
					licenseKey.getValidToDate().isBefore(LocalDateTime.now())) {
				isActiveLicenseExists =true;
				licenseKey.setIsActive(false);
				licenseKey.setUpdatedBy(getUser());
				licenseKey.setUpdatedDateTime(LocalDateTime.now());
				mispLicenseRepository.save(licenseKey);
				MISPLicenseEntity newLicenseKey =   generateLicense(mispId);
				response.setLicenseKey(newLicenseKey.getMispLicenseUniqueKey().getLicense_key());
				response.setLicenseKeyExpiry(newLicenseKey.getValidToDate());
				response.setLicenseKeyStatus("Active");
				response.setProviderId(mispId);
				notify(MapperUtils.mapDataToPublishDto(newLicenseKey), EventType.MISP_LICENSE_UPDATED);
			}
			
			if(licenseKey.getIsActive() && 
					licenseKey.getValidToDate().isAfter(LocalDateTime.now())) {
				isActiveLicenseExists =true;
				response.setLicenseKey(licenseKey.getMispLicenseUniqueKey().getLicense_key());
				response.setLicenseKeyExpiry(licenseKey.getValidToDate());
				response.setLicenseKeyStatus("Active");
				response.setProviderId(mispId);
			}
		}
		
		if(!isActiveLicenseExists) {
			throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_ARE_NOT_ACTIVE.getErrorCode(),
					MISPErrorMessages.MISP_LICENSE_ARE_NOT_ACTIVE.getErrorMessage());
		}
		
		return response;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUser() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.getUserId();
		} else {
			return "system";
		}
	}	
	
	/**
	 * 
	 * @param dataToPublish
	 * @param eventType
	 */
	private void notify(MISPDataPublishDto dataToPublish,EventType eventType) {
		Type type = new Type();
		type.setName("InfraProviderServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.InfraProviderServiceImpl");
		Map<String,Object> data = new HashMap<>();
		data.put("mispLicenseData", dataToPublish);
		webSubPublisher.notify(eventType,data,type);		
	}
}
