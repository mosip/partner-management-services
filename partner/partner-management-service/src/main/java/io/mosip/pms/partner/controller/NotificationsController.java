package io.mosip.pms.partner.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.partner.service.NotificationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class NotificationsController {

    public static final Logger LOGGER = PMSLogger.getLogger(NotificationsController.class);

    @Autowired
    NotificationsService notificationsService;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetnotifications())")
    @GetMapping(value = "/notifications")
    @Operation(summary = "This endpoint retrieves a list of all notifications.",
            description = "Available since release-1.3.0-beta.1. This endpoint supports pagination, sorting, and filtering.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> getNotifications(
            @RequestParam(value = "filterBy", required = false) String filterBy,
            @RequestParam(value = "notificationStatus", defaultValue = "ACTIVE") String notificationStatus,
            @RequestParam(value = "notificationType", required = false) String notificationType,
            @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize
    ) {
        validatePaginationParams(pageNo, pageSize);
        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        if (filterBy != null) {
            filterDto.setFilterBy(filterBy.toLowerCase());
        }
        if (notificationStatus != null) {
            filterDto.setNotificationStatus(notificationStatus.toLowerCase());
        }
        if (notificationType != null) {
            filterDto.setNotificationType(notificationType.toLowerCase());
        }
        return notificationsService.getNotifications(pageNo, pageSize, filterDto);
    }

    public void validatePaginationParams(Integer pageNo, Integer pageSize) {
        // Validate pageNo and pageSize
        if ((Objects.nonNull(pageNo) && Objects.isNull(pageSize)) || (Objects.isNull(pageNo) && Objects.nonNull(pageSize))) {
            LOGGER.error("Both pageNo and pageSize must be provided together.");
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_PARAMETERS.getErrorCode(),
                    ErrorCode.INVALID_PAGE_PARAMETERS.getErrorMessage());
        }

        // Validate pageNo
        if (Objects.nonNull(pageNo) && pageNo < 0) {
            LOGGER.error("Invalid page no: " + pageNo);
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_NO.getErrorCode(),
                    ErrorCode.INVALID_PAGE_NO.getErrorMessage());
        }

        // Validate pageSize
        if (Objects.nonNull(pageSize) && pageSize <= 0) {
            LOGGER.error("Invalid page size: " + pageSize);
            throw new PartnerServiceException(ErrorCode.INVALID_PAGE_SIZE.getErrorCode(),
                    ErrorCode.INVALID_PAGE_SIZE.getErrorMessage());
        }
    }
}
