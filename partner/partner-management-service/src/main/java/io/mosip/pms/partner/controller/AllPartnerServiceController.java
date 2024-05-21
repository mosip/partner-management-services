package io.mosip.pms.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.service.AllPartnerService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/partners")
@Api(tags = { "All Partner Service Controller" })
public class AllPartnerServiceController {

    @Autowired
    AllPartnerService allPartnerService;

    @GetMapping(value = "/getAllCertificateDetails")
    @Operation(summary = "Get partner certificates", description = "fetch partner certificates")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<List<CertificateDto>> getAllCertificateDetails() {
        ResponseWrapper<List<CertificateDto>> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(allPartnerService.getAllCertificateDetails());
        return responseWrapper;
    }
}
