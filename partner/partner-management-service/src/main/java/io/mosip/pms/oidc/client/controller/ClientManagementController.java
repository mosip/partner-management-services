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
import io.mosip.pms.oidc.client.dto.ClientDetailCreateRequestV2;
import io.mosip.pms.oidc.client.dto.ClientDetailResponse;
import io.mosip.pms.oidc.client.dto.ClientDetailUpdateRequest;
import io.mosip.pms.oidc.client.dto.ClientDetailUpdateRequestV2;
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


	@RequestMapping(value = "/oidc/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> createClient(
			@Valid @RequestBody RequestWrapper<ClientDetailCreateRequest> requestWrapper) throws Exception {
		var clientRespDto = clientManagementService.createOIDCClient(requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT,requestWrapper.getRequest().getName(),"clientID");
		response.setResponse(clientRespDto);
		return response;
	}
	
	@RequestMapping(value = "/oidc/client/v2", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> createClientV2(
			@Valid @RequestBody RequestWrapper<ClientDetailCreateRequestV2> requestWrapper) throws Exception {
		var clientRespDto = clientManagementService.createOIDCClientV2(requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		response.setResponse(clientRespDto);
		return response;
	}

	@RequestMapping(value = "/oidc/client/{client_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> updateClient(@PathVariable("client_id") String clientId,
			@Valid @RequestBody RequestWrapper<ClientDetailUpdateRequest> requestWrapper) throws Exception {
		var clientRespDto = clientManagementService.updateOIDCClient(clientId, requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT, clientId, "clientID");
		response.setResponse(clientRespDto);
		return response;
	}
	
	@RequestMapping(value = "/oidc/client/v2/{client_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetailResponse> updateClientV2(@PathVariable("client_id") String clientId,
			@Valid @RequestBody RequestWrapper<ClientDetailUpdateRequestV2> requestWrapper) throws Exception {
		var clientRespDto = clientManagementService.updateOIDCClientV2(clientId, requestWrapper.getRequest());
		var response = new ResponseWrapper<ClientDetailResponse>();
		response.setResponse(clientRespDto);
		return response;
	}

	@RequestMapping(value = "/oidc/client/{client_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<ClientDetail> getClient(@PathVariable("client_id") String clientId)
			throws Exception {
		var response = new ResponseWrapper<ClientDetail>();
		response.setResponse(clientManagementService.getClientDetails(clientId));
		return response;
	}
}