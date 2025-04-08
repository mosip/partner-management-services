package io.mosip.pms.partner.service;

import org.springframework.stereotype.Service;

import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.NotificationsFilterDto;

@Service
public interface NotificationsService {

    public ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> getNotifications(Integer pageNo, Integer pageSize, NotificationsFilterDto filterDto);

    public ResponseWrapperV2<DismissNotificationResponseDto> dismissNotification(String notificationId, DismissNotificationRequestDto dismissNotificationRequestDto);
}
