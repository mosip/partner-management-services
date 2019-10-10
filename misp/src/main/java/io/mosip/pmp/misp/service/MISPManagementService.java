package io.mosip.pmp.misp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.pmp.misp.dto.MISPCreateRequestDto;
import io.mosip.pmp.misp.dto.MISPCreateResponseDto;
import io.mosip.pmp.misp.dto.MISPLiceneseDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPWithLicenseDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.ResponseWrapper;
import io.mosip.pmp.misp.entity.MISPEntity;
import io.mosip.pmp.misp.entity.MISPLicenseEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.misp.exception.ErrorMessages;
import io.mosip.pmp.misp.exception.MISPException;
import io.mosip.pmp.misp.repository.MispLicenseKeyRepository;
import io.mosip.pmp.misp.repository.MispServiceRepository;
import io.mosip.pmp.misp.utils.MetaDataUtils;

@Service
public class MISPManagementService {

	@Autowired
	private MispServiceRepository mispRepository;
	
	@Autowired
	private MispLicenseKeyRepository misplKeyRepository;	
	
//	@Autowired
//	private MispIdGenerator<String> mispIdGenerator;
//	
//	@Autowired
//	private MISPLicenseGenerator<String> mispLicenseKeyGenerator;
	
	public ResponseWrapper<MISPCreateResponseDto> createMISP(MISPCreateRequestDto createRequestDto){
		
		validateMISPWithName(createRequestDto.getName());
		
		ResponseWrapper<MISPCreateResponseDto> response = new ResponseWrapper<>();
		MISPCreateResponseDto responseDto = new MISPCreateResponseDto();
		
		MISPEntity mispEntity = MetaDataUtils.setCreateMetaData(createRequestDto, MISPEntity.class);
		
		mispEntity.setID(mispRepository.count() + "1");
		mispEntity.setIsActive(true);
		mispEntity.setName(mispEntity.getName());
		mispEntity.setCreatedDateTime(LocalDateTime.now());
		mispEntity.setUserID("Nagarjuna");
		mispEntity.setCreatedBy("SYSTEM");
		
		MISPLicenseEntity misplEntity = new MISPLicenseEntity();
		
		misplEntity.setLicense_key("qqqq-uuyuyu-klklklk");		
		misplEntity.setValidFromDate(LocalDateTime.now());
		misplEntity.setValidToDate(LocalDateTime.now());	
		misplEntity.setCreatedDateTime(LocalDateTime.now());		
		misplEntity.setMisp_id(mispEntity.getID());
		misplEntity.setIsActive(true);
		misplEntity.setCreatedBy("SYSTEM");
		
		
		mispEntity.setMispLicenses(misplEntity);
		
		mispEntity = mispRepository.save(mispEntity);
		misplEntity =  misplKeyRepository.save(misplEntity);
		
		
		responseDto.setMispID(mispEntity.getID());
		responseDto.setMispLicenseKey(misplEntity.getLicense_key());
		responseDto.setMispLicenseKeyExpiry(misplEntity.getValidToDate().toLocalDate().toString());
		responseDto.setMispLicenseKeyStatus(misplEntity.getIsActive()== true ? "Active" : "De-Active");
		responseDto.setMispStatus(misplEntity.getIsActive()== true ? "Active" : "De-Active");
		response.setResponse(responseDto);
		
		return response;
	}
	
	public ResponseWrapper<MISPStatusUpdateResponseDto> updateMISPStatus(MISPStatusUpdateRequestDto requestDto)
	{		
		ResponseWrapper<MISPStatusUpdateResponseDto> response = new ResponseWrapper<>();
		MISPStatusUpdateResponseDto responseDto = new MISPStatusUpdateResponseDto();
		
		Boolean status = (requestDto.getMispStatus().contains("De-Active") ? false: true);
		
		Optional<MISPEntity> mispFromDb = mispRepository.findById(requestDto.getMispId());
		MISPEntity mispEntity = mispFromDb.get();
		
		if(status != mispEntity.getIsActive())
		{
			mispEntity.setIsActive(status);
			mispEntity.setUpdatedDateTime(LocalDateTime.now());
			mispRepository.save(mispEntity);
			
			if(status == true){
				responseDto.setMessage("MISP activated successfully");
			}
			else{
				responseDto.setMessage("MISP deactivated successfully");
			}
		}
		
		response.setResponse(responseDto);
		return response;
	}
	
	public ResponseWrapper<MISPUpdateResponseDto> update(MISPUpdateRequestDto mispUpdateRequestDto)
	{
		ResponseWrapper<MISPUpdateResponseDto> response = new ResponseWrapper<>();
		MISPUpdateResponseDto responseDto = new MISPUpdateResponseDto();
		
		MISPEntity mispEntity = validateMISPWithID(mispUpdateRequestDto.getMispID());

		MISPEntity mispUpdateRequest = MetaDataUtils.setCreateMetaData(mispUpdateRequestDto, MISPEntity.class);
			
		validateMISPWithName(mispUpdateRequest.getName());		
		
		mispEntity.setName(mispUpdateRequest.getName());
		mispEntity.setAddress(mispUpdateRequest.getAddress());
		mispEntity.setContactNumber(mispUpdateRequest.getContactNumber());
		mispEntity.setEmailId(mispUpdateRequest.getEmailId());
		
		mispEntity.setUpdatedDateTime(LocalDateTime.now());
		mispEntity.setUpdatedBy(mispEntity.getCreatedBy());			

		mispRepository.save(mispEntity);

		responseDto.setMispID(mispEntity.getID());
		responseDto.setAddress(mispEntity.getAddress());
		responseDto.setContactNumber(mispEntity.getContactNumber());
		responseDto.setEmailID(mispEntity.getEmailId());
		responseDto.setName(mispEntity.getName());
		
		response.setResponse(responseDto);

		return response;		
	}
	
	public ResponseWrapper<MISPlKeyStatusUpdateResponseDto> updateMisplkeyStatus(MISPlKeyStatusUpdateRequestDto request)
	{
		MISPLicenseEntity mispLicense =  getMispLiecense( new MISPlKeyUniqueKeyEntity(request.getMispId(),request.getMispLicenseKey()));
		
		Boolean status = request.getMispLicenseKeyStatus() == "De-Active" ? false: true;
		
		ResponseWrapper<MISPlKeyStatusUpdateResponseDto> response = new ResponseWrapper<>();
		MISPlKeyStatusUpdateResponseDto responseDto = new MISPlKeyStatusUpdateResponseDto();
		
		if(mispLicense != null)
		{
			mispLicense.setIsActive(status);
			mispLicense.setUpdatedDateTime(LocalDateTime.now());
			mispLicense.setUpdatedBy("Nagarjuna");
			misplKeyRepository.save(mispLicense);
			responseDto.setMispLicenseKeyStatus(request.getMispLicenseKeyStatus());
		}

		response.setResponse(responseDto);
	  
		return response;	
	}
	
	public List<MISPEntity> getAllMISPS()
	{
		List<MISPEntity> allMISPsFromDb = mispRepository.findAll(); 
		
		if(allMISPsFromDb.isEmpty())
		{
			throw new MISPException(ErrorMessages.NO_MISP_DETAILS.getErrorCode(),
					ErrorMessages.NO_MISP_DETAILS.getErrorMessage());
		}
		
		return allMISPsFromDb;
	}
	
	public MISPEntity getMISPDeatils(String id)
	{
		return validateMISPWithID(id);
	}
    
	public  MISPEntity findMispByName(String name)
	{
		MISPEntity mispEntity = mispRepository.findByName(name);
		if(mispEntity == null)
		{
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + name);
		}
		
		List<MISPEntity> misps = mispRepository.findByStartsWithName(mispEntity.getName());
		List<MISPWithLicenseDto> mispsWithLicenses = new ArrayList<MISPWithLicenseDto>();
				
		for(MISPEntity misp : misps)
		{
			MISPWithLicenseDto mispWithLicense = new MISPWithLicenseDto();
			
			mispWithLicense.setAddress(misp.getAddress());
			mispWithLicense.setContactNumber(misp.getContactNumber());
			mispWithLicense.setEmailID(misp.getEmailId());
			mispWithLicense.setId(misp.getID());
			mispWithLicense.setOrganizationName(misp.getName());
			mispWithLicense.setStatus(misp.getIsActive() == true ? "Active" : "De-Active");
			
			List<MISPLicenseEntity> licenses = misplKeyRepository.findByMispId(misp.getID());
			List<MISPLiceneseDto> licensesDto = new ArrayList<MISPLiceneseDto>();
			
			for(MISPLicenseEntity license : licenses)
			{
				MISPLiceneseDto licenseDto = new MISPLiceneseDto();
				licenseDto.setLicenseKey(license.getLicense_key());
				licenseDto.setLicenseKeyExpiry(license.getValidToDate().toLocalDate().toString());
				licenseDto.setLicenseKeyStatus(license.getIsActive() == true ? "Active" : "De-Active");
				licensesDto.add(licenseDto);
			}
			
			mispWithLicense.setLicenses(licensesDto);
			mispsWithLicenses.add(mispWithLicense);
		}
		
         return mispEntity;
	}

	private void validateMISPWithName(String name)
	{
		MISPEntity mispEntity = mispRepository.findByName(name);
		
		if(mispEntity != null)
		{
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + name);
		}
	}
	
	private MISPEntity validateMISPWithID(String id)
	{
		MISPEntity mispEntity = mispRepository.findById(id).get();
		
		if(mispEntity != null)
		{
			return mispEntity;
		}
		else{
			throw new MISPException(ErrorMessages.MISP_ID_NOT_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_ID_NOT_EXISTS.getErrorMessage() + "  " + id);
		}
	}
    
	public MISPLicenseEntity getMispLiecense(MISPlKeyUniqueKeyEntity unique)
	{
		validateMISPWithID(unique.getMisp_id());		
		
		List<MISPLicenseEntity> mispLicenses = misplKeyRepository.findByLicensekey(unique.getLicense_key());
		
		if(mispLicenses.size() <= 0){
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage() + " " + unique.getLicense_key());
			
		}		
		
		MISPLicenseEntity mispLicense =  misplKeyRepository.findById(unique.getMisp_id()).get();
		
		if(mispLicense == null)
		{
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage() + "  MISPID: "  + unique.getMisp_id() + 
					", LicenseKey: " + unique.getLicense_key());
		}
        
		return mispLicense;		
	}
}
