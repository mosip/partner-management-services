package io.mosip.pms.test.oauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.oauth.client.controller.ClientManagementController;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.oauth.client.service.ClientManagementService;
import io.mosip.pms.oauth.client.service.impl.ClientManagementServiceImpl;
import io.mosip.pms.partner.dto.PolicyGroupDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientManagementControllerTest {

    @Autowired
    ClientManagementController clientManagementController;

    @Autowired
    private ClientManagementServiceImpl serviceImpl;

    @Mock
    ClientManagementService clientManagementService;

    @Mock
    ClientManagementController clientController;

    Map<String, Object> public_key;

    @Before
    public void setUp() {

        public_key = new HashMap<>();
        public_key.put("kty","RSA");
        public_key.put("e","AQAB");
        public_key.put( "use", "sig");
        public_key.put(  "kid", "1bbdc9de-c24f-4801-b6b3-691ac07641af");
        public_key.put( "alg", "RS256");
        public_key.put(  "n","wXGQA574CU-WTWPILd4S3_1sJf0Yof0kwMeNctXc1thQo70Ljfn9f4igpRe7f8qNs_W6dLuLWemFhGJBQBQ7vvickECKNJfo_EzSD_yyPCg7k_AGbTWTkuoObHrpilwJGyKVSkOIujH_FqHIVkwkVXjWc25Lsb8Gq4nAHNQEqqgaYPLEi5evCR6S0FzcXTPuRh9zH-cM0Onjv4orrfYpEr61HcRp5MXL55b7yBoIYlXD8NfalcgdrWzp4VZHvQ8yT9G5eaf27XUn6ZBeBf7VnELcKFTyw1pK2wqoOxRBc8Y1wO6rEy8PlCU6wD-mbIzcjG1wUfnbgvJOM4A5G41quQ");
    }

    @Test
    public void testCreateOIDCClient() throws Exception {
        io.mosip.pms.common.request.dto.RequestWrapper<ClientDetailCreateRequest> requestWrapper = new io.mosip.pms.common.request.dto.RequestWrapper<>();
        requestWrapper.setId("42");
        requestWrapper.setMetadata("Metadata");
        requestWrapper.setRequest(new ClientDetailCreateRequest());
        requestWrapper.setRequesttime(null);
        requestWrapper.setVersion("1.0.2");
        String content = (new ObjectMapper()).writeValueAsString(requestWrapper);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/oauth/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(clientManagementController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    public void testUpdateClient() throws Exception {
        io.mosip.pms.common.request.dto.RequestWrapper<ClientDetailUpdateRequest> requestWrapper = new io.mosip.pms.common.request.dto.RequestWrapper<>();
        requestWrapper.setId("42");
        requestWrapper.setMetadata("Metadata");
        requestWrapper.setRequest(new ClientDetailUpdateRequest());
        requestWrapper.setRequesttime(null);
        requestWrapper.setVersion("1.0.2");
        String content = (new ObjectMapper()).writeValueAsString(requestWrapper);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/oauth/client/{client_id}", "Client id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(clientManagementController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    public void testCreateOAUTHClient() throws Exception {
        io.mosip.pms.common.request.dto.RequestWrapper<ClientDetailCreateRequestV2> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId("42");
        requestWrapper.setMetadata("Metadata");
        requestWrapper.setRequest(new ClientDetailCreateRequestV2());
        requestWrapper.setRequesttime(null);
        requestWrapper.setVersion("1.0.2");
        String content = (new ObjectMapper()).writeValueAsString(requestWrapper);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/oauth/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(clientManagementController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test
    public void testUpdateOAUTHClient() throws Exception {
        io.mosip.pms.common.request.dto.RequestWrapper<ClientDetailUpdateRequestV2> requestWrapper = new io.mosip.pms.common.request.dto.RequestWrapper<>();
        requestWrapper.setId("42");
        requestWrapper.setMetadata("Metadata");
        requestWrapper.setRequest(new ClientDetailUpdateRequestV2());
        requestWrapper.setRequesttime(null);
        requestWrapper.setVersion("1.0.2");
        String content = (new ObjectMapper()).writeValueAsString(requestWrapper);
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .put("/oauth/client/{client_id}", "Client id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        ResultActions actualPerformResult = MockMvcBuilders.standaloneSetup(clientManagementController)
                .build()
                .perform(requestBuilder);
        actualPerformResult.andExpect(MockMvcResultMatchers.status().is(400));
    }

    @Test (expected = PartnerServiceException.class)
    public void testGetOAuthClient() throws Exception {
        io.mosip.pms.oauth.client.dto.ClientDetail clientDetail = new io.mosip.pms.oauth.client.dto.ClientDetail();
        clientDetail.setAcrValues(Collections.singletonList("Value"));
        clientDetail.setClaims(Collections.singletonList("Claims"));
        clientDetail.setClientAuthMethods(Collections.singletonList("Client Auth Methods"));
        clientDetail.setGrantTypes(Collections.singletonList("Grant Types"));
        clientDetail.setId("123");
        clientDetail.setLogoUri("Logo Uri");
        clientDetail.setName("Name");
        clientDetail.setPolicyId("123");
        clientDetail.setPublicKey("Public Key");
        clientDetail.setRedirectUris(Collections.singletonList("Redirect Uris"));
        clientDetail.setStatus("Status");

        when(serviceImpl.getClientDetails("123")).thenReturn(clientDetail);

        ResponseWrapper<io.mosip.pms.oauth.client.dto.ClientDetail> expectedResponse = new ResponseWrapper<>();
        expectedResponse.setResponse(clientDetail);

        ResponseWrapper<io.mosip.pms.oauth.client.dto.ClientDetail> actualResponse = clientManagementController.getOAuthClient("123");

        verify(serviceImpl).getClientDetails("123");
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getAllOidcClients() throws Exception {
        List<OidcClientDto> oidcClientDtoList = new ArrayList<>();
        when(clientManagementService.getAllOidcClients()).thenReturn(oidcClientDtoList);
        ResponseWrapper<List<OidcClientDto>> actualResponse = clientController.getAllOidcClients();
    }

}