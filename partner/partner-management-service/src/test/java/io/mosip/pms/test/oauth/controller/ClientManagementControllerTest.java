package io.mosip.pms.test.oauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.oauth.client.controller.ClientManagementController;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.oauth.client.service.impl.ClientManagementServiceImpl;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
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

    private MockMvc mockMvc;

    @MockBean
    private ClientManagementServiceImpl clientManagementService;

    Map<String, Object> public_key;

    @Before
    public void setUp() {

        this.mockMvc = MockMvcBuilders.standaloneSetup(clientManagementController).build();

        public_key = new HashMap<>();
        public_key.put("kty","RSA");
        public_key.put("e","AQAB");
        public_key.put( "use", "sig");
        public_key.put(  "kid", "1bbdc9de-c24f-4801-b6b3-691ac07641af");
        public_key.put( "alg", "RS256");
        public_key.put(  "n","wXGQA574CU-WTWPILd4S3_1sJf0Yof0kwMeNctXc1thQo70Ljfn9f4igpRe7f8qNs_W6dLuLWemFhGJBQBQ7vvickECKNJfo_EzSD_yyPCg7k_AGbTWTkuoObHrpilwJGyKVSkOIujH_FqHIVkwkVXjWc25Lsb8Gq4nAHNQEqqgaYPLEi5evCR6S0FzcXTPuRh9zH-cM0Onjv4orrfYpEr61HcRp5MXL55b7yBoIYlXD8NfalcgdrWzp4VZHvQ8yT9G5eaf27XUn6ZBeBf7VnELcKFTyw1pK2wqoOxRBc8Y1wO6rEy8PlCU6wD-mbIzcjG1wUfnbgvJOM4A5G41quQ");

    }


    @Test
    @WithMockUser(roles = {"AUTH_PARTNER"})
    public void testCreateOIDCClient() throws Exception {
        RequestWrapper<ClientDetailCreateRequestV2> requestWrapper = new RequestWrapper<>();
        ClientDetailCreateRequestV2 clientDetailCreateRequestV2 = new ClientDetailCreateRequestV2();
        Map<String, String> clientNameLangMap = new HashMap<>();
        clientNameLangMap.put("eng", "English Client Name");
        clientNameLangMap.put("fra", "French Client Name");
        clientNameLangMap.put("ara", "Arabic Client Name");

        clientDetailCreateRequestV2.setClientNameLangMap(clientNameLangMap);
        clientDetailCreateRequestV2.setName("Mock Name");
        clientDetailCreateRequestV2.setPolicyId("policy-id");
        clientDetailCreateRequestV2.setPublicKey(public_key);
        clientDetailCreateRequestV2.setAuthPartnerId("auth-partner-id");
        clientDetailCreateRequestV2.setLogoUri("https://example.com/logo.png");
        List<String> redirectUris = List.of("https://example.com/redirect1");
        clientDetailCreateRequestV2.setRedirectUris(redirectUris);
        List<String> grantTypes = Arrays.asList("authorization_code", "refresh_token");
        clientDetailCreateRequestV2.setGrantTypes(grantTypes);
        List<String> clientAuthMethods = List.of("private_key_jwt");
        clientDetailCreateRequestV2.setClientAuthMethods(clientAuthMethods);

        requestWrapper.setRequest(clientDetailCreateRequestV2);
        String requestJson = new ObjectMapper().writeValueAsString(requestWrapper);

        mockMvc.perform(MockMvcRequestBuilders.post("/oauth/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Test
    @WithMockUser(roles = {"AUTH_PARTNER"})
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
    @WithMockUser(roles = {"AUTH_PARTNER"})
    public void testUpdateOAUTHClient() throws Exception {
        RequestWrapper<ClientDetailUpdateRequestV2> requestWrapper = new RequestWrapper<>();
        ClientDetailUpdateRequestV2 clientDetailUpdateRequestV2 = new ClientDetailUpdateRequestV2();
        Map<String, String> clientNameLangMap = new HashMap<>();
        clientNameLangMap.put("eng", "English Client Name");
        clientNameLangMap.put("fra", "French Client Name");
        clientNameLangMap.put("ara", "Arabic Client Name");

        clientDetailUpdateRequestV2.setClientNameLangMap(clientNameLangMap);
        clientDetailUpdateRequestV2.setClientName("Mock Name");
        clientDetailUpdateRequestV2.setLogoUri("https://example.com/logo.png");
        clientDetailUpdateRequestV2.setStatus("ACTIVE");
        List<String> redirectUris = List.of("https://example.com/redirect1");
        clientDetailUpdateRequestV2.setRedirectUris(redirectUris);
        List<String> grantTypes = Arrays.asList("authorization_code", "refresh_token");
        clientDetailUpdateRequestV2.setGrantTypes(grantTypes);
        List<String> clientAuthMethods = List.of("private_key_jwt");
        clientDetailUpdateRequestV2.setClientAuthMethods(clientAuthMethods);
        requestWrapper.setRequest(clientDetailUpdateRequestV2);
        String requestJson = new ObjectMapper().writeValueAsString(requestWrapper);

        mockMvc.perform(MockMvcRequestBuilders.put("/oauth/client/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test ()
    @WithMockUser(roles = {"AUTH_PARTNER"})
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

        when(clientManagementService.getClientDetails("123")).thenReturn(clientDetail);

        ResponseWrapper<io.mosip.pms.oauth.client.dto.ClientDetail> expectedResponse = new ResponseWrapper<>();
        expectedResponse.setResponse(clientDetail);

        ResponseWrapper<io.mosip.pms.oauth.client.dto.ClientDetail> actualResponse = clientManagementController.getOAuthClient("123");

        verify(clientManagementService).getClientDetails("123");
        assertEquals(expectedResponse.getClass(), actualResponse.getClass());
    }

    @Test
    @WithMockUser(roles = {"AUTH_PARTNER"})
    public void getClients() throws Exception {
        ResponseWrapperV2<List<OauthClientDto>> responseWrapper = new ResponseWrapperV2<>();
        List<OauthClientDto> oauthClientDtoList = new ArrayList<>();
        responseWrapper.setResponse(oauthClientDtoList);

        when(clientManagementService.getClients()).thenReturn(responseWrapper);

        mockMvc.perform(MockMvcRequestBuilders.get("/oauth/clients"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void getPartnersClientsTest() throws Exception {
        String sortFieldName = "createdDateTime";
        String sortType = "desc";
        Integer pageNo = 0;
        Integer pageSize = 8;
        ClientFilterDto filterDto = new ClientFilterDto();
        ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
        PageResponseV2Dto<ClientSummaryDto> pageResponse = new PageResponseV2Dto<>();
        responseWrapper.setResponse(pageResponse);

        Mockito.when(clientManagementService.getPartnersClients(sortFieldName, sortType, pageNo, pageSize, filterDto))
                .thenReturn(responseWrapper);

        mockMvc.perform(MockMvcRequestBuilders.get("/oauth/partners/clients")
                        .param("sortFieldName", sortFieldName)
                        .param("sortType", sortType)
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("partnerId", "123")
                        .param("orgName", "ABC")
                        .param("policyGroupName", "test")
                        .param("policyName", "test")
                        .param("clientName", "abc")
                        .param("status", "approved"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}