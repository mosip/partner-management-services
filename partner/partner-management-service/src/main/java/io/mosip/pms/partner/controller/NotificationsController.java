package io.mosip.pms.partner.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RequestValidator;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.common.dto.ExpiryCertCountResponseDto;
import io.mosip.pms.partner.service.NotificationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
public class NotificationsController {

    public static final Logger LOGGER = PMSLogger.getLogger(NotificationsController.class);

    @Value("${mosip.pms.api.id.dismiss.notification.patch}")
    private String patchDismissNotificationId;

    @Autowired
    NotificationsService notificationsService;

    @Autowired
    RequestValidator requestValidator;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetnotifications())")
    @GetMapping(value = "/notifications")
    @Operation(summary = "This endpoint retrieves a list of all notifications.",
            description = "Available since release-1.3.0. This endpoint supports pagination, sorting, and filtering.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> getNotifications(
            @Parameter(
                    description = "Status of notifications",
                    in = ParameterIn.QUERY,
                    schema = @Schema(allowableValues = {"active", "dismissed"})
            )
            @RequestParam(value = "notificationStatus", required = false) String notificationStatus,
            @Parameter(
                    description = "Type of notifications",
                    in = ParameterIn.QUERY,
                    schema = @Schema(allowableValues = {"root", "intermediate", "partner", "weekly", "sbi", "ftm-chip", "apikey"})
            )
            @RequestParam(value = "notificationType", required = false) String notificationType,
            @RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
            @RequestParam(value = "certificateId", required = false) String certificateId,
            @RequestParam(value = "expiryDate", required = false)
            @Parameter(description = "Expiry date in 'yyyy-MM-dd' format") String expiryDate,
            @RequestParam(value = "issuedBy", required = false) String issuedBy,
            @RequestParam(value = "issuedTo", required = false) String issuedTo,
            @Parameter(
                    description = "Type of partner domain",
                    in = ParameterIn.QUERY,
                    schema = @Schema(allowableValues = {"FTM", "DEVICE", "AUTH"})
            )
            @RequestParam(value = "partnerDomain", required = false) String partnerDomain,
            @RequestParam(value = "createdFromDate", required = false)
            @Parameter(description = "Created From Date in 'yyyy-MM-dd' format") String createdFromDate,
            @RequestParam(value = "createdToDate", required = false)
            @Parameter(description = "Created To Date in 'yyyy-MM-dd' format") String createdToDate
    ) {
        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        if (certificateId != null) {
            filterDto.setCertificateId(certificateId);
        }
        if (expiryDate != null) {
            filterDto.setExpiryDate(expiryDate);
        }
        if (issuedBy != null) {
            filterDto.setIssuedBy(issuedBy);
        }
        if (issuedTo != null) {
            filterDto.setIssuedTo(issuedTo);
        }
        if (partnerDomain != null) {
            filterDto.setPartnerDomain(partnerDomain);
        }
        if (notificationStatus != null) {
            filterDto.setNotificationStatus(notificationStatus.toLowerCase());
        }
        if (notificationType != null) {
            filterDto.setNotificationType(notificationType.toLowerCase());
        }
        if (createdFromDate != null) {
            filterDto.setCreatedFromDate(createdFromDate);
        }
        if (createdToDate != null) {
            filterDto.setCreatedToDate(createdToDate);
        }
        return notificationsService.getNotifications(pageNo, pageSize, filterDto);
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdismissnotification())")
    @PatchMapping("/notifications/{notificationId}")
    @Operation(
            summary = "This endpoint dismisses a notification.",
            description = "This endpoint is available since release-1.3.x and is used to dismiss a notification."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapperV2<DismissNotificationResponseDto> dismissNotification(
            @PathVariable String notificationId,
            @RequestBody RequestWrapperV2<DismissNotificationRequestDto> requestWrapper) {
        Optional<ResponseWrapperV2<DismissNotificationResponseDto>> validationResponse = requestValidator.validate(patchDismissNotificationId, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        return notificationsService.dismissNotification(notificationId, requestWrapper.getRequest());
    }
}
