package io.mosip.pmp.misp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.idgenerator.spi.MISPLicenseGenerator;
import io.mosip.kernel.core.idgenerator.spi.MispIdGenerator;
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
import io.mosip.pmp.misp.entity.MISPLicenseReadEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.misp.exception.ErrorMessages;
import io.mosip.pmp.misp.exception.MISPException;
import io.mosip.pmp.misp.repository.MispLicenseKeyReadRepository;
import io.mosip.pmp.misp.repository.MispLicenseKeyRepository;
import io.mosip.pmp.misp.repository.MispServiceRepository;
import io.mosip.pmp.misp.utils.MetaDataUtils;

 /**
  * <p>This class manages business logic before or after performing database operations.</p>
  * This class is performing following operations.</br>
  * 1. Creating misp {@link #createMISP(MISPCreateRequestDto)}</br>
  * 2. Updating the specific misp details {@link #update(MISPUpdateRequestDto)} </br>
  * 3. Updating the specific misp status(Active OR De-Active) {@link #updateMISPStatus(MISPStatusUpdateRequestDto)} </br>
  * 4. Updating the specific misp license status(Active OR De-Active) {@link #updateMisplkeyStatus(MISPlKeyStatusUpdateRequestDto)}</br>
  * 5. Getting or retrieving all the misp's details {@link #getAllMISPS()} </br>
  * 6. Getting or retrieving specific misp details {@link #getMISPDeatils(String)} </br>
  * 7. Validating the misp license key {@link #getMispLiecense(MISPlKeyUniqueKeyEntity)} </br>
  * 
  * @author Nagarjuna Kuchi
  * @version 1.0
  * @since 2019-Oct-11
  *
  */
@Service
public class MISPManagementService {

	@Autowired
	private MispServiceRepository mispRepository;
	
	@Autowired
	private MispLicenseKeyRepository misplKeyRepository;	

	@Autowired
	private MispLicenseKeyReadRepository misplKeyReadRepository;
	
	@Autowired
	private MispIdGenerator<String> mispIdGenerator;
	
	@Autowired
	private MISPLicenseGenerator<String> mispLicenseKeyGenerator;

	/**
	 * This method is creating the misp along with license. 
	 * 	</br>
	 *  1. checks the database for the uniqueness of name. </br>
	 *     If name exists will throw exception. </br>
	 *  2. Transfers or maps the request dto to db entity.</br>
	 *  3. Uses kernal misp id generator to generate misp id.</br>
	 *  4. By default sets the Inactive to true, created date as today's date, userId as login id,
	 *     and create dby as login name.</br>
	 *  5. Uses kernal license generator to generate misp license key.</br>
	 *  6. Uses pre defined variables: Active for true and De-Active for false </br>
	 *  7. By using corresponding repositories will save or insert the data into misp and misp_license tables. </br>
	 *  8. After successful insertion maps the data to response dto. </br>
	 *  9. And returns the response.</br>
	 * 
	 * @param mispCreateRequest{@link {@link MISPCreateRequestDto} this class contains all the input/request fields.
	 * @return MISPCreateResponseDto {@link MISPCreateResponseDto} this class contains all the response fields.
	 */
	public ResponseWrapper<MISPCreateResponseDto> createMISP(MISPCreateRequestDto mispCreateRequest){
		
		validateMISPWithName(mispCreateRequest.getName());
		
		ResponseWrapper<MISPCreateResponseDto> response = new ResponseWrapper<>();
		MISPCreateResponseDto responseDto = new MISPCreateResponseDto();
		
		MISPEntity mispEntity = MetaDataUtils.setCreateMetaData(mispCreateRequest, MISPEntity.class);
		
		mispEntity.setID(mispIdGenerator.generateId());
		mispEntity.setIsActive(true);
		mispEntity.setName(mispEntity.getName());
		mispEntity.setCreatedDateTime(LocalDateTime.now());
		mispEntity.setUserID("SYSTEM");
		mispEntity.setCreatedBy("SYSTEM");
		
		MISPLicenseEntity misplEntity = new MISPLicenseEntity();
		
		misplEntity.setLicense_key(mispLicenseKeyGenerator.generateLicense());		
		misplEntity.setValidFromDate(LocalDateTime.now());
		misplEntity.setValidToDate(LocalDateTime.now().plusDays(60));	
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
	
	/**
	 * This method update the status of misp to active or de-active.
	 * 
	 * 1. Checks the db with misp id to get misp details.</br>
	 *    if misp details not exists for misp id throws exception </br>
	 * 2. Update the misp status according to input received. </br>
	 * 3. Send the response to the caller </br>
	 * @param mispStatusUpdateRequest {@link MISPStatusUpdateRequestDto} this class contains all the request parameters. 
	 * @return MISPStatusUpdateResponseDto {@link MISPStatusUpdateResponseDto} this class contains all the response parameters.
	 */
	public ResponseWrapper<MISPStatusUpdateResponseDto> updateMISPStatus(MISPStatusUpdateRequestDto mispStatusUpdateRequest)
	{		
		ResponseWrapper<MISPStatusUpdateResponseDto> response = new ResponseWrapper<>();
		MISPStatusUpdateResponseDto responseDto = new MISPStatusUpdateResponseDto();
		
		Boolean status = (mispStatusUpdateRequest.getMispStatus().contains("De-Active") ? false: true);
		
		Optional<MISPEntity> mispFromDb = mispRepository.findById(mispStatusUpdateRequest.getMispId());
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
	
	/**
	 * This method update the MISP details for a specific misp.
	 * </br>
	 * 1. Checks the db with misp id for misp details.</br>
	 *   if misp details not found throws exception.</br>
	 * 2. Checks for the uniqueness of name </br>
	 *    if duplicate name found throws exception. </br>
	 * 3. And saves the data with inputed updated data to misp table. </br>
	 * 4. And sends updated details as response. </br>
	 * @param mispUpdateRequestDto {@link MISPUpdateRequestDto} this class contains all the required fields for misp update request.
	 * @return mispUpdateResponseDto {@link MISPUpdateResponseDto} this class contains all the required fields for misp update response.
	 */
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
	
	/** 
	 * This method update the status of misp license key to Active or DeActive of a specific misp.
	 * 
	 * 1. Checks the db for misp with specific misp id. </br>
	 *    if misp details not found throws exception. </br>
	 * 2. Checks the db with the license key. </br>
	 *    if license key details not found throws the exception. </br>
	 * 3. Checks the with the combination of misp id and license key. </br>
	 *     if details not found throws the exception. </br>
	 * 4. Save the data with inputed data. </br>
	 * 5. And returns the updated data </br>
	 * @param misplKeyStatusUpdateRequest {@link MISPlKeyStatusUpdateRequestDto} this class contains all the required fields for misp license key status update request.
	 * @return MISPlKeyStatusUpdateResponseDto {@link MISPlKeyStatusUpdateResponseDto} this class contains all the required fields for misp license key status update response.
	 */
	public ResponseWrapper<MISPlKeyStatusUpdateResponseDto> updateMisplkeyStatus(MISPlKeyStatusUpdateRequestDto misplKeyStatusUpdateRequest)
	{
		MISPLicenseReadEntity mispValidLicense =  getMispLicense( new MISPlKeyUniqueKeyEntity(misplKeyStatusUpdateRequest.getMispId(),
				misplKeyStatusUpdateRequest.getMispLicenseKey()));
		
		Boolean status = misplKeyStatusUpdateRequest.getMispLicenseKeyStatus().contains("De-Active") ? false : true;
		
		ResponseWrapper<MISPlKeyStatusUpdateResponseDto> response = new ResponseWrapper<>();
		MISPlKeyStatusUpdateResponseDto responseDto = new MISPlKeyStatusUpdateResponseDto();
		
		MISPLicenseEntity mispLicense = new MISPLicenseEntity();
		
		mispLicense.setMisp_id(mispValidLicense.getMispUniqueEntity().getMisp_id());		
		mispLicense.setIsActive(status);
		mispLicense.setUpdatedDateTime(LocalDateTime.now());
		mispLicense.setUpdatedBy("SYSTEM");
		
		misplKeyRepository.save(mispLicense);
		
		responseDto.setMispLicenseKeyStatus(misplKeyStatusUpdateRequest.getMispLicenseKeyStatus());
		
		response.setResponse(responseDto);
	  
		return response;	
	}
	
	/**
	 * <p>This method brings the all misp's present in the database.</p>
	 * </br>
	 * 1. Checks the db for misp details.</br>
	 * 2. if misp details not found throws the exception </br>
	 * 3. else returns the misp's </br>
	 * @return mispEntity list of misp's </br>
	 */
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
	
	/**
	 *  <p> This method calls the validate misp id function to get the misp details.</p>
	 * @param mispId this is the specific misp id.
	 * @return mispEntity{@link MISPEntity} specific misp details.
	 */
	public MISPEntity getMISPDeatils(String mispId){
		return validateMISPWithID(mispId);
	}
    
	/**
	 * <p>This method gets the misp details by using misp name.</p>
	 * </br>
	 * @param mispName misp name/misp organization name.
	 * @return mispEntity {@link MISPEntity} misp details against to give input misp name.
	 */
	public MISPEntity findMispByName(String mispName){

		MISPEntity mispEntity = mispRepository.findByName(mispName);
		
		if(mispEntity == null)
		{
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + mispName);
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

	/**
	 *  <p> This method validate the misp with misp name</p>
	 *  1. Checks the db with misp name </br>
	 *     if not exists throws the exception </br>. 
	 * @param mispName misp name
	 */
	private void validateMISPWithName(String mispName)
	{
		MISPEntity mispEntity = mispRepository.findByName(mispName);
		
		if(mispEntity != null)
		{
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + mispName);
		}
	}
	
	/**
	 *<p> This method validates the misp id.</p>
	 *</br>
	 * 1. Checks the db for misp details with misp id.</br>
	 *    if not exists throws the exception.</br>
	 * 2. else returns the misp details of the input misp id. </br>
	 * @param mispId misp id
	 * @return MISPEntity {@link MISPEntity}  misp details.
	 */
	private MISPEntity validateMISPWithID(String mispId)
	{
		Optional<MISPEntity> mispEntity = mispRepository.findById(mispId);
		
		if(mispEntity.isPresent())
		{
			return mispEntity.get();
		}
		else{
			throw new MISPException(ErrorMessages.MISP_ID_NOT_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_ID_NOT_EXISTS.getErrorMessage() + "  " + mispId);
		}
	}
    
	/**
	 * <p>This method brings misp license details.</p>
	 * 1. Validates the misp id </br>
	 * 2. Validates the misp license key </br>
	 * 3. Validates the combination of misp id and license key </br>
	 * 4. Returns the license key for combination of misp id and license key. </br>
	 * @param mispUniqueKey {@link MISPlKeyUniqueKeyEntity } This request contains unique key of misp license(misp_id, license key)
	 * @return {@link MISPLicenseReadEntity } misp license 
	 */
	public MISPLicenseReadEntity getMispLicense(MISPlKeyUniqueKeyEntity mispUniqueKey)
	{
		validateMISPWithID(mispUniqueKey.getMisp_id());		
		
		List<MISPLicenseEntity> mispLicenses = misplKeyRepository.findByLicensekey(mispUniqueKey.getLicense_key());
		
		if(mispLicenses.size() <= 0){
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage() + " " + mispUniqueKey.getLicense_key());
			
		}
		
		Optional<MISPLicenseReadEntity> mispLicense = misplKeyReadRepository.findById(mispUniqueKey);
		
		if(!mispLicense.isPresent())
		{
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage() + "  MISPID: "  + mispUniqueKey.getMisp_id() + 
					", LicenseKey: " + mispUniqueKey.getLicense_key());
		}
		
		return mispLicense.get();		
	}
	
	/**
	 * <p> This method will bring all the misp's starts with given name.</p>
	 *  
	 *  <p>Checks the db with given name for getting the misp details.</p>
	 *  <p> if data exists shows the data else throws the exception saying </p>
	 *  <p> no details found for given misp name.</p>
	 * 
	 * @param name
	 * @return {@link MISPEntity } list of misp's matches to given name.
	 */
	public List<MISPEntity> getMispsByOrganization(String name)
	{
		List<MISPEntity> misps = mispRepository.findByStartsWithName(name); 
		
		if(misps.isEmpty()){
			throw new MISPException("", "No misp is prenset with name :" + name);
		}
		
		return misps;
	}
	
	/**
	 * <p> This method retrieves the misp license key details for given misp id.</p>
	 *  
	 * <p> Validates the misp id. if data not present throws exception.</p>
	 * <p> Checks the db for misp licenses for given misp id. if data not presents throws the exception.</p>
	 * <p> If misp license is not active throws exception saying all licenses are in active.</p>
	 * <p> If misp license is Active and license is expired then creates new license key by making old license key as in active</p> 
	 * @param mispId
	 * @return {@link MISPLiceneseDto} misp license details.
	 */
	public ResponseWrapper<MISPLiceneseDto> retriveMISPLicense(String mispId)
	{
		MISPEntity misp = validateMISPWithID(mispId);
		
		List<MISPLicenseEntity> mispLicenses = misplKeyRepository.findByMispId(mispId);
		
		if(mispLicenses.isEmpty()){
			throw new MISPException("", "MISP " + mispId + "is not having licenses");
		}
		
		ResponseWrapper<MISPLiceneseDto> response = new ResponseWrapper<>();

		MISPLiceneseDto licenseDto = new MISPLiceneseDto();
		
		Boolean isActiveLicensePresent = false;
		for(MISPLicenseEntity mispLicense : mispLicenses)
		{
			if(mispLicense.isActive){			
				
				isActiveLicensePresent = true;
				
				if(mispLicense.getValidToDate().compareTo(LocalDateTime.now()) < 0 ){					
					mispLicense.setIsActive(false);
					mispLicense.setUpdatedBy("SYSTEM");
					mispLicense.setUpdatedDateTime(LocalDateTime.now());					
					
					MISPLicenseEntity misplEntity = new MISPLicenseEntity();
					
					misplEntity.setLicense_key(mispLicenseKeyGenerator.generateLicense());		
					misplEntity.setValidFromDate(LocalDateTime.now());
					misplEntity.setValidToDate(LocalDateTime.now().plusDays(60));	
					misplEntity.setCreatedDateTime(LocalDateTime.now());		
					misplEntity.setMisp_id(mispLicense.getMisp_id());
					misplEntity.setIsActive(true);
					misplEntity.setCreatedBy("SYSTEM");				
					
					misp.setMispLicenses(misplEntity);
					misp = mispRepository.save(misp);
					misplEntity =  misplKeyRepository.save(misplEntity);
					mispLicense = misplKeyRepository.save(mispLicense);					
					
					licenseDto.setLicenseKey(misplEntity.getLicense_key());
					licenseDto.setLicenseKeyExpiry(misplEntity.getValidToDate().toLocalDate().toString());
					licenseDto.setLicenseKeyStatus("Active");
					response.setResponse(licenseDto);
				}
				else{
					licenseDto.setLicenseKey(mispLicense.getLicense_key());
					licenseDto.setLicenseKeyExpiry(mispLicense.getValidToDate().toLocalDate().toString());
					licenseDto.setLicenseKeyStatus("Active");	
					response.setResponse(licenseDto);
				}
			}
		}
		
		if(!isActiveLicensePresent){
			//
		}
		
		return response;
	}
}
