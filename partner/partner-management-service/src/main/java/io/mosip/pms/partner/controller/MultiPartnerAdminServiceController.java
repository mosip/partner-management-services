package io.mosip.pms.partner.controller;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.service.MultiPartnerAdminService;
import io.mosip.pms.partner.util.RequestValidator;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/admin")
@Api(tags = {"Multi Partner Admin Service Controller"})
public class MultiPartnerAdminServiceController {

    public static final String VERSION = "1.0";

    @Value("${mosip.pms.api.id.approve.device.with.sbi.mapping.post:mosip.approve.device.with.sbi.mapping.post}")
    private String postApproveDeviceWithSbiMappingId;

    @Value("${mosip.pms.api.id.reject.device.with.sbi.mapping.post:mosip.reject.device.with.sbi.mapping.post}")
    private String postRejectDeviceWithSbiMappingId;

    @Autowired
    MultiPartnerAdminService multiPartnerAdminService;

    @Autowired
    RequestValidator requestValidator;

    public static final String APPROVE_DEVICE_WITH_SBI_MAPPING_POST = "approve.device.with.sbi.mapping.post";
    public static final String REJECT_DEVICE_WITH_SBI_MAPPING_POST = "reject.device.with.sbi.mapping.post";

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicewithsbimapping())")
    @PostMapping(value = "/approveDeviceWithSbiMapping")
    @Operation(summary = "Approve device and activate device mapping to sbi.", description = "Approve device and activate device mapping to sbi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<Boolean> approveDeviceWithSbiMapping(@RequestBody @Valid RequestWrapper<SbiAndDeviceMappingRequestDto> requestWrapper) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        requestValidator.validateId(APPROVE_DEVICE_WITH_SBI_MAPPING_POST, requestWrapper.getId());
        requestValidator.validate(requestWrapper);
        responseWrapper.setId(postApproveDeviceWithSbiMappingId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerAdminService.approveOrRejectDeviceWithSbiMapping(requestWrapper.getRequest(), false));
        return responseWrapper;
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicewithsbimapping())")
    @PostMapping(value = "/rejectDeviceWithSbiMapping")
    @Operation(summary = "Reject device and activate device mapping to sbi.", description = "Reject device and activate device mapping to sbi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<Boolean> rejectDeviceWithSbiMapping(@RequestBody @Valid RequestWrapper<SbiAndDeviceMappingRequestDto> requestWrapper) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        requestValidator.validateId(REJECT_DEVICE_WITH_SBI_MAPPING_POST, requestWrapper.getId());
        requestValidator.validate(requestWrapper);
        responseWrapper.setId(postRejectDeviceWithSbiMappingId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerAdminService.approveOrRejectDeviceWithSbiMapping(requestWrapper.getRequest(), true));
        return responseWrapper;
    }
}
