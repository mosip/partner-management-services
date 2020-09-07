package io.mosip.pmp.misp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.mosip.kernel.auth.adapter.model.AuthUserDetails;
import io.mosip.kernel.core.idgenerator.spi.MISPLicenseGenerator;
import io.mosip.kernel.core.idgenerator.spi.MispIdGenerator;
import io.mosip.pmp.misp.dto.MISPCreateRequestDto;
import io.mosip.pmp.misp.dto.MISPCreateResponseDto;
import io.mosip.pmp.misp.dto.MISPDetailsDto;
import io.mosip.pmp.misp.dto.MISPLiceneseDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponse;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyResponseDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.ResponseWrapper;
import io.mosip.pmp.misp.entity.MISPEntity;
import io.mosip.pmp.misp.entity.MISPLicenseEntity;
import io.mosip.pmp.misp.entity.MISPLicenseReadEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.misp.exception.ErrorMessages;
import io.mosip.pmp.misp.exception.MISPException;
import io.mosip.pmp.misp.repository.MispLicenseKeyRepository;
import io.mosip.pmp.misp.repository.MispServiceRepository;
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
 * 8. Update misp request status {@link #processRequest(MISPStatusUpdateRequestDto)}
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
	private MispIdGenerator<String> mispIdGenerator;

	@Autowired
	private MISPLicenseGenerator<String> mispLicenseKeyGenerator;
	
	@Value("${mosip.pmp.misp.license.expiry.period.indays}")
	private int mispLicenseExpiryInDays;
	
	@Value("${pmp.misp.valid.email.address.regex}")
	private String emailRegex;

	public static final String APPROVED_STATUS = "approved";
	public static final String REJECTED_STATUS = "rejected";
	public static final String ACTIVE_STATUS = "active";
	public static final String NOTACTIVE_STATUS = "de-active";
	
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
		if(!emailValidator(mispCreateRequest.getEmailId())) {
			MispLogger.error(mispCreateRequest.getEmailId() + " : this is invalid email");
			throw new MISPException(
					ErrorMessages.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					ErrorMessages.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());

		}
		ResponseWrapper<MISPCreateResponseDto> response = new ResponseWrapper<>();
		MISPCreateResponseDto responseDto = new MISPCreateResponseDto();
		
		MispLogger.info("Validating misp name " + mispCreateRequest.getName());
		validateName(mispCreateRequest.getName());	
		MISPEntity mispEntity = new MISPEntity();
		
		MispLogger.info("Generating misp id by using kernel misp id generator");
		mispEntity.setID(mispIdGenerator.generateId());	
		mispEntity.setIsActive(true);
		mispEntity.setName(mispCreateRequest.getName());
		mispEntity.setEmailId(mispCreateRequest.getEmailId());
		mispEntity.setContactNumber(mispCreateRequest.getContactNumber());
		mispEntity.setAddress(mispCreateRequest.getAddress());
		mispEntity.setStatus_code("Inprogress");
		mispEntity.setCreatedDateTime(LocalDateTime.now());
		mispEntity.setUserID(getUser());
		mispEntity.setCreatedBy(getUser());
		
		MispLogger.info("Data insertion stated into misp table");
		mispRepository.save(mispEntity);	
		responseDto.setMispID(mispEntity.getID());
		responseDto.setMispStatus("Active");
		response.setResponse(responseDto);		
		MispLogger.info("Cursor back to controller");		
		return response;
	}	
	
	/**
	 * This method is used for to approve or reject the misp.
	 * </br>
	 * 1. Validate for misp id.
	 * 2. Update the misp table with given status.
	 * 3. If misp status is approved then license key will be generated.
	 * 4. Else license key will not be generated.
	 * @param {@link MISPStatusUpdateRequestDto} mispStatusUpdateRequest
	 * @return {@link MISPStatusUpdateResponse} returns the misp update response.
	 */
	
	public ResponseWrapper<MISPStatusUpdateResponse> processRequest(MISPStatusUpdateRequestDto request){
		if(!(request.getMispStatus().toLowerCase().equals(APPROVED_STATUS) || 
				request.getMispStatus().toLowerCase().equals(REJECTED_STATUS))) {
			throw new MISPException(ErrorMessages.STATUS_CODE_EXCEPTION.getErrorCode(),
					ErrorMessages.STATUS_CODE_EXCEPTION.getErrorMessage());
		}		
		MispLogger.info("Updating the misp request.");
		ResponseWrapper<MISPStatusUpdateResponse> response = new ResponseWrapper<>();		
		MispLogger.info("Validating misp id " + request.getMispId());
		MISPEntity misp = findById(request.getMispId());
		if(misp.getStatus_code().toLowerCase().equals(request.getMispStatus().toLowerCase())) {
			throw new MISPException(ErrorMessages.MISP_STATUS_CHENAGE_REQUEST_EXCEPTION.getErrorCode(),
					ErrorMessages.MISP_STATUS_CHENAGE_REQUEST_EXCEPTION.getErrorMessage() + request.getMispStatus().toLowerCase());
		}
		if(misp.getIsActive() == false && 
				request.getMispStatus().toLowerCase().equals(APPROVED_STATUS)){
			MispLogger.info("MISP " + misp.getName() + ""
					+ "is de-activated. Can not be approved the de-actived misps.");
			throw new MISPException(ErrorMessages.MISP_IS_INACTIVE.getErrorCode(),
					ErrorMessages.MISP_IS_INACTIVE.getErrorMessage());
		}
		misp.setStatus_code(request.getMispStatus().toLowerCase());
		misp.setUpdatedDateTime(LocalDateTime.now());
		misp.setUpdatedBy(getUser());
		MispLogger.info("Updating the misp request status");
		mispRepository.save(misp);		
		MispLogger.info("MISP " + request.getMispStatus().toLowerCase() + " successfully");
		MISPStatusUpdateResponse responseDto = new MISPStatusUpdateResponse();				
		if(request.getMispStatus().toLowerCase().equals(APPROVED_STATUS)){
			MISPLicenseEntity misplEntity = generateLicense(request.getMispId());
			responseDto.setMispLicenseKey(misplEntity.getMispLicenseUniqueKey().getLicense_key());			
			responseDto.setMispLicenseKeyExpiry(misplEntity.getValidToDate().toString());
			responseDto.setMispLicenseKeyStatus(ACTIVE_STATUS);			
		}		
		
		if(request.getMispStatus().toLowerCase().equals(REJECTED_STATUS)){
			updateMISPLicenseStatus(request.getMispId(), NOTACTIVE_STATUS);
			MispLogger.info("MISP license key is not generated for rejected misps");
		}
		responseDto.setMessage("MISP " + request.getMispStatus().toLowerCase() + " successfully");
		responseDto.setMispStatus(misp.getIsActive() == true ? "Active" : "De-Active");
		responseDto.setMispStatusCode(misp.getStatus_code());
		responseDto.setMispID(misp.getID());	
		response.setResponse(responseDto);
		MispLogger.info("Cursor back to controller");
		return response;
	}
	
	/**
	 * This method is used to generate the license key for misp.
	 */
	protected MISPLicenseEntity generateLicense(String misp_Id) {
		MispLogger.info("MISP license generation started.");
		MISPLicenseEntity misplEntity = new MISPLicenseEntity();
		MISPlKeyUniqueKeyEntity uniqueKey = new MISPlKeyUniqueKeyEntity();		
		uniqueKey.setLicense_key(mispLicenseKeyGenerator.generateLicense());
		uniqueKey.setMisp_id(misp_Id);
		misplEntity.setValidFromDate(LocalDateTime.now());
		misplEntity.setValidToDate(LocalDateTime.now().plusDays(mispLicenseExpiryInDays));	
		misplEntity.setCreatedDateTime(LocalDateTime.now());		
		misplEntity.setMispLicenseUniqueKey(uniqueKey);
		misplEntity.setIsActive(true);
		misplEntity.setCreatedBy(getUser());
		misplKeyRepository.save(misplEntity);
		return misplEntity;
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
		if(!(mispStatusUpdateRequest.getMispStatus().toLowerCase().equals(ACTIVE_STATUS) || 
				mispStatusUpdateRequest.getMispStatus().toLowerCase().equals(NOTACTIVE_STATUS))) {
			throw new MISPException(ErrorMessages.MISP_STATUS_CODE_EXCEPTION.getErrorCode(),
					ErrorMessages.MISP_STATUS_CODE_EXCEPTION.getErrorMessage());
		}	
		ResponseWrapper<MISPStatusUpdateResponseDto> response = new ResponseWrapper<>();
		
		MISPStatusUpdateResponseDto responseDto = new MISPStatusUpdateResponseDto();
		Boolean status = false;
		if(mispStatusUpdateRequest.getMispStatus().toLowerCase().equals(ACTIVE_STATUS)){
			status = true;
		}		
		MispLogger.info("Validating misp id " + mispStatusUpdateRequest.getMispId());
		MISPEntity mispEntity = findById(mispStatusUpdateRequest.getMispId());		
		
		mispEntity.setIsActive(status);
		mispEntity.setUpdatedDateTime(LocalDateTime.now());
		MispLogger.info("Updating the misp status");	
		mispRepository.save(mispEntity);
		updateMISPLicenseStatus(mispStatusUpdateRequest.getMispId(),mispStatusUpdateRequest.getMispStatus());
		if(status){
			responseDto.setMessage("MISP actived successfully");
		}else{
			responseDto.setMessage("MISP de-actived successfully");
		}		
		MispLogger.info(responseDto.getMessage());
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
		if(!emailValidator(mispUpdateRequestDto.getEmailId())) {
			MispLogger.error(mispUpdateRequestDto.getEmailId() + " : this is invalid email");
			throw new MISPException(
					ErrorMessages.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					ErrorMessages.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());

		}
		ResponseWrapper<MISPUpdateResponseDto> response = new ResponseWrapper<MISPUpdateResponseDto>();
		MISPUpdateResponseDto responseDto = new MISPUpdateResponseDto();
		
		MispLogger.info("Validating misp id " + mispUpdateRequestDto.getMispID());
		MISPEntity mispEntity = findById(mispUpdateRequestDto.getMispID());	
		
		MispLogger.info("Validating misp name " + mispUpdateRequestDto.getName());		
		MISPEntity mispByName = mispRepository.findByName(mispUpdateRequestDto.getName());
		if(mispByName != null && !mispEntity.getID().equals(mispByName.getID())) {
			MispLogger.error("Misp details found with misp name : " + mispUpdateRequestDto.getName() + "Can't create misp with same name.");
			throw new MISPException(ErrorMessages.MISP_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_EXISTS.getErrorMessage() + "  " + mispUpdateRequestDto.getName());
		}
		
		mispEntity.setName(mispUpdateRequestDto.getName());
		mispEntity.setAddress(mispUpdateRequestDto.getAddress());
		mispEntity.setContactNumber(mispUpdateRequestDto.getContactNumber());
		mispEntity.setEmailId(mispUpdateRequestDto.getEmailId());		
		mispEntity.setUpdatedDateTime(LocalDateTime.now());
		mispEntity.setUpdatedBy(mispEntity.getCreatedBy());		
		MispLogger.info("Updating the misp details into misp table");
		mispRepository.save(mispEntity);		
		responseDto.setMispID(mispEntity.getID());
		responseDto.setAddress(mispEntity.getAddress());
		responseDto.setContactNumber(mispEntity.getContactNumber());
		responseDto.setEmailID(mispEntity.getEmailId());
		responseDto.setName(mispEntity.getName());		
		MispLogger.info("Cursor back to controller");
		response.setResponse(responseDto);		
		return response;		
	}

	private void updateMISPLicenseStatus(String misp_id, String status) {
		Boolean activeness = status.toLowerCase().equals(NOTACTIVE_STATUS) ? false : true;
		List<MISPLicenseEntity> licenses = misplKeyRepository.findByMispId(misp_id);
		if(activeness) {
			for(MISPLicenseEntity license : licenses) {
				if(license.getValidToDate().isAfter(LocalDateTime.now())){
					license.setIsActive(true);
				}else {
					license.setIsActive(false);
				}
				license.setUpdatedDateTime(LocalDateTime.now());
				license.setUpdatedBy(getUser());
				misplKeyRepository.save(license);
			}
		}
		
		if(!activeness) {
			for(MISPLicenseEntity license : licenses) {
				license.setIsActive(false);
				license.setUpdatedDateTime(LocalDateTime.now());
				license.setUpdatedBy(getUser());
				misplKeyRepository.save(license);
			}
		}
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
	public ResponseWrapper<MISPlKeyStatusUpdateResponseDto> updateMisplkeyStatus(MISPlKeyStatusUpdateRequestDto updateRequest){		
		Boolean status = updateRequest.getMispLicenseKeyStatus().toLowerCase().equals(NOTACTIVE_STATUS) ? false : true;
		ResponseWrapper<MISPlKeyStatusUpdateResponseDto> response = new ResponseWrapper<>();
		MISPlKeyStatusUpdateResponseDto responseDto = new MISPlKeyStatusUpdateResponseDto();		
		MispLogger.info("Validating the misp license along with misp id.");
		MISPLicenseEntity mispLicense = getLicenseDetails(updateRequest.getMispLicenseKey());
		if(!mispLicense.getMispLicenseUniqueKey().getMisp_id().equals(updateRequest.getMispId())) {
			MispLogger.warn("No details found for combination of misp license key " + updateRequest.getMispLicenseKey() + 
					" and misp id" + "." + updateRequest.getMispId());
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage() + "  MISPID: "  
			+ updateRequest.getMispId() + ", LicenseKey: " + updateRequest.getMispLicenseKey());
		}	
		mispLicense.getMispLicenseUniqueKey().setMisp_id(updateRequest.getMispId());
		if(status && mispLicense.getValidToDate().isBefore(LocalDateTime.now())) {
			throw new MISPException(ErrorMessages.MISP_LICENSE_EXPIRED_NOT_ACTIVATE.getErrorCode(),
					ErrorMessages.MISP_LICENSE_EXPIRED_NOT_ACTIVATE.getErrorMessage());
		}		
		mispLicense.setIsActive(status);
		mispLicense.setUpdatedDateTime(LocalDateTime.now());
		mispLicense.setUpdatedBy(getUser());
		MispLogger.info("Updating the misp license status.");
		misplKeyRepository.save(mispLicense);
		responseDto.setMispLicenseKeyStatus(updateRequest.getMispLicenseKeyStatus());
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
	public List<MISPDetailsDto> getMisps(){
		MispLogger.info("Getting all the misps.");
		List<MISPDetailsDto> mispDetails = new ArrayList<MISPDetailsDto>();
		List<MISPEntity> misps = mispRepository.findAll();		
		if(misps.isEmpty()){
			MispLogger.warn("No details found ");
			throw new MISPException(ErrorMessages.NO_MISP_DETAILS.getErrorCode(),
					ErrorMessages.NO_MISP_DETAILS.getErrorMessage());
		}		
		for(MISPEntity misp : misps) {
			MISPDetailsDto mispDetail = new MISPDetailsDto();
			mispDetail.setMisp(misp);
			mispDetail.setMisp_licenses(misplKeyRepository.findByMispId(misp.getID()));
			mispDetails.add(mispDetail);
		}

		return mispDetails;
	}

	/**
	 *  <p> This method calls the validate misp id function to get the misp details.</p>
	 * @param mispId this is the specific misp id.
	 * @return mispEntity{@link MISPEntity} specific misp details.
	 */
	public MISPDetailsDto getMisp(String mispId){
		MISPDetailsDto response = new MISPDetailsDto();
		MISPEntity misp = findById(mispId);
		List<MISPLicenseEntity> misp_licenses  = misplKeyRepository.findByMispId(mispId);
		response.setMisp(misp);
		response.setMisp_licenses(misp_licenses);
		return response;
	}
	
	/**
	 *  <p> This method validate the misp with misp name</p>
	 *  1. Checks the db with misp name </br>
	 *     if not exists throws the exception </br>. 
	 * @param mispName misp name
	 */
	private void validateName(String mispName){
		MispLogger.info("Reading the database with misp name " + mispName);
		MISPEntity mispEntity = mispRepository.findByName(mispName);
		if(mispEntity != null){				
			MispLogger.error("Misp details found with misp name : " + mispName + "Can't create misp with same name.");
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
	private MISPEntity findById(String mispId) {
		MispLogger.info("Finding the misp details with misp id " + mispId);
		Optional<MISPEntity> misp = mispRepository.findById(mispId);
		if(!misp.isPresent()) {
			MispLogger.warn("No details found for misp id " + mispId);
			throw new MISPException(ErrorMessages.MISP_ID_NOT_EXISTS.getErrorCode(), 
					ErrorMessages.MISP_ID_NOT_EXISTS.getErrorMessage() + "  " + mispId);
		}
		return misp.get();
	}

	/**
	 * <p>This method brings misp license details.</p>
	 * 1. Validates the misp license key </br>
	 * 2. Validates the combination of misp id and license key </br>
	 * 3. Returns the license key for combination of misp id and license key. </br>
	 * @param mispUniqueKey {@link MISPlKeyUniqueKeyEntity } This request contains unique key of misp license(misp_id, license key)
	 * @return {@link MISPLicenseReadEntity } misp license 
	 */	
	public MISPValidatelKeyResponseDto validateLicenseKey(MISPlKeyUniqueKeyEntity mispUniqueKey) {
		MispLogger.info("Getting the misp license key " + mispUniqueKey.getLicense_key());		
		MISPValidatelKeyResponseDto response = validateLicenseKey(mispUniqueKey.getLicense_key());		
		if(!response.getMisp_id().equals(mispUniqueKey.getMisp_id())) {
			MispLogger.warn("No details found for combination of misp license key " + mispUniqueKey.getLicense_key() + 
					" and misp id" + "." + mispUniqueKey.getMisp_id());
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage() + "  MISPID: "  
			+ mispUniqueKey.getMisp_id() + ", LicenseKey: " + mispUniqueKey.getLicense_key());
		}		
		
		return response;
	}
	
	/**
	 * This method validates the license key.
	 * @param licenseKey
	 * @return
	 */
	private MISPValidatelKeyResponseDto validateLicenseKey(String licenseKey) {
		MISPValidatelKeyResponseDto response = new MISPValidatelKeyResponseDto();
		MispLogger.info("Getting the misp license key " + licenseKey);
		MISPLicenseEntity mispLicense = getLicenseDetails(licenseKey);
		
		response.setActive(mispLicense.getIsActive());
		response.setLicenseKey(mispLicense.getMispLicenseUniqueKey().getLicense_key());
		response.setValid(mispLicense.getValidToDate().isAfter(LocalDateTime.now()));
		response.setValidFrom(mispLicense.getValidFromDate());
		response.setValidTo(mispLicense.getValidToDate());
		response.setMisp_id(mispLicense.getMispLicenseUniqueKey().getMisp_id());
		String message ;
		if(response.isValid()) {
			message = "Valid";
		}else {
			message = "Expired";
		}
		response.setMessage("MISP " + mispLicense.getMispLicenseUniqueKey().getMisp_id() + " with license key " 
		+ mispLicense.getMispLicenseUniqueKey().getLicense_key() + "  is " + message);
		return response;
	}
	
	/**
	 * This method retrieves the license details with license key.
	 * @param licenseKey
	 * @return
	 */
	private MISPLicenseEntity getLicenseDetails(String licenseKey) {
		MispLogger.info("Retrieving the misp license key " + licenseKey);
		MISPLicenseEntity mispLicense = misplKeyRepository.findByLicensekey(licenseKey);
		if(mispLicense == null){
			MispLogger.warn("No details found for license key " + licenseKey);
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage() + " " + licenseKey);
		}
		
		return mispLicense;		
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
	public List<MISPDetailsDto> getMispsByOrg(String name)	{
		MispLogger.info("Finding misp details starts with " + name);
		List<MISPDetailsDto> mispDetails = new ArrayList<MISPDetailsDto>();
		List<MISPEntity> misps = mispRepository.findByStartsWithName(name);		
		if(misps.isEmpty()){
			MispLogger.warn("No details found  with name starts with " + name);
			throw new MISPException(ErrorMessages.NO_MISP_DETAILS.getErrorCode(),
					ErrorMessages.NO_MISP_DETAILS.getErrorMessage());
		}
		
		for(MISPEntity misp : misps) {
			MISPDetailsDto mispDetail = new MISPDetailsDto();
			mispDetail.setMisp(misp);
			mispDetail.setMisp_licenses(misplKeyRepository.findByMispId(misp.getID()));
			mispDetails.add(mispDetail);
		}

		return mispDetails;
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
			return null;
		}
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
	public ResponseWrapper<MISPLiceneseDto> retriveLicense(String mispId){	
		ResponseWrapper<MISPLiceneseDto> response = new ResponseWrapper<>();
		MISPLiceneseDto licenseDto = new MISPLiceneseDto();
		
		MispLogger.info("Validating the misp id " + mispId);
		MISPEntity misp = findById(mispId);
		if(!misp.status_code.toLowerCase().equals("approved")) {
			throw new MISPException(ErrorMessages.MISP_NOT_APPROVED.getErrorCode(),
					ErrorMessages.MISP_NOT_APPROVED.getErrorMessage());
		}
		if(!misp.getIsActive()) {
			throw new MISPException(ErrorMessages.MISP_IS_INACTIVE.getErrorCode(),
					ErrorMessages.MISP_IS_INACTIVE.getErrorMessage());
		}
		MispLogger.info("Getting the misp license key with misp id " + mispId);
		List<MISPLicenseEntity> mispLicenses = misplKeyRepository.findByMispId(mispId);	
		if(mispLicenses.isEmpty()) {
			throw new MISPException(ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
					ErrorMessages.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage());
		}
		MISPLicenseEntity misplEntity = new MISPLicenseEntity();
		boolean isMISPHavingActiveLicense = false;
		for(MISPLicenseEntity mispLicense : mispLicenses){
			if(mispLicense.getIsActive() && 
					mispLicense.getValidToDate().isBefore(LocalDateTime.now())){
				isMISPHavingActiveLicense = true;
				mispLicense.setIsActive(false);
				mispLicense.setUpdatedBy("SYSTEM");
				mispLicense.setUpdatedDateTime(LocalDateTime.now());
				misplKeyRepository.save(mispLicense);
				
				misplEntity = generateLicense(misp.getID());
				licenseDto.setLicenseKey(misplEntity.getMispLicenseUniqueKey().getLicense_key());
				licenseDto.setLicenseKeyExpiry(misplEntity.getValidToDate().toLocalDate().toString());
				licenseDto.setLicenseKeyStatus("Active");
				response.setResponse(licenseDto);
			}
			
			if(mispLicense.getIsActive() && 
					mispLicense.getValidToDate().isAfter(LocalDateTime.now())){		
				isMISPHavingActiveLicense = true;
				licenseDto.setLicenseKey(mispLicense.getMispLicenseUniqueKey().getLicense_key());
				licenseDto.setLicenseKeyExpiry(mispLicense.getValidToDate().toLocalDate().toString());
				licenseDto.setLicenseKeyStatus("Active");
				response.setResponse(licenseDto);
			}
		}
		if(!isMISPHavingActiveLicense) {
			throw new MISPException(ErrorMessages.MISP_LICENSE_ARE_NOT_ACTIVE.getErrorCode(),
					ErrorMessages.MISP_LICENSE_ARE_NOT_ACTIVE.getErrorMessage());
		}

		return response;
	}
	
	/**
	 * 
	 * @param email
	 * @return
	 */
	public  boolean emailValidator(String email) {
		return email.matches(emailRegex);
	}
}
