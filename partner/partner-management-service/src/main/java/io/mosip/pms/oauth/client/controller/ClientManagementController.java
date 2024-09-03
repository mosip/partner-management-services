package io.mosip.pms.oauth.client.controller;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import io.mosip.pms.oauth.client.service.ClientManagementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.oauth.client.dto.ClientDetail;
import io.mosip.pms.oauth.client.dto.ClientDetailCreateRequest;
import io.mosip.pms.oauth.client.dto.ClientDetailCreateRequestV2;
import io.mosip.pms.oauth.client.dto.ClientDetailResponse;
import io.mosip.pms.oauth.client.dto.ClientDetailUpdateRequest;
import io.mosip.pms.oauth.client.dto.ClientDetailUpdateRequestV2;
import io.mosip.pms.oauth.client.dto.OidcClientDto;

import jakarta.validation.Valid;

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

	@Value("${mosip.pms.api.id.all.oidc.clients.get}")
	private String getAllOidcClientsId;

	public static final String VERSION = "1.0";

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

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetalloidcclients())")
	@GetMapping(value = "/getAllOidcClients")
	@Operation(summary = "Get all Oidc clients", description = "fetch all oidc clients")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<OidcClientDto>> getAllOidcClients() {
		ResponseWrapper<List<OidcClientDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(getAllOidcClientsId);
		responseWrapper.setVersion(VERSION);
		responseWrapper.setResponse(clientManagementService.getAllOidcClients());
		return responseWrapper;
	}
	
}