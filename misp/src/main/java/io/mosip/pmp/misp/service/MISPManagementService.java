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
import io.mosip.pmp.misp.utils.MispLogger;

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
		ResponseWrapper<MISPCreateResponseDto> response = new ResponseWrapper<>();
		MISPCreateResponseDto responseDto = new MISPCreateResponseDto();
		
		MispLogger.info("Validating misp name " + mispCreateRequest.getName());
		validateMISPWithName(mispCreateRequest.getName());				

		MispLogger.info("Casting misp create request to misp entity(table)");
		MISPEntity mispEntity = MetaDataUtils.setCreateMetaData(mispCreateRequest, MISPEntity.class);
		
		MispLogger.info("Generating misp id by using kerner misp is generator");
		try{
			mispEntity.setID(mispIdGenerator.generateId());
		}catch(Exception e){
			MispLogger.error("Error occured while generating misp id. Check kernel misp id generator logs.");
			MispLogger.logStackTrace(e);
			throw new MISPException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
					ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		}
		
		MispLogger.info("Generated misp id : " + mispEntity.getID());
		mispEntity.setIsActive(true);
		mispEntity.setName(mispEntity.getName());
		mispEntity.setCreatedDateTime(LocalDateTime.now());
		mispEntity.setUserID("SYSTEM");
		mispEntity.setCreatedBy("SYSTEM");		

		MispLogger.info("Generating misp licenseKey by using kerner misp licenseKey generator");
		MISPLicenseEntity misplEntity = new MISPLicenseEntity();
		try{
			misplEntity.setLicense_key(mispLicenseKeyGenerator.generateLicense());
		}catch(Exception e){
			MispLogger.error("Error occured while generating misp licenseKey. Check kernel misp licenseKey generator logs.");
			MispLogger.logStackTrace(e);
			throw new MISPException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
					ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		}		
		MispLogger.info("Generated misp licenseKey : " + misplEntity.getLicense_key());
		
		misplEntity.setValidFromDate(LocalDateTime.now());
		misplEntity.setValidToDate(LocalDateTime.now().plusDays(60));	
		misplEntity.setCreatedDateTime(LocalDateTime.now());		
		misplEntity.setMisp_id(mispEntity.getID());
		misplEntity.setIsActive(true);
		misplEntity.setCreatedBy("SYSTEM");
		mispEntity.setMispLicenses(misplEntity);
		
		MispLogger.info("Data insertion stated into misp table");
		try{
			mispEntity = mispRepository.save(mispEntity);
		}catch(Exception e){
			MispLogger.error("Error occured while inserting data into misp table.");
			MispLogger.logStackTrace(e);
			throw new MISPException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
					ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		}		
		
		MispLogger.info("Data insertion stated into misp_license table");
		try{
			misplEntity =  misplKeyRepository.save(misplEntity);
		}catch(Exception e){
			MispLogger.error("Error occured while inserting data into misp_license table.");
			MispLogger.logStackTrace(e);
			throw new MISPException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
					ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		}

		MispLogger.info("Data inserted into misp_license table");
		responseDto.setMispID(mispEntity.getID());
		responseDto.setMispLicenseKey(misplEntity.getLicense_key());
		responseDto.setMispLicenseKeyExpiry(misplEntity.getValidToDate().toLocalDate().toString());
		responseDto.setMispLicenseKeyStatus(misplEntity.getIsActive()== true ? "Active" : "De-Active");
		responseDto.setMispStatus(misplEntity.getIsActive()== true ? "Active" : "De-Active");
		response.setResponse(responseDto);
		
		MispLogger.info("Cursor back to controller");
		
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
	public ResponseWrapper<MISPStatusUpdateResponseDto> updateMISPStatus(MISPStatusUpdateRequestDto mispStatusUpdateRequest){		
		ResponseWrapper<MISPStatusUpdateResponseDto> response = new ResponseWrapper<>();
		MISPStatusUpdateResponseDto responseDto = new MISPStatusUpdateResponseDto();
		Boolean status = (mispStatusUpdateRequest.getMispStatus().contains("De-Active") ? false: true);
		
		MispLogger.info("Validating misp id " + mispStatusUpdateRequest.getMispId());
		MISPEntity mispEntity = validateMISPWithID(mispStatusUpdateRequest.getMispId());
		if(status != mispEntity.getIsActive()){
			mispEntity.setIsActive(status);
			mispEntity.setUpdatedDateTime(LocalDateTime.now());			
			try{
				MispLogger.info("Updating the misp status");	
				mispRepository.save(mispEntity);
			}catch(Exception e){
				MispLogger.error("Error occured while updating the misp status");
				MispLogger.logStackTrace(e);
				throw new MISPException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
						ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
			}
		}
		if(status == true){
			responseDto.setMessage("MISP activated successfully");
		}else{
			responseDto.setMessage("MISP deactivated successfully");
		}
		
		response.setResponse(responseDto);
		
		MispLogger.info("Cursor back to controller");
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
	public ResponseWrapper<MISPUpdateResponseDto> update(MISPUpdateRequestDto mispUpdateRequestDto){
		ResponseWrapper<MISPUpdateResponseDto> response = new ResponseWrapper<MISPUpdateResponseDto>();
		MISPUpdateResponseDto responseDto = new MISPUpdateResponseDto();
		
		MispLogger.info("Validating misp id " + mispUpdateRequestDto.getMispID());
		MISPEntity mispEntity = validateMISPWithID(mispUpdateRequestDto.getMispID());
		
		MISPEntity mispUpdateRequest = MetaDataUtils.setCreateMetaData(mispUpdateRequestDto, MISPEntity.class);		
		
		MispLogger.info("Validating misp name " + mispUpdateRequestDto.getName());		
		MISPEntity mispName = mispRepository.findByName(mispUpdateRequestDto.getName());
		if(mispName!=null && !mispName.getID().equals(mispEntity.getID())){
			MispLogger.error("Misp details found with misp name : " + mispName + "misp id :" + mispEntity.getID());
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + mispName + "misp id :" + mispEntity.getID());
		}
		
		mispEntity.setName(mispUpdateRequest.getName());
		mispEntity.setAddress(mispUpdateRequest.getAddress());
		mispEntity.setContactNumber(mispUpdateRequest.getContactNumber());
		mispEntity.setEmailId(mispUpdateRequest.getEmailId());		
		mispEntity.setUpdatedDateTime(LocalDateTime.now());
		mispEntity.setUpdatedBy(mispEntity.getCreatedBy());	
		
		MispLogger.info("Updating the misp details into misp table");
		try{
			mispRepository.save(mispEntity);
		}catch(Exception e){			
			MispLogger.error("Error occured  while updating the misp");
			MispLogger.logStackTrace(e);
			throw new MISPException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
					ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		}
		
		responseDto.setMispID(mispEntity.getID());
		responseDto.setAddress(mispEntity.getAddress());
		responseDto.setContactNumber(mispEntity.getContactNumber());
		responseDto.setEmailID(mispEntity.getEmailId());
		responseDto.setName(mispEntity.getName());
		
		MispLogger.info("Cursor back to controller");
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
		MispLogger.info("Validating the misp license along with misp id.");
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

		MispLogger.info("Updating the misp license status.");
		try{
			misplKeyRepository.save(mispLicense);
		}catch(Exception e){
			MispLogger.error("Error occured while updating the misp license status.");
			MispLogger.logStackTrace(e);
			throw new MISPException(ErrorMessages.INTERNAL_SERVER_ERROR.getErrorCode(),
					ErrorMessages.INTERNAL_SERVER_ERROR.getErrorMessage());
		}

		responseDto.setMispLicenseKeyStatus(misplKeyStatusUpdateRequest.getMispLicenseKeyStatus());

		response.setResponse(responseDto);
		MispLogger.info("Cursor back to controller");
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
		MispLogger.info("Getting all the misp details from the database");
		List<MISPEntity> allMISPsFromDb = mispRepository.findAll(); 

		if(allMISPsFromDb.isEmpty())
		{
			MispLogger.error("No misp details found in database");
			throw new MISPException(ErrorMessages.NO_MISP_DETAILS.getErrorCode(),
					ErrorMessages.NO_MISP_DETAILS.getErrorMessage());
		}

		MispLogger.info("Cursor back to controller");
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
		MispLogger.info("Finding the misp details with misp name : " + mispName);
		MISPEntity mispEntity = mispRepository.findByName(mispName);

		if(mispEntity == null)
		{
			MispLogger.error("No misp details exists with given misp name " + mispName);
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + mispName);
		}

		MispLogger.info("Finding the misp details starts with misp name : " + mispName);
		List<MISPEntity> misps = mispRepository.findByStartsWithName(mispEntity.getName());

		if(misps.isEmpty())
		{
			MispLogger.error("No misp details exists with starting misp name " + mispName);
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + mispName);
		}

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
		MispLogger.info("Reading the database with misp name " + mispName);
		MISPEntity mispEntity = mispRepository.findByName(mispName);

		if(mispEntity != null)
		{				
			MispLogger.error("Misp details found with misp name : " + mispName);
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
		MispLogger.info("Finding the misp details with misp id " + mispId);
		Optional<MISPEntity> mispEntity = mispRepository.findById(mispId);

		if(mispEntity.isPresent())
		{
			return mispEntity.get();
		}
		else{
			MispLogger.warn("No details found for misp id " + mispId);
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
		MispLogger.info("Validating the misp id " + mispUniqueKey.getMisp_id());
		validateMISPWithID(mispUniqueKey.getMisp_id());		

		MispLogger.info("Validating the misp license key " + mispUniqueKey.getLicense_key());
		List<MISPLicenseEntity> mispLicenses = misplKeyRepository.findByLicensekey(mispUniqueKey.getLicense_key());

		if(mispLicenses.size() <= 0){
			MispLogger.warn("No details found for license key " + mispUniqueKey.getLicense_key());
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage() + " " + mispUniqueKey.getLicense_key());

		}

		MispLogger.info("Validating misp license key " + mispUniqueKey.getLicense_key() + " along with misp id"
				+ "." + mispUniqueKey.getMisp_id());
		Optional<MISPLicenseReadEntity> mispLicense = misplKeyReadRepository.findById(mispUniqueKey);
		if(!mispLicense.isPresent()){
			MispLogger.warn("No details found for combination of misp license key " + mispUniqueKey.getLicense_key() + 
					" and misp id" + "." + mispUniqueKey.getMisp_id());
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage() + "  MISPID: "  
			+ mispUniqueKey.getMisp_id() + ", LicenseKey: " + mispUniqueKey.getLicense_key());
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
	public List<MISPEntity> getMispsByOrganization(String name)	{
		MispLogger.info("Finding misp details starts with " + name);
		List<MISPEntity> misps = mispRepository.findByStartsWithName(name);
		if(misps.isEmpty()){
			MispLogger.warn("No details found  with name starts with " + name);
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
	public ResponseWrapper<MISPLiceneseDto> retriveMISPLicense(String mispId){
		MispLogger.info("Validating the misp id " + mispId);
		MISPEntity misp = validateMISPWithID(mispId);

		MispLogger.info("Getting the misp license key with misp id " + mispId);
		List<MISPLicenseEntity> mispLicenses = misplKeyRepository.findByMispId(mispId);

		if(mispLicenses.isEmpty()){
			MispLogger.warn("No license details found for misp id " + mispId);
			throw new MISPException("", "MISP " + mispId + "is not having licenses");
		}

		ResponseWrapper<MISPLiceneseDto> response = new ResponseWrapper<>();

		MISPLiceneseDto licenseDto = new MISPLiceneseDto();

		Boolean isActiveLicensePresent = false;
		for(MISPLicenseEntity mispLicense : mispLicenses){
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
