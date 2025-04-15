package io.mosip.pms.partner.service.impl;

import static io.mosip.pms.common.constant.PartnerConstants.API_KEY;
import static io.mosip.pms.common.constant.PartnerConstants.FTM_CHIP;
import static io.mosip.pms.common.constant.PartnerConstants.INTERMEDIATE;
import static io.mosip.pms.common.constant.PartnerConstants.INTERMEDIATE_CERT_EXPIRY;
import static io.mosip.pms.common.constant.PartnerConstants.PARTNER;
import static io.mosip.pms.common.constant.PartnerConstants.PARTNER_CERT_EXPIRY;
import static io.mosip.pms.common.constant.PartnerConstants.ROOT;
import static io.mosip.pms.common.constant.PartnerConstants.ROOT_CERT_EXPIRY;
import static io.mosip.pms.common.constant.PartnerConstants.SBI;
import static io.mosip.pms.common.constant.PartnerConstants.WEEKLY;
import static io.mosip.pms.common.constant.PartnerConstants.WEEKLY_SUMMARY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.repository.NotificationsSummaryRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.service.NotificationsService;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;

@Service
public class NotificationsServiceImpl implements NotificationsService {

    private static final Logger LOGGER = PMSLogger.getLogger(NotificationsServiceImpl.class);

    public static final String VERSION = "1.0";
    public static final String BLANK_STRING = "";


    @Value("${mosip.pms.api.id.notifications.get}")
    private String getNotificationsId;

    @Value("${mosip.pms.api.id.dismiss.notification.patch}")
    private String patchDismissNotificationId;

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
    public ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> getNotifications(
            Integer pageNo, Integer pageSize, NotificationsFilterDto filterDto) {
        ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> responseWrapper = new ResponseWrapperV2<>();
        PageResponseV2Dto<NotificationsResponseDto> pageResponseV2Dto = new PageResponseV2Dto<>();

        try {
            boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
            // Validate expiry date
            if (Objects.nonNull(filterDto.getExpiryDate())) {
                validateDate(filterDto.getExpiryDate(), "expiryDate");
            }
            if (Objects.nonNull(filterDto.getCreatedFromDate())) {
                validateDate(filterDto.getCreatedFromDate(), "createdFromDate");
            }
            if (Objects.nonNull(filterDto.getCreatedToDate())) {
                validateDate(filterDto.getCreatedToDate(), "createdToDate");
            }
            if (filterDto.getNotificationType() != null) {
                String filterNotificationType = filterDto.getNotificationType();

                // Validate notificationType access for non-admin users
                if (!isPartnerAdmin && !filterNotificationType.isBlank() && validateNotificationTypeForPartner(filterNotificationType)) {
                    throw new PartnerServiceException(ErrorCode.UNABLE_TO_GET_NOTIFICATIONS.getErrorCode(),
                            ErrorCode.UNABLE_TO_GET_NOTIFICATIONS.getErrorMessage());
                }
                if ((Objects.nonNull(filterDto.getCreatedFromDate()) || Objects.nonNull(filterDto.getCreatedToDate())) && !filterNotificationType.equals(WEEKLY)) {
                    throw new PartnerServiceException(ErrorCode.WEEKLY_TYPE_NOT_SELECTED.getErrorCode(),
                            ErrorCode.WEEKLY_TYPE_NOT_SELECTED.getErrorMessage());
                }
            } else {
                if (Objects.nonNull(filterDto.getCertificateId()) || Objects.nonNull(filterDto.getIssuedBy()) || Objects.nonNull(filterDto.getIssuedTo()) ||
                    Objects.nonNull(filterDto.getExpiryDate()) || Objects.nonNull(filterDto.getPartnerDomain()) || Objects.nonNull(filterDto.getCreatedFromDate()) || Objects.nonNull(filterDto.getCreatedToDate())) {
                    throw new PartnerServiceException(ErrorCode.NOTIFICATION_TYPE_NOT_SELECTED.getErrorCode(),
                            ErrorCode.NOTIFICATION_TYPE_NOT_SELECTED.getErrorMessage());
                }
            }

            // Fetch and validate partner list
            String userId = getUserId();
            List<Partner> partnerList = partnerServiceRepository.findByUserId(userId);
            if (partnerList.isEmpty()) {
                LOGGER.info("sessionId", "idType", "id", "User ID does not exist.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }

            // Filter active partners
            List<String> partnerIdList = new ArrayList<>();
            for (Partner partner : partnerList) {
                partnerHelper.validatePartnerId(partner, userId);
                if (isPartnerAdmin || partner.getIsActive()) {
                    partnerIdList.add(partner.getId());
                }
            }

            Pageable pageable = PageRequest.of(pageNo, pageSize);

            // Fetch notifications
            Page<NotificationEntity> page = fetchNotifications(filterDto, pageable, partnerIdList);

            if (page != null && !page.getContent().isEmpty()) {
                pageResponseV2Dto.setPageNo(page.getNumber());
                pageResponseV2Dto.setPageSize(page.getSize());
                pageResponseV2Dto.setTotalResults(page.getTotalElements());
                pageResponseV2Dto.setData(page.getContent().stream()
                        .map(this::mapToResponseDto)
                        .collect(Collectors.toList()));
            }

            responseWrapper.setResponse(pageResponseV2Dto);
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id",
                    "In getNotifications method of NotificationsServiceImpl - {}", ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.error("sessionId", "idType", "id",
                    "Unexpected error in getNotifications method of NotificationsServiceImpl - {}", ex.getMessage(), ex);
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(
                    ErrorCode.FETCH_ALL_NOTIFICATIONS_ERROR.getErrorCode(),
                    ErrorCode.FETCH_ALL_NOTIFICATIONS_ERROR.getErrorMessage()));
        }
        responseWrapper.setId(getNotificationsId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    public Page<NotificationEntity> fetchNotifications(NotificationsFilterDto filterDto, Pageable pageable, List<String> partnerIdList) {

        String notificationType = filterDto.getNotificationType();

        // Default case when notificationType is null
        if (Objects.isNull(notificationType)) {
            return notificationsSummaryRepository.getSummaryOfAllNotifications(
                    filterDto.getNotificationStatus(), partnerIdList, pageable);
        }

        switch (notificationType) {
            case ROOT:
                return notificationsSummaryRepository.getSummaryOfAllRootIntermediatePartnerCertNotifications(
                        filterDto.getCertificateId(), filterDto.getIssuedBy(), filterDto.getIssuedTo(),
                        filterDto.getPartnerDomain(), filterDto.getExpiryDate(), filterDto.getNotificationStatus(),
                        ROOT_CERT_EXPIRY, partnerIdList, pageable);

            case INTERMEDIATE:
                return notificationsSummaryRepository.getSummaryOfAllRootIntermediatePartnerCertNotifications(
                        filterDto.getCertificateId(), filterDto.getIssuedBy(), filterDto.getIssuedTo(),
                        filterDto.getPartnerDomain(), filterDto.getExpiryDate(), filterDto.getNotificationStatus(),
                        INTERMEDIATE_CERT_EXPIRY, partnerIdList, pageable);

            // TODO: Logic for WEEKLY notifications to be implemented
            case WEEKLY:
                return notificationsSummaryRepository.getSummaryOfWeeklyNotifications(
                        filterDto.getCreatedFromDate(), filterDto.getCreatedToDate(),
                        filterDto.getNotificationStatus(), WEEKLY_SUMMARY, partnerIdList, pageable);

            // TODO: Logic for PARTNER notifications to be implemented
            case PARTNER:
                return notificationsSummaryRepository.getSummaryOfAllRootIntermediatePartnerCertNotifications(
                        filterDto.getCertificateId(), filterDto.getIssuedBy(), filterDto.getIssuedTo(),
                        filterDto.getPartnerDomain(), filterDto.getExpiryDate(), filterDto.getNotificationStatus(),
                        PARTNER_CERT_EXPIRY, partnerIdList, pageable);
            case SBI:

            // TODO: Logic for FTM_CHIP notifications to be implemented
            case FTM_CHIP:

           // TODO: Logic for API_KEY notifications to be implemented
            case API_KEY:

            default:
                return Page.empty(pageable); // Return empty paginated response
        }
    }

    private boolean validateNotificationTypeForPartner(String notificationType) {
        return notificationType.equalsIgnoreCase(PartnerConstants.ROOT) ||
                notificationType.equalsIgnoreCase(PartnerConstants.INTERMEDIATE) ||
                notificationType.equalsIgnoreCase(PartnerConstants.WEEKLY);
    }

    private void validateDate(String date, String fieldName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new PartnerServiceException(ErrorCode.INVALID_DATE.getErrorCode(),
                    String.format(ErrorCode.INVALID_DATE.getErrorMessage(), fieldName));
        }
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
            if(Objects.isNull(dismissNotificationRequestDto.getNotificationStatus())) {
                LOGGER.info("Notification status is null or empty");
                throw new PartnerServiceException(
                        ErrorCode.NOTIFICATION_STATUS_REQUIRED_IN_REQUEST.getErrorCode(),
                        ErrorCode.NOTIFICATION_STATUS_REQUIRED_IN_REQUEST.getErrorMessage());
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
            boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
            if (notificationEntity.getNotificationType().equals(ROOT_CERT_EXPIRY) || notificationEntity.getNotificationType().equals(INTERMEDIATE_CERT_EXPIRY)
                    || notificationEntity.getNotificationType().equals(WEEKLY_SUMMARY)) {
                if(!isPartnerAdmin) {
                    LOGGER.info("Partner Admin can only dismiss this notifications: {}", notificationId);
                    throw new PartnerServiceException(
                            ErrorCode.UNABLE_TO_DISMISS.getErrorCode(),
                            ErrorCode.UNABLE_TO_DISMISS.getErrorMessage());
                }
            }

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
                    if(!isPartnerAdmin) {
                        partnerHelper.checkIfPartnerIsNotActive(partner);
                    }
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

    public NotificationsResponseDto mapToResponseDto(NotificationEntity notificationEntity) {
        NotificationsResponseDto responseDto = new NotificationsResponseDto();
        responseDto.setNotificationId(notificationEntity.getId());
        responseDto.setNotificationPartnerId(notificationEntity.getPartnerId());
        responseDto.setNotificationType(notificationEntity.getNotificationType());
        responseDto.setNotificationStatus(notificationEntity.getNotificationStatus());
        responseDto.setCreatedDateTime(notificationEntity.getCreatedDatetime());

        // Convert JSON string to NotificationDetailsDto
        if (notificationEntity.getNotificationDetailsJson() != null) {
            try {
                NotificationDetailsDto detailsDto = objectMapper.readValue(
                        notificationEntity.getNotificationDetailsJson(), NotificationDetailsDto.class);
                responseDto.setNotificationDetails(detailsDto);
            } catch (JsonProcessingException e) {
                throw new PartnerServiceException(ErrorCode.NOTIFICATION_DETAILS_JSON_ERROR.getErrorCode(),
                        ErrorCode.NOTIFICATION_DETAILS_JSON_ERROR.getErrorMessage());
            }
        }

        return responseDto;
    }

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserId() {
        String userId = authUserDetails().getUserId();
        return userId;
    }
}
