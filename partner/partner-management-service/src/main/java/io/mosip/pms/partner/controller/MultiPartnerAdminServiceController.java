package io.mosip.pms.partner.controller;

import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
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
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(value = "/admin")
@Api(tags = {"Multi Partner Admin Service Controller"})
public class MultiPartnerAdminServiceController {

    public static final String VERSION = "1.0";

    @Value("${mosip.pms.api.id.approve.mapping.device.to.sbi.post:mosip.pms.approve.mapping.device.to.sbi.post}")
    private String postApproveMappingDeviceToSbiId;

    @Value("${mosip.pms.api.id.reject.mapping.device.to.sbi.post:mosip.pms.reject.mapping.device.to.sbi.post}")
    private String postRejectMappingDeviceToSbiId;

    @Autowired
    MultiPartnerAdminService multiPartnerAdminService;

    @Autowired
    RequestValidator requestValidator;

    @PreAuthorize("hasAnyRole(@authorizedRoles.getDeviceprovideradminservice())")
    @PostMapping(value = "/approve-mapping-device-to-sbi")
    @Operation(summary = "Approve device and activate device mapping to sbi.", description = "Approve device and activate device mapping to sbi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapperV2<Boolean> approveMappingDeviceToSbi(@RequestBody @Valid RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper) {
        Optional<ResponseWrapperV2<Boolean>> validationResponse = requestValidator.validate(postApproveMappingDeviceToSbiId, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        return multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), false);
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getDeviceprovideradminservice())")
    @PostMapping(value = "/reject-mapping-device-to-sbi")
    @Operation(summary = "Reject device and activate device mapping to sbi.", description = "Reject device and activate device mapping to sbi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseWrapperV2<Boolean> rejectMappingDeviceToSbi(@RequestBody @Valid RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper) {
        Optional<ResponseWrapperV2<Boolean>> validationResponse = requestValidator.validate(postRejectMappingDeviceToSbiId, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        return multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), true);
    }
}
