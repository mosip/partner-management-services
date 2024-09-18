package io.mosip.pms.oauth.client.controller;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import io.swagger.v3.oas.annotations.Operation;
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
	AuditUtil auditUtil;

	@RequestMapping(value = "/oauth/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> createOAUTHClient(
			@Valid @RequestBody RequestWrapper<ClientDetailCreateRequestV2> requestWrapper) throws Exception {
		var clientRespDto = clientManagementService.createOAuthClient(requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		response.setResponse(clientRespDto);
		return response;
	}
	
	@RequestMapping(value = "/oauth/client/{client_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> updateOAUTHClient(@PathVariable("client_id") String clientId,
			@Valid @RequestBody RequestWrapper<ClientDetailUpdateRequestV2> requestWrapper) throws Exception {
		var clientRespDto = clientManagementService.updateOAuthClient(clientId, requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		response.setResponse(clientRespDto);
		return response;
	}

	@RequestMapping(value = "/oauth/client/{client_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetail> getOAuthClient(@PathVariable("client_id") String clientId)
			throws Exception {
		var response = new ResponseWrapper<ClientDetail>();
		response.setResponse(clientManagementService.getClientDetails(clientId));
		return response;
	}
	
	@Deprecated
	@RequestMapping(value = "/oidc/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> createClient(
			@Valid @RequestBody RequestWrapper<ClientDetailCreateRequest> requestWrapper) throws Exception {
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
		var response = new ResponseWrapper<ClientDetail>();
		response.setResponse(clientManagementService.getClientDetails(clientId));
		return response;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getOauthclients())")
	@GetMapping(value = "/oauth/clients")
	@Operation(summary = "Get all clients", description = "fetch all clients")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapperV2<List<OauthClientDto>> getClients() {
		return clientManagementService.getClients();
	}
	
}