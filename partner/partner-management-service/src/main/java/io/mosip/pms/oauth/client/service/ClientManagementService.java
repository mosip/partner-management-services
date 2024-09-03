package io.mosip.pms.oauth.client.service;

import io.mosip.pms.oauth.client.dto.ClientDetail;
import io.mosip.pms.oauth.client.dto.ClientDetailCreateRequest;
import io.mosip.pms.oauth.client.dto.ClientDetailCreateRequestV2;
import io.mosip.pms.oauth.client.dto.ClientDetailResponse;
import io.mosip.pms.oauth.client.dto.ClientDetailUpdateRequest;
import io.mosip.pms.oauth.client.dto.ClientDetailUpdateRequestV2;
import io.mosip.pms.oauth.client.dto.OidcClientDto;

import java.util.List;

public interface ClientManagementService {
	
    /**
     * API to register Relying party client
     * @param clientDetailCreateRequest
     * @return
     * @throws Exception 
     * @throws IdPException
     */
    ClientDetailResponse createOIDCClient(ClientDetailCreateRequest clientDetailCreateRequest) throws Exception;

    /**
     * API to update registered Relying party client
     * @param clientId
     * @param clientDetailCreateRequest
     * @return
     * @throws Exception 
     * @throws IdPException
     */
    ClientDetailResponse updateOIDCClient(String clientId, ClientDetailUpdateRequest clientDetailCreateRequest) throws Exception;
    
    
    /**
     * Api to get the active client detail with the provided client name.
     * @param clientId
     * @return
     */
    ClientDetail getClientDetails(String clientId);

    /**
     * API to create OIDC Client with new Client Name Lang Map 
     * based on V2 OIDC Client Create API of Esignet
     * @param createRequest
     * @return
     * @throws Exception
     */
	ClientDetailResponse createOAuthClient(ClientDetailCreateRequestV2 createRequest) throws Exception;

	/**
	 * API to update OIDC Client with new Client Name Lang Map 
     * based on V2 OIDC Client Update API of Esignet
	 * @param clientId
	 * @param updateRequest
	 * @return
	 * @throws Exception
	 */
	ClientDetailResponse updateOAuthClient(String clientId, ClientDetailUpdateRequestV2 updateRequest)
			throws Exception;

    List<OidcClientDto> getAllOidcClients();
}
