package io.mosip.pms.partner.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.NotificationsSummaryEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.repository.NotificationsSummaryRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.common.dto.ExpiryCertCountResponseDto;
import io.mosip.pms.common.dto.TrustCertTypeListRequestDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.partner.service.NotificationsService;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NotificationsServiceImpl implements NotificationsService {

    private static final Logger LOGGER = PMSLogger.getLogger(NotificationsServiceImpl.class);

    public static final String VERSION = "1.0";
    public static final String BLANK_STRING = "";


    @Value("${mosip.pms.api.id.notifications.get}")
    private String getNotificationsId;

    @Value("${mosip.pms.api.id.dismiss.notification.patch}")
    private String patchDismissNotificationId;

    @Value("${mosip.pms.api.id.expiring.certificates.count.get}")
    private String getExpiringCertCountId;

    @Autowired
    PartnerHelper partnerHelper;

    @Autowired
    PartnerServiceRepository partnerServiceRepository;

    @Autowired
    NotificationsSummaryRepository notificationsSummaryRepository;

    @Autowired
    NotificationServiceRepository notificationServiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> getNotifications(Integer pageNo, Integer pageSize, NotificationsFilterDto filterDto) {
        ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            PageResponseV2Dto<NotificationsResponseDto> pageResponseV2Dto = new PageResponseV2Dto<>();
            boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
            String notificationType = filterDto.getNotificationType();
            if(!isPartnerAdmin) {
                if(Objects.nonNull(notificationType) && !notificationType.equals(BLANK_STRING)) {
                    if ((notificationType.equalsIgnoreCase(PartnerConstants.ROOT) || notificationType.equalsIgnoreCase(PartnerConstants.INTERMEDIATE) || notificationType.equalsIgnoreCase(PartnerConstants.WEEKLY))) {
                        throw new PartnerServiceException(ErrorCode.UNABLE_TO_GET_NOTIFICATIONS.getErrorCode(),
                                ErrorCode.UNABLE_TO_GET_NOTIFICATIONS.getErrorMessage());
                    }
                }
            }
            List<String> partnerIdList = new ArrayList<>();
            String userId = getUserId();
            List<Partner> partnerList = partnerServiceRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
            for (Partner partner : partnerList) {
                partnerHelper.validatePartnerId(partner, userId);
                if (partner.getIsActive()) {
                    partnerIdList.add(partner.getId());
                }
            }
            Pageable pageable = PageRequest.of(pageNo, pageSize);

            Page<NotificationsSummaryEntity> page = notificationsSummaryRepository.getSummaryOfAllNotifications(filterDto.getFilterBy(), filterDto.getNotificationStatus(),
                    filterDto.getNotificationType(), partnerIdList, pageable);
            if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
                List<NotificationsResponseDto> notificationsResponseDtoList = page.getContent().stream()
                        .map(this::mapToResponseDto)
                        .collect(Collectors.toList());
                pageResponseV2Dto.setPageNo(page.getNumber());
                pageResponseV2Dto.setPageSize(page.getSize());
                pageResponseV2Dto.setTotalResults(page.getTotalElements());
                pageResponseV2Dto.setData(notificationsResponseDtoList);
            }
            responseWrapper.setResponse(pageResponseV2Dto);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getNotifications method of NotificationsServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getNotifications method of NotificationsServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.FETCH_ALL_NOTIFICATIONS_ERROR.getErrorCode();
            String errorMessage = ErrorCode.FETCH_ALL_NOTIFICATIONS_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getNotificationsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    @Override
    public ResponseWrapperV2<DismissNotificationResponseDto> dismissNotification(String notificationId, DismissNotificationRequestDto dismissNotificationRequestDto) {
        ResponseWrapperV2<DismissNotificationResponseDto> responseWrapper = new ResponseWrapperV2<>();
        try {
            if (Objects.isNull(notificationId) || notificationId.isBlank()) {
                LOGGER.info("Invalid request: Notification ID is null or empty");
                throw new PartnerServiceException(
                        ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_PARAM.getErrorMessage());
            }
            if (!dismissNotificationRequestDto.getNotificationStatus().equals(PartnerConstants.STATUS_DISMISSED)){
                LOGGER.info("Invalid Notification status for Notification Id: {}", notificationId);
                throw new PartnerServiceException(
                        ErrorCode.INVALID_NOTIFICATION_STATUS.getErrorCode(),
                        ErrorCode.INVALID_NOTIFICATION_STATUS.getErrorMessage());
            }
            Optional<NotificationEntity> optionalNotification = notificationServiceRepository.findById(notificationId);
            if (optionalNotification.isEmpty()) {
                LOGGER.info("Notification does not exist: {}", notificationId);
                throw new PartnerServiceException(
                        ErrorCode.NOTIFICATION_NOT_EXISTS.getErrorCode(),
                        ErrorCode.NOTIFICATION_NOT_EXISTS.getErrorMessage());
            }

            NotificationEntity notificationEntity = optionalNotification.get();

            String userId = getUserId();
            List<Partner> partnerList = partnerServiceRepository.findByUserId(userId);

            if (partnerList.isEmpty()) {
                LOGGER.info("User ID does not exist: {}", userId);
                throw new PartnerServiceException(
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }

            String notificationPartnerId = notificationEntity.getPartnerId();
            boolean partnerIdExists = false;

            // check if partnerId is associated with user
            for (Partner partner : partnerList) {
                partnerHelper.validatePartnerId(partner, userId);
                if (partner.getId().equals(notificationPartnerId)) {
                    //check if partner is active or not
                    partnerHelper.checkIfPartnerIsNotActive(partner);
                    partnerIdExists = true;
                    break;
                }
            }

            if (!partnerIdExists) {
                LOGGER.info("Notification does not belong to the partner: {}", notificationId);
                throw new PartnerServiceException(
                        ErrorCode.NOTIFICATION_NOT_BELONGS_TO_PARTNER.getErrorCode(),
                        ErrorCode.NOTIFICATION_NOT_BELONGS_TO_PARTNER.getErrorMessage());
            }

            if (PartnerConstants.STATUS_DISMISSED.equals(notificationEntity.getNotificationStatus())) {
                LOGGER.info("Notification already dismissed: {}", notificationId);
                throw new PartnerServiceException(
                        ErrorCode.NOTIFICATION_ALREADY_DISMISSED.getErrorCode(),
                        ErrorCode.NOTIFICATION_ALREADY_DISMISSED.getErrorMessage());
            }

            // Update notification status
            notificationEntity.setNotificationStatus(PartnerConstants.STATUS_DISMISSED);
            notificationEntity.setUpdatedDatetime(LocalDateTime.now());
            notificationEntity.setUpdatedBy(getUserBy());

            NotificationEntity savedEntity = notificationServiceRepository.save(notificationEntity);

            DismissNotificationResponseDto responseDto = objectMapper.convertValue(savedEntity, DismissNotificationResponseDto.class);
            responseWrapper.setResponse(responseDto);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In dismissNotification method of NotificationsServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In dismissNotification method of NotificationsServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.DISMISS_NOTIFICATION_ERROR.getErrorCode();
            String errorMessage = ErrorCode.DISMISS_NOTIFICATION_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(patchDismissNotificationId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    private String getUserBy() {
        return authUserDetails().getMail();
    }

    public NotificationsResponseDto mapToResponseDto(NotificationsSummaryEntity summaryEntity) {
        NotificationsResponseDto responseDto = new NotificationsResponseDto();
        responseDto.setNotificationId(summaryEntity.getNotificationId());
        responseDto.setNotificationPartnerId(summaryEntity.getNotificationPartnerId());
        responseDto.setNotificationType(summaryEntity.getNotificationType());
        responseDto.setNotificationStatus(summaryEntity.getNotificationStatus());
        responseDto.setCreatedDateTime(summaryEntity.getCreatedDateTime());

        // Convert JSON string to NotificationDetailsDto
        if (summaryEntity.getNotificationDetails() != null) {
            try {
                NotificationDetailsDto detailsDto = objectMapper.readValue(
                        summaryEntity.getNotificationDetails(), NotificationDetailsDto.class);
                responseDto.setNotificationDetails(detailsDto);
            } catch (JsonProcessingException e) {
                throw new PartnerServiceException(ErrorCode.NOTIFICATION_DETAILS_JSON_ERROR.getErrorCode(),
                        ErrorCode.NOTIFICATION_DETAILS_JSON_ERROR.getErrorMessage());
            }
        }

        return responseDto;
    }

    @Override
    public ResponseWrapperV2<ExpiryCertCountResponseDto> getExpiringCertsCount(Integer period, String type) {
        ResponseWrapperV2<ExpiryCertCountResponseDto> responseWrapper = new ResponseWrapperV2<>();
        try {
            if (Objects.isNull(period) || Objects.isNull(type) || type.equals(BLANK_STRING)) {
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_PARAM.getErrorMessage());
            }
            boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
            if ((type.equals(PartnerConstants.ROOT) || type.equals(PartnerConstants.INTERMEDIATE))) {
                if (isPartnerAdmin) {
                    TrustCertTypeListRequestDto trustCertTypeListRequestDto = new TrustCertTypeListRequestDto();
                    trustCertTypeListRequestDto.setCaCertificateType(type);
                    trustCertTypeListRequestDto.setExcludeMosipCA(true);

                    LocalDate validTillDate = LocalDate.now().plusDays(period);
                    LocalTime validTillTime = LocalTime.MAX;
                    LocalDateTime validTillDateTime = LocalDateTime.of(validTillDate, validTillTime);
                    trustCertTypeListRequestDto.setValidTillDate(validTillDateTime);
                    TrustCertTypeListResponseDto responseObject = partnerHelper.getTrustCertificatesList(trustCertTypeListRequestDto);
                     ExpiryCertCountResponseDto responseDto = new ExpiryCertCountResponseDto();
                     responseDto.setCertificateType(type);
                     responseDto.setExpiryPeriod(period);
                     responseDto.setCount(responseObject.getTotalRecords());
                     responseWrapper.setResponse(responseDto);
                } else {
                    throw new PartnerServiceException(ErrorCode.UNABLE_TO_GET_EXPIRING_CERTS_COUNT.getErrorCode(),
                            ErrorCode.UNABLE_TO_GET_EXPIRING_CERTS_COUNT.getErrorMessage());
                }
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getExpiringCertsCount method of NotificationsServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getExpiringCertsCount method of NotificationsServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.EXPIRING_CERT_COUNT_FETCH_ERROR.getErrorCode();
            String errorMessage = ErrorCode.EXPIRING_CERT_COUNT_FETCH_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(getExpiringCertCountId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserId() {
        String userId = authUserDetails().getUserId();
        return userId;
    }
}
