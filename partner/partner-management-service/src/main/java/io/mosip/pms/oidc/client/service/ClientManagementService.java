package io.mosip.pms.oidc.client.service;

import io.mosip.pms.oidc.client.dto.ClientDetail;
import io.mosip.pms.oidc.client.dto.ClientDetailCreateRequest;
import io.mosip.pms.oidc.client.dto.ClientDetailResponse;
import io.mosip.pms.oidc.client.dto.ClientDetailUpdateRequest;

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
}
