package io.mosip.pms.oauth.client.controller;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.RequestValidator;
import io.mosip.pms.common.validator.InputValidator;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.util.FeatureAvailabilityUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.mosip.pms.oauth.client.service.ClientManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class ClientManagementController {

    @Value("${mosip.pms.client.id.regex}")
    private String clientIdRegex;

    @Value("${mosip.pms.api.id.create.oidc.client.post}")
    private String postCreateOidcClientId;

    @Value("${mosip.pms.api.id.update.oidc.client.put}")
    private String putUpdateOidcClientId;

    @Value("${mosip.pms.api.id.deactivate.oidc.client.patch}")
    private String patchDeactivateOidcClientId;

    @Autowired
    ClientManagementService clientManagementService;

    @Autowired
    PartnerHelper partnerHelper;

    @Autowired
    AuditUtil auditUtil;

    @Autowired
    RequestValidator requestValidator;

    @Autowired
    FeatureAvailabilityUtil featureAvailabilityUtil;

    @Autowired
    private InputValidator inputValidator;

    @Deprecated(since = "release-1.3.0-beta.4")
    @RequestMapping(value = "/oauth/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Service to create OAuth client  - deprecated since release-1.3.0-beta.4", description = "This endpoint has been deprecated since the release-1.3.0-beta.4 and replaced by the POST /oidc-clients endpoint.")
    public ResponseWrapper<ClientDetailResponse> createOAUTHClient(
            @Valid @RequestBody RequestWrapper<ClientDetailCreateRequestV2> requestWrapper) throws Exception {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        var clientRespDto = clientManagementService.createOAuthClient(requestWrapper.getRequest());
        var response = new ResponseWrapper<ClientDetailResponse>();
        response.setResponse(clientRespDto);
        return response;
    }

    @Deprecated(since = "release-1.3.0-beta.4")
    @RequestMapping(value = "/oauth/client/{client_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Service to update OAuth client  - deprecated since release-1.3.0-beta.4", description = "This endpoint has been deprecated since the release-1.3.0-beta.4 and replaced by the PUT /oidc-clients/{clientId} endpoint.")
    public ResponseWrapper<ClientDetailResponse> updateOAUTHClient(@PathVariable("client_id") String clientId,
                                                                   @Valid @RequestBody RequestWrapper<ClientDetailUpdateRequestV2> requestWrapper) throws Exception {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        if (!clientId.matches(clientIdRegex)) {
            throw new PartnerServiceException(
                    ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
                    String.format(
                            ErrorCode.INVALID_INPUT_FORMAT.getErrorMessage(),
                            "clientId",
                            "Only alphanumeric characters (A–Z, a–z, 0–9), hyphens (-), and underscores (_) are allowed, with a maximum length of 100 characters"
                    )
            );
        }
        requestValidator.validateReqTime(requestWrapper.getRequesttime());
        var clientRespDto = clientManagementService.updateOAuthClient(clientId, requestWrapper.getRequest());
        var response = new ResponseWrapper<ClientDetailResponse>();
        response.setResponse(clientRespDto);
        return response;
    }

    @Deprecated(since = "release-1.3.0-beta.4")
    @RequestMapping(value = "/oauth/client/{client_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Service to get OAuth client details  - deprecated since release-1.3.0-beta.4", description = "This endpoint has been deprecated since the release-1.3.0-beta.4 and replaced by the GET /oidc-clients/{clientId} endpoint.")
    public ResponseWrapper<ClientDetail> getOAuthClient(@PathVariable("client_id") String clientId)
            throws Exception {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        var response = new ResponseWrapper<ClientDetail>();
        response.setResponse(clientManagementService.getClientDetails(clientId));
        return response;
    }

    @Deprecated
    @RequestMapping(value = "/oidc/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<ClientDetailResponse> createClient(
            @Valid @RequestBody RequestWrapper<ClientDetailCreateRequest> requestWrapper) throws Exception {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        var clientRespDto = clientManagementService.createOIDCClient(requestWrapper.getRequest());
        var response = new ResponseWrapper<ClientDetailResponse>();
        auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT,requestWrapper.getRequest().getName(),"clientID");
        response.setResponse(clientRespDto);
        return response;
    }

    @Deprecated
    @RequestMapping(value = "/oidc/client/{client_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<ClientDetailResponse> updateClient(@PathVariable("client_id") String clientId,
                                                              @Valid @RequestBody RequestWrapper<ClientDetailUpdateRequest> requestWrapper) throws Exception {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        if (!clientId.matches(clientIdRegex)) {
            throw new PartnerServiceException(
                    ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
                    String.format(
                            ErrorCode.INVALID_INPUT_FORMAT.getErrorMessage(),
                            "clientId",
                            "Only alphanumeric characters (A–Z, a–z, 0–9), hyphens (-), and underscores (_) are allowed, with a maximum length of 100 characters"
                    )
            );
        }
        var clientRespDto = clientManagementService.updateOIDCClient(clientId, requestWrapper.getRequest());
        var response = new ResponseWrapper<ClientDetailResponse>();
        auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT, clientId, "clientID");
        response.setResponse(clientRespDto);
        return response;
    }

    @Deprecated
    @RequestMapping(value = "/oidc/client/{client_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<ClientDetail> getOIDCClient(@PathVariable("client_id") String clientId)
            throws Exception {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        var response = new ResponseWrapper<ClientDetail>();
        response.setResponse(clientManagementService.getClientDetails(clientId));
        return response;
    }

    @Deprecated
    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetoauthpartnersclients())")
    @GetMapping(value = "/oauth/client")
    @Operation(summary = "This endpoint retrieves a list of all OAuth clients created by the Auth Partners - deprecated since release-1.3.0-beta.4.",
            description = "This endpoint has been deprecated since the release-1.3.0-beta.4 and replaced by the GET /oidc-clients endpoint. This endpoint supports pagination, sorting, and and filtering based on optional query parameters.  If the token used to access this endpoint, does not have the PARTNER_ADMIN role, then it will fetch all the OAuth clients created by all the partners associated with the logged in user only. If the token used to access this endpoint, has PARTNER_ADMIN role, then it will fetch all the OAuth clients created by all the partners. It is configured for PARTNER_ADMIN and AUTH_PARTNER roles.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> getPartnersClients(
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortType", required = false) String sortType,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "partnerId", required = false) String partnerId,
            @RequestParam(value = "orgName", required = false) String orgName,
            @RequestParam(value = "policyGroupName", required = false) String policyGroupName,
            @RequestParam(value = "policyName", required = false) String policyName,
            @RequestParam(value = "clientName", required = false) String clientName,
            @Parameter(
                    description = "Status of OAuth client",
                    in = ParameterIn.QUERY,
                    schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE"})
            )
            @RequestParam(value = "status", required = false) String status
    ) {
        ClientFilterDto filterDto = populateClientFilterDto(sortFieldName, sortType, pageNo, pageSize,
                partnerId, orgName, policyGroupName, policyName, clientName, status);
        return clientManagementService.getPartnersClientsV2(sortFieldName, sortType, pageNo, pageSize, filterDto);
    }

    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetoauthpartnersclients())")
    @GetMapping(value = "/oidc-clients")
    @Operation(summary = "Endpoint to get the list of all the OIDC clients created by the Auth Partners.",
            description = "Available since release-1.3.0-beta.4. This endpoint supports pagination, sorting, and and filtering based on optional query parameters.  If the token used to access this endpoint, does not have the PARTNER_ADMIN role, then it will fetch all the OIDC clients created by all the partners associated with the logged in user only. If the token used to access this endpoint, has PARTNER_ADMIN role, then it will fetch all the OIDC clients created by all the partners. It is configured for PARTNER_ADMIN and AUTH_PARTNER roles. Also it is an enhanced version of the previous GET /oauth/client endpoint")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> getPartnersClientsV2(
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortType", required = false) String sortType,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "partnerId", required = false) String partnerId,
            @RequestParam(value = "orgName", required = false) String orgName,
            @RequestParam(value = "policyGroupName", required = false) String policyGroupName,
            @RequestParam(value = "policyName", required = false) String policyName,
            @RequestParam(value = "clientName", required = false) String clientName,
            @Parameter(
                    description = "Status of OAuth client",
                    in = ParameterIn.QUERY,
                    schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE"})
            )
            @RequestParam(value = "status", required = false) String status
    ) {
        ClientFilterDto filterDto = populateClientFilterDto(sortFieldName, sortType, pageNo, pageSize,
                partnerId, orgName, policyGroupName, policyName, clientName, status);
        return clientManagementService.getPartnersClientsV2(sortFieldName, sortType, pageNo, pageSize, filterDto);
    }

    private ClientFilterDto populateClientFilterDto(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, String partnerId,
                                                    String orgName, String policyGroupName, String policyName, String clientName, String status) {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        // validate input fields
        inputValidator.validateRequestInput("sortFieldName", sortFieldName);
        inputValidator.validateRequestInput("sortType", sortType);
        inputValidator.validateRequestInput("partnerId", partnerId);
        inputValidator.validateRequestInput("orgName", orgName);
        inputValidator.validateRequestInput("policyGroupName", policyGroupName);
        inputValidator.validateRequestInput("policyName", policyName);
        inputValidator.validateRequestInput("clientName", clientName);
        inputValidator.validateRequestInput("status", status);

        // build filter dto
        ClientFilterDto filterDto = new ClientFilterDto();
        if (partnerId != null) filterDto.setPartnerId(partnerId.toLowerCase());
        if (orgName != null) filterDto.setOrgName(orgName.toLowerCase());
        if (policyGroupName != null) filterDto.setPolicyGroupName(policyGroupName.toLowerCase());
        if (policyName != null) filterDto.setPolicyName(policyName.toLowerCase());
        if (clientName != null) filterDto.setClientName(clientName.toLowerCase());
        if (status != null) filterDto.setStatus(status);

        return filterDto;
    }

    @RequestMapping(value = "/oidc-clients", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole(@authorizedRoles.getPostcreateoidcclient())")
    @Operation(summary = "Creates a new OIDC client for a given Auth Partner.",
            description = " Available since release 1.3.0-beta.4. This endpoint is only accessible to users with AUTH_PARTNER role and is an enhanced version of the previous POST /oauth/client endpoint, with support for the new additionalConfig field in the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<ClientDetailResponse> createOIDCClientV2(
            @Valid @RequestBody RequestWrapperV2<ClientDetailCreateRequestV3> requestWrapper) {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        featureAvailabilityUtil.validateOidcClientAdditionalInfoFeatureAvailable();
        Optional<ResponseWrapperV2<ClientDetailResponse>> validationResponse = requestValidator.validate(postCreateOidcClientId, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        inputValidator.validateRequestInput("name", requestWrapper.getRequest().getName());
        inputValidator.validateRequestInput("policyId", requestWrapper.getRequest().getPolicyId());
        inputValidator.validateRequestInput("authPartnerId", requestWrapper.getRequest().getAuthPartnerId());
        return clientManagementService.createOIDCClientV2(requestWrapper.getRequest());
    }

    @RequestMapping(value = "/oidc-clients/{clientId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole(@authorizedRoles.getPutupdateoidcclient())")
    @Operation(summary = "Update existing OIDC client by Client ID.",
            description = " Available since release 1.3.0-beta.4. This endpoint is only accessible to users with AUTH_PARTNER role and is an enhanced version of the previous PUT /oauth/client/{client_id} endpoint, with support for the new additionalConfig field in the request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<ClientDetailResponse> updateOIDCClientV2( @PathVariable("clientId") String clientId,
                                                                       @Valid @RequestBody RequestWrapperV2<ClientDetailUpdateRequestV3> requestWrapper) {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        featureAvailabilityUtil.validateOidcClientAdditionalInfoFeatureAvailable();
        Optional<ResponseWrapperV2<ClientDetailResponse>> validationResponse = requestValidator.validate(putUpdateOidcClientId, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        if (!clientId.matches(clientIdRegex)) {
            throw new PartnerServiceException(ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
                    String.format(ErrorCode.INVALID_INPUT_FORMAT.getErrorMessage(), "clientId", "Only alphanumeric characters (A–Z, a–z, 0–9), hyphens (-), and underscores (_) are allowed, with a maximum length of 100 characters"));
        }
        inputValidator.validateRequestInput("clientName", requestWrapper.getRequest().getClientName());
        return clientManagementService.updateOIDCClientV2(clientId, requestWrapper.getRequest());
    }

    @RequestMapping(value = "/oidc-clients/{clientId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole(@authorizedRoles.getGetoidcclientdetails())")
    @Operation(summary = "Get details of an existing OIDC client",
            description = " Available since release 1.3.0-beta.4. This endpoint is accessible to users with AUTH_PARTNER or PARTNER_ADMIN role and is an enhanced version of the previous GET /oauth/client/{client_id} endpoint, with support for the new additionalConfig field in the response.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<ClientDetailV2> getOIDCClientV2( @PathVariable("clientId") String clientId) {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        if (!clientId.matches(clientIdRegex)) {
            throw new PartnerServiceException(ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
                    String.format(ErrorCode.INVALID_INPUT_FORMAT.getErrorMessage(), "clientId", "Only alphanumeric characters (A–Z, a–z, 0–9), hyphens (-), and underscores (_) are allowed, with a maximum length of 100 characters"));
        }
        return clientManagementService.getOIDCClientV2(clientId);
    }

    @RequestMapping(value = "/oidc-clients/{clientId}", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdeactivateoidcclient())")
    @Operation(summary = "Deactivate OIDC client by Client ID.",
            description = " Available since release 1.3.0-beta.4. This endpoint is accessible to users with AUTH_PARTNER or PARTNER_ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapperV2<ClientDetailResponse> deactivateOIDCClient( @PathVariable("clientId") String clientId,
                                                                         @Valid @RequestBody RequestWrapperV2<DeactivateOidcClientRequestDto> requestWrapper) {
        featureAvailabilityUtil.validateOidcClientFeatureEnabled();
        featureAvailabilityUtil.validateOidcClientAdditionalInfoFeatureAvailable();
        Optional<ResponseWrapperV2<ClientDetailResponse>> validationResponse = requestValidator.validate(patchDeactivateOidcClientId, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        if (!clientId.matches(clientIdRegex)) {
            throw new PartnerServiceException(ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
                    String.format(ErrorCode.INVALID_INPUT_FORMAT.getErrorMessage(), "clientId", "Only alphanumeric characters (A–Z, a–z, 0–9), hyphens (-), and underscores (_) are allowed, with a maximum length of 100 characters"));
        }
        inputValidator.validateRequestInput("status", requestWrapper.getRequest().getStatus());
        return clientManagementService.deactivateOIDCClient(clientId, requestWrapper.getRequest());
    }

}