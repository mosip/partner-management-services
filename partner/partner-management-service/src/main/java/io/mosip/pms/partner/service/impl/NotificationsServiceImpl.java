package io.mosip.pms.partner.service.impl;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.entity.NotificationsSummaryEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationsSummaryRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.partner.response.dto.NotificationsResponseDto;
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

@Service
public class NotificationsServiceImpl implements NotificationsService {

    private static final Logger LOGGER = PMSLogger.getLogger(NotificationsServiceImpl.class);

    public static final String VERSION = "1.0";

    public static final String ROOT_CERT_EXPIRY = "root_cert_expiry";
    public static final String INTERMEDIATE_CERT_EXPIRY = "intermediate_cert_expiry";
    public static final String WEEKLY_SUMMARY = "weekly_summary";
    public static final String BLANK_STRING = "";


    @Value("${mosip.pms.api.id.notifications.get}")
    private String getNotificationsId;

    @Autowired
    PartnerHelper partnerHelper;

    @Autowired
    PartnerServiceRepository partnerServiceRepository;

    @Autowired
    NotificationsSummaryRepository notificationsSummaryRepository;

    @Override
    public ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> getNotifications(Integer pageNo, Integer pageSize, NotificationsFilterDto filterDto) {
        ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> responseWrapper = new ResponseWrapperV2<>();
        try {
            PageResponseV2Dto<NotificationsResponseDto> pageResponseV2Dto = new PageResponseV2Dto<>();
            boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
            String notificationType = filterDto.getNotificationType();
            if(!isPartnerAdmin) {
                if(Objects.isNull(notificationType) || notificationType.equals(BLANK_STRING)) {
                    throw new PartnerServiceException(ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorCode(),
                            ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorMessage());
                }
                if ((notificationType.equalsIgnoreCase(ROOT_CERT_EXPIRY) || notificationType.equalsIgnoreCase(INTERMEDIATE_CERT_EXPIRY) || notificationType.equalsIgnoreCase(WEEKLY_SUMMARY))) {
                    throw new PartnerServiceException(ErrorCode.UNABLE_TO_GET_NOTIFICATIONS.getErrorCode(),
                            ErrorCode.UNABLE_TO_GET_NOTIFICATIONS.getErrorMessage());
                }
            }
            List<String> partnerIdList = null;
            if (!isPartnerAdmin) {
                String userId = getUserId();
                List<Partner> partnerList = partnerServiceRepository.findByUserId(userId);
                if (partnerList.isEmpty()) {
                    LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                    throw new PartnerServiceException(io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                            io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
                }
                partnerIdList = new ArrayList<>();
                for (Partner partner : partnerList) {
                    partnerHelper.validatePartnerId(partner, userId);
                    partnerIdList.add(partner.getId());
                }
            }
            Pageable pageable = PageRequest.of(pageNo, pageSize);

            Page<NotificationsSummaryEntity> page = notificationsSummaryRepository.getSummaryOfAllNotifications(filterDto.getFilterBy(), filterDto.getNotificationStatus(),
                    filterDto.getNotificationType(), partnerIdList, isPartnerAdmin, pageable);
            if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
                List<NotificationsResponseDto> notificationsResponseDtoList = MapperUtils.mapAll(page.getContent(), NotificationsResponseDto.class);
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

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserId() {
        String userId = authUserDetails().getUserId();
        return userId;
    }
}
