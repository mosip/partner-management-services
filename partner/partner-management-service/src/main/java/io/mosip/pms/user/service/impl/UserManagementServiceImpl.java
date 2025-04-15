package io.mosip.pms.user.service.impl;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.NotificationsSeenRequestDto;
import io.mosip.pms.common.dto.NotificationsSeenResponseDto;
import io.mosip.pms.common.entity.UserDetails;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.UserDetailsRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.UserDetailsDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
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
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
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

	@Value("${mosip.pms.api.id.users.notifications.seen.timestamp.put}")
	private String putNotificationsSeenTimestampId;

	@Value("${mosip.pms.api.id.users.notifications.seen.timestamp.get}")
	private String getNotificationsSeenTimestampId;

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
        return authUserDetails().getUserId();
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
					userDetails.setUpdBy(this.getUserId());
					userDetails.setUpdDtimes(nowDate);
					userDetails.setCrBy(entity.getCrBy());
					userDetails.setCrDtimes(entity.getCrDtimes());
					userDetails.setUserId(entity.getUserId());
				} else {
					userDetails.setId(PartnerUtil.generateUUID("id", "", 36));
					userDetails.setCrBy(this.getUserId());
					userDetails.setCrDtimes(nowDate);
					userDetails.setUserId(userId);
					userDetails.setNotificationsSeenDtimes(null);
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

	@Override
	public ResponseWrapperV2<NotificationsSeenResponseDto> updateNotificationsSeenTimestamp(String userId, NotificationsSeenRequestDto requestDto) {
		ResponseWrapperV2<NotificationsSeenResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (partnerList.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			LOGGER.info("sessionId", "idType", "id", "updating notification seen timestamp for user :", userId);
			Optional<UserDetails> optionalEntity = userDetailsRepository.findByUserId(userId);
			if (optionalEntity.isPresent()) {
				UserDetails entity = optionalEntity.get();
				if (Objects.nonNull(requestDto.getNotificationsSeenDtimes())) {
					entity.setNotificationsSeenDtimes(requestDto.getNotificationsSeenDtimes());
					entity.setUpdBy(userId);
					entity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
					UserDetails savedEntity = userDetailsRepository.save(entity);

					NotificationsSeenResponseDto responseDto = new NotificationsSeenResponseDto();
					responseDto.setNotificationsSeenDtimes(savedEntity.getNotificationsSeenDtimes());
					responseWrapper.setResponse(responseDto);
				} else {
					LOGGER.info("sessionId", "idType", "id", "Unable to update notifications seen date time.");
					throw new PartnerServiceException(ErrorCode.UNABLE_TO_UPDATE_NOTIFICATIONS_SEEN_TIME.getErrorCode(),
							ErrorCode.UNABLE_TO_UPDATE_NOTIFICATIONS_SEEN_TIME.getErrorMessage());
				}
			}
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In updateNotificationsSeenTimestamp method of UserManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception e) {
			LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
			LOGGER.error("sessionId", "idType", "id", "In updateNotificationsSeenTimestamp method of UserManagementServiceImpl - " + e.getMessage());
			String errorCode = ErrorCode.UPDATE_NOTIFICATIONS_SEEN_TIME_ERROR.getErrorCode();
			String errorMessage = ErrorCode.UPDATE_NOTIFICATIONS_SEEN_TIME_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(putNotificationsSeenTimestampId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<NotificationsSeenResponseDto> getNotificationsSeenTimestamp(String userId) {
		ResponseWrapperV2<NotificationsSeenResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (partnerList.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			LOGGER.info("sessionId", "idType", "id", "fetching notification seen timestamp for user :", userId);
			Optional<UserDetails> optionalEntity = userDetailsRepository.findByUserId(userId);
			if (optionalEntity.isPresent()) {
				UserDetails entity = optionalEntity.get();

				NotificationsSeenResponseDto responseDto = new NotificationsSeenResponseDto();
				responseDto.setNotificationsSeenDtimes(entity.getNotificationsSeenDtimes());
				responseWrapper.setResponse(responseDto);
			} else {
				LOGGER.info("sessionId", "idType", "id", "User details not exists.");
				throw new PartnerServiceException(ErrorCode.USER_DETAILS_NOT_EXIST.getErrorCode(),
						ErrorCode.USER_DETAILS_NOT_EXIST.getErrorMessage());
			}
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getNotificationsSeenTimestamp method of UserManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception e) {
			LOGGER.debug("sessionId", "idType", "id", e.getStackTrace());
			LOGGER.error("sessionId", "idType", "id", "In getNotificationsSeenTimestamp method of UserManagementServiceImpl - " + e.getMessage());
			String errorCode = ErrorCode.GET_NOTIFICATIONS_SEEN_TIME_ERROR.getErrorCode();
			String errorMessage = ErrorCode.GET_NOTIFICATIONS_SEEN_TIME_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getNotificationsSeenTimestampId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

}
