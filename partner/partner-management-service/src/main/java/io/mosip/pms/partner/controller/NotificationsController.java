package io.mosip.pms.partner.controller;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RequestValidator;
import io.mosip.pms.common.validator.InputValidator;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.partner.service.NotificationsService;
import io.mosip.pms.partner.util.PartnerHelper;
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

    @Autowired
	private InputValidator inputValidator;

    @Autowired
    private PartnerHelper partnerHelper;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetnotifications())")
    @GetMapping(value = "/notifications")
    @Operation(summary = "This endpoint retrieves a list of all notifications.",
            description = "Available since release-1.3.0-beta.1. The notifications fetched will be for all the partner ids which are linked to the logged in user id, which is sent as a Header in the Authorization token. This endpoint supports pagination, sorting, and filtering.")
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
                    schema = @Schema(allowableValues = {"root", "intermediate", "partner", "weekly", "sbi", "ftm-chip", "apikey", "misp"})
            )
            @RequestParam(value = "notificationType", required = false) String notificationType,
            @RequestParam(value = "pageNo", defaultValue = "0") String pageNo,
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
            @Parameter(description = "Created To Date in 'yyyy-MM-dd' format") String createdToDate,
            @RequestParam(value = "ftmId", required = false) String ftmId,
            @RequestParam(value = "make", required = false) String make,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "apiKeyName", required = false) String apiKeyName,
            @RequestParam(value = "policyName", required = false) String policyName,
            @RequestParam(value = "sbiId", required = false) String sbiId,
            @RequestParam(value = "sbiVersion", required = false) String sbiVersion,
            @RequestParam(value = "mispLicenseKeyName", required = false) String mispLicenseKeyName,
            @RequestParam(value = "mispPartnerId", required = false) String mispPartnerId
    ) {
        // Validate all inputs
        inputValidator.validateRequestInput("notificationStatus", notificationStatus);
        inputValidator.validateRequestInput("notificationType", notificationType);
        inputValidator.validateRequestInput("certificateId", certificateId);
        inputValidator.validateRequestInput("issuedBy", issuedBy);
        inputValidator.validateRequestInput("issuedTo", issuedTo);
        inputValidator.validateRequestInput("partnerDomain", partnerDomain);
        inputValidator.validateRequestInput("ftmId", ftmId);
        inputValidator.validateRequestInput("make", make);
        inputValidator.validateRequestInput("model", model);
        inputValidator.validateRequestInput("apiKeyName", apiKeyName);
        inputValidator.validateRequestInput("policyName", policyName);
        inputValidator.validateRequestInput("sbiId", sbiId);
        inputValidator.validateRequestInput("sbiVersion", sbiVersion);
        inputValidator.validateRequestInput("mispLicenseKeyName", mispLicenseKeyName);
        inputValidator.validateRequestInput("mispPartnerId", mispPartnerId);

        // Populate filter DTO
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
        if (ftmId != null) {
            filterDto.setFtmId(ftmId);
        }
        if (make != null) {
            filterDto.setMake(make);
        }
        if (model != null) {
            filterDto.setModel(model);
        }
        if (apiKeyName != null) {
            filterDto.setApiKeyName(apiKeyName);
        }
        if (policyName != null) {
            filterDto.setPolicyName(policyName);
        }
        if (sbiId != null) {
            filterDto.setSbiId(sbiId);
        }
        if (sbiVersion != null) {
            filterDto.setSbiVersion(sbiVersion);
        }
        if (mispLicenseKeyName != null) {
            filterDto.setMispLicenseKeyName(mispLicenseKeyName);
        }
        if (mispPartnerId != null) {
            filterDto.setMispPartnerId(mispPartnerId);
        }
        return notificationsService.getNotifications(
                partnerHelper.parsePageNo(pageNo),
                pageSize,
                filterDto);
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdismissnotification())")
    @PatchMapping("/notifications/{notificationId}")
    @Operation(
            summary = "This endpoint dismisses a notification.",
            description = "This endpoint is available since release-1.3.0-beta.1 and is used to dismiss a notification."
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
        inputValidator.validateRequestInput("notificationId", notificationId);
        inputValidator.validateRequestInput("status", requestWrapper.getRequest().getNotificationStatus());
        return notificationsService.dismissNotification(notificationId, requestWrapper.getRequest());
    }
}
