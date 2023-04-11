package io.mosip.pms.oidc.client.controller;

import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.oidc.client.dto.ClientDetail;
import io.mosip.pms.oidc.client.dto.ClientDetailCreateRequest;
import io.mosip.pms.oidc.client.dto.ClientDetailResponse;
import io.mosip.pms.oidc.client.dto.ClientDetailUpdateRequest;
import io.mosip.pms.oidc.client.service.ClientManagementService;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClientManagementController {

	@Autowired
	ClientManagementService clientManagementService;

	@Autowired
	AuditUtil auditUtil;

	String msg = "mosip.clientmanagement.clients.retrieve";

	String version = "1.0";

	@RequestMapping(value = "/oidc/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<ClientDetailResponse>> createClient(
			@Valid @RequestBody RequestWrapper<ClientDetailCreateRequest> requestWrapper) throws Exception {
		//var clientRespDto = clientManagementService.createOIDCClient(requestWrapper.getRequest());
		ResponseWrapper<ClientDetailResponse> response = new ResponseWrapper<>();
		//var response = new ResponseWrapper<ClientDetailResponse>();
		ClientDetailResponse clientDetailResponse = null;
		auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT,requestWrapper.getRequest().getName(),"clientID");
		clientDetailResponse = clientManagementService.createOIDCClient(requestWrapper.getRequest());
		//response.setResponse(clientRespDto);
		response.setId(requestWrapper.getId());
		response.setVersion(requestWrapper.getVersion());
		response.setResponse(clientDetailResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/oidc/client/{client_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<ClientDetailResponse>> updateClient(@PathVariable("client_id") String clientId,
			@Valid @RequestBody RequestWrapper<ClientDetailUpdateRequest> requestWrapper) throws Exception {
		//var clientRespDto = clientManagementService.updateOIDCClient(clientId, requestWrapper.getRequest());
		ResponseWrapper<ClientDetailResponse> response = new ResponseWrapper<>();
		//var response = new ResponseWrapper<ClientDetailResponse>();
		ClientDetailResponse clientDetailResponse = null;
		ClientDetailUpdateRequest updateRequest = requestWrapper.getRequest();
		auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT, clientId, "clientID");
		clientDetailResponse = clientManagementService.updateOIDCClient(clientId, updateRequest);
		response.setId(requestWrapper.getId());
		response.setVersion(requestWrapper.getVersion());
		response.setResponse(clientDetailResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/oidc/client/{client_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<ClientDetail>> getClient(@PathVariable("client_id") String clientId)
			throws Exception {
		ResponseWrapper<ClientDetail> response = new ResponseWrapper<>();
		ClientDetail clientDetail = null;
		clientDetail = clientManagementService.getClientDetails(clientId);
		//var response = new ResponseWrapper<ClientDetail>();
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(clientDetail);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
