package io.mosip.pms.oauth.client.controller;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ClientManagementController {

	@Autowired
	ClientManagementService clientManagementService;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	FeatureAvailabilityUtil featureAvailabilityUtil;

	@RequestMapping(value = "/oauth/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> createOAUTHClient(
			@Valid @RequestBody RequestWrapper<ClientDetailCreateRequestV2> requestWrapper) throws Exception {
		featureAvailabilityUtil.validateOidcClientFeatureEnabled();
		var clientRespDto = clientManagementService.createOAuthClient(requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		response.setResponse(clientRespDto);
		return response;
	}
	
	@RequestMapping(value = "/oauth/client/{client_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> updateOAUTHClient(@PathVariable("client_id") String clientId,
			@Valid @RequestBody RequestWrapper<ClientDetailUpdateRequestV2> requestWrapper) throws Exception {
		featureAvailabilityUtil.validateOidcClientFeatureEnabled();
		var clientRespDto = clientManagementService.updateOAuthClient(clientId, requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		response.setResponse(clientRespDto);
		return response;
	}

	@RequestMapping(value = "/oauth/client/{client_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
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

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetoauthpartnersclients())")
	@GetMapping(value = "/oauth/client")
	@Operation(summary = "This endpoint retrieves a list of all OAuth clients created by the Auth Partners.",
			description = "Available since release-1.2.2.0. This endpoint supports pagination, sorting, and and filtering based on optional query parameters.  If the token used to access this endpoint, does not have the PARTNER_ADMIN role, then it will fetch all the OAuth clients created by all the partners associated with the logged in user only. If the token used to access this endpoint, has PARTNER_ADMIN role, then it will fetch all the OAuth clients created by all the partners. It is configured for PARTNER_ADMIN and AUTH_PARTNER roles.")
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
		featureAvailabilityUtil.validateOidcClientFeatureEnabled();
		partnerHelper.validateRequestParameters(partnerHelper.oidcClientsAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		ClientFilterDto filterDto = new ClientFilterDto();
		if (partnerId != null) {
			filterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (orgName != null) {
			filterDto.setOrgName(orgName.toLowerCase());
		}
		if (policyGroupName != null) {
			filterDto.setPolicyGroupName(policyGroupName.toLowerCase());
		}
		if (policyName != null) {
			filterDto.setPolicyName(policyName.toLowerCase());
		}
		if (clientName != null) {
			filterDto.setClientName(clientName.toLowerCase());
		}
		if (status != null) {
			filterDto.setStatus(status);
		}
		return clientManagementService.getPartnersClients(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}
	
}