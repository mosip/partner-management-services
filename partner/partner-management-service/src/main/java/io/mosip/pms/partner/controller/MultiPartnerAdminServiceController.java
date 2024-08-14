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
@Api(tags = { "Multi Partner Admin Service Controller" })
public class MultiPartnerAdminServiceController {

    public static final String VERSION = "1.0";

    private static final String MULTI_PARTNER_ADMIN_SERVICE_POST = "multi.partner.admin.service.post";

    @Value("${mosip.pms.api.id.approve.device.with.sbi.mapping.get:mosip.approve.device.with.sbi.mapping.get}")
    private String getApproveDeviceWithSbiMappingId;

    @Autowired
    MultiPartnerAdminService multiPartnerAdminService;

    @Autowired
    RequestValidator requestValidator;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdevicedetail())")
    @PostMapping(value = "/approveDeviceWithSbiMapping")
    @Operation(summary = "Add inactive device mapping to SBI.", description = "Add inactive device mapping to SBI.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapper<Boolean> approveDeviceWithSbiMapping(@RequestBody @Valid RequestWrapper<SbiAndDeviceMappingRequestDto> requestWrapper) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        requestValidator.validateId(MULTI_PARTNER_ADMIN_SERVICE_POST, requestWrapper.getId());
        requestValidator.validate(requestWrapper);
        responseWrapper.setId(getApproveDeviceWithSbiMappingId);
        responseWrapper.setVersion(VERSION);
        responseWrapper.setResponse(multiPartnerAdminService.approveDeviceWithSbiMapping(requestWrapper.getRequest()));
        return responseWrapper;
    }
}
