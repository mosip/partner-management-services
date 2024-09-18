package io.mosip.pms.user.service.impl;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.UserDetails;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.UserDetailsRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.UserDetailsDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.service.impl.MultiPartnerServiceImpl;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.partner.keycloak.service.KeycloakImpl;
import io.mosip.pms.user.service.UserManagementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserManagementServiceImpl implements UserManagementService{

	private static final Logger LOGGER = PMSLogger.getLogger(UserManagementServiceImpl.class);
	public static final String YES = "YES";
	public static final String VERSION = "1.0";

	@Value("${mosip.pms.api.id.user.consent.post}")
	private String postSaveUserConsentGivenId;

	@Value("${mosip.pms.api.id.user.consent.get}")
	private String getUserConsentGivenId;

	@Autowired
	KeycloakImpl keycloakService;

	@Autowired
	UserDetailsRepository userDetailsRepository;

	@Autowired
	PartnerServiceRepository partnerRepository;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getUserId() {
		String userId = authUserDetails().getUserId();
		return userId;
	}

	private String getUserBy() {
		String crBy = authUserDetails().getMail();
		return crBy;
	}

	@Override
	public MosipUserDto registerUser(UserRegistrationRequestDto userToBeRegistred) {
		return keycloakService.registerUser(userToBeRegistred);
	}

	@Override
	public ResponseWrapperV2<UserDetailsDto> saveUserConsent() {
		ResponseWrapperV2<UserDetailsDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (!partnerList.isEmpty()) {
				UserDetails userDetails = new UserDetails();

				LocalDateTime nowDate = LocalDateTime.now();
				userDetails.setConsentGiven(YES);
				userDetails.setConsentGivenDtimes(nowDate);

				Optional<UserDetails> optionalEntity = userDetailsRepository.findByUserId(userId);
				if (optionalEntity.isPresent()) {
					UserDetails entity = optionalEntity.get();
					userDetails.setId(entity.getId());
					userDetails.setUpdBy(this.getUserBy());
					userDetails.setUpdDtimes(nowDate);
					userDetails.setCrBy(entity.getCrBy());
					userDetails.setCrDtimes(entity.getCrDtimes());
					userDetails.setUserId(entity.getUserId());
				} else {
					userDetails.setId(PartnerUtil.generateUUID("id", "", 36));
					userDetails.setCrBy(this.getUserBy());
					userDetails.setCrDtimes(nowDate);
					userDetails.setUserId(userId);
				}
				UserDetails respEntity = userDetailsRepository.save(userDetails);
				LOGGER.info("sessionId", "idType", "id", "saving user consent data for user id : ", userId);

				UserDetailsDto userDetailsDto = new UserDetailsDto();
				userDetailsDto.setConsentGiven(true);
				userDetailsDto.setUserId(respEntity.getUserId());
				userDetailsDto.setConsentGivenDateTime(respEntity.getConsentGivenDtimes());

				responseWrapper.setResponse(userDetailsDto);
			} else {
				LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In saveUserConsentGiven method of UserManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception e) {
			LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
			LOGGER.error("sessionId", "idType", "id", "In saveUserConsentGiven method of UserManagementServiceImpl - " + e.getMessage());
			String errorCode = ErrorCode.PMS_CONSENT_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ErrorCode.PMS_CONSENT_UNABLE_TO_ADD.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(postSaveUserConsentGivenId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<UserDetailsDto> isUserConsentGiven() {
		ResponseWrapperV2<UserDetailsDto> responseWrapper =  new ResponseWrapperV2<>();
		try {
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (!partnerList.isEmpty()) {
				UserDetailsDto userDetailsDto = new UserDetailsDto();
				userDetailsDto.setUserId(userId);
				LOGGER.info("sessionId", "idType", "id", "fetching consent status from db for user :", userId);
				Optional<UserDetails> optionalEntity = userDetailsRepository.findByUserId(userId);
				if (optionalEntity.isPresent()) {
					UserDetails entity = optionalEntity.get();
					if (entity.getConsentGiven().equals(YES)) {
						userDetailsDto.setConsentGiven(true);
						userDetailsDto.setConsentGivenDateTime(entity.getConsentGivenDtimes());
					}
				}
				responseWrapper.setResponse(userDetailsDto);
			} else {
				LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In isUserConsentGiven method of UserManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception e) {
			LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
			LOGGER.error("sessionId", "idType", "id", "In isUserConsentGiven method of UserManagementServiceImpl - " + e.getMessage());
			String errorCode = ErrorCode.PMS_CONSENT_ERR.getErrorCode();
			String errorMessage = ErrorCode.PMS_CONSENT_ERR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getUserConsentGivenId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

}
