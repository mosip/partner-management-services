package io.mosip.pms.partner.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.entity.NotificationsSummaryEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationsSummaryRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.common.dto.NotificationsResponseDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NotificationsServiceImpl implements NotificationsService {

    private static final Logger LOGGER = PMSLogger.getLogger(NotificationsServiceImpl.class);

    public static final String VERSION = "1.0";
    public static final String BLANK_STRING = "";


    @Value("${mosip.pms.api.id.notifications.get}")
    private String getNotificationsId;

    @Autowired
    PartnerHelper partnerHelper;

    @Autowired
    PartnerServiceRepository partnerServiceRepository;

    @Autowired
    NotificationsSummaryRepository notificationsSummaryRepository;

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

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserId() {
        String userId = authUserDetails().getUserId();
        return userId;
    }
}
