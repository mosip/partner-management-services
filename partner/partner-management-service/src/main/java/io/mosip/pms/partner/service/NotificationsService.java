package io.mosip.pms.partner.service;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface NotificationsService {

    public ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> getNotifications(Integer pageNo, Integer pageSize, NotificationsFilterDto filterDto);
}
