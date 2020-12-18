package io.mosip.pmp.partner.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pmp.authdevice.util.dto.Type;
import io.mosip.pmp.misp.dto.MISPLicenseResponseDto;
import io.mosip.pmp.misp.exception.MISPErrorMessages;
import io.mosip.pmp.misp.exception.MISPServiceException;
import io.mosip.pmp.partner.constant.EventType;
import io.mosip.pmp.partner.entity.MISPLicenseEntity;
import io.mosip.pmp.partner.entity.MISPLicenseKey;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.repository.MispLicenseRepository;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.partner.service.InfraProviderService;
import io.mosip.pmp.partner.util.WebSubPublisher;

@Component
public class InfraProviderServiceImpl implements InfraProviderService {	
	
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
	
	public static final String APPROVED_STATUS = "approved";
	public static final String REJECTED_STATUS = "rejected";
	public static final String ACTIVE_STATUS = "active";
	public static final String NOTACTIVE_STATUS = "de-active";
	
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
		return response;
	}
	
	private String generate() {
		String generatedLicenseKey = RandomStringUtils.randomAlphanumeric(licenseKeyLength);
		return generatedLicenseKey;
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
		notify(licenseKey);
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
				notify(licenseKey.getMispLicenseUniqueKey().getLicense_key());
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
	 * @param mispLicenseKey
	 */
	private void notify(String mispLicenseKey) {
		Type type = new io.mosip.pmp.authdevice.util.dto.Type();
		type.setName("InfraProviderServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.InfraProviderServiceImpl");
		Map<String,Object> data = new HashMap<>();
		data.put("mispLicenseKey", mispLicenseKey);
		webSubPublisher.notify(EventType.MISP_UPDATED,data,type);
	}
}
