package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
import io.mosip.pms.partner.controller.MultiPartnerServiceController;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.service.MultiPartnerService;
import io.mosip.pms.partner.util.RequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MultiPartnerServiceController.class})
public class MultiPartnerServiceControllerTest {

    @Autowired
    private MultiPartnerServiceController multiPartnerServiceController;

    @MockBean
    private MultiPartnerService multiPartnerService;

    @MockBean
    RequestValidator requestValidator;

    public static final String VERSION = "1.0";

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAuthPartnersPoliciesTest() throws Exception {
        ResponseWrapperV2<List<ApprovedPolicyDto>> responseWrapper = new ResponseWrapperV2<>();
        ApprovedPolicyDto policyDto = new ApprovedPolicyDto();
        policyDto.setPartnerId("abc");
        policyDto.setPolicyGroupName("123");
        List<ApprovedPolicyDto> policyDtoList = new ArrayList<>();
        policyDtoList.add(policyDto);
        responseWrapper.setResponse(policyDtoList);
        Mockito.when(multiPartnerService.getAuthPartnersPolicies()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<ApprovedPolicyDto>> response = multiPartnerServiceController.getAuthPartnersPolicies();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getApprovedPartnerIdsWithPolicyGroupsTest() throws Exception {
        ResponseWrapperV2<List<PolicyGroupDto>> responseWrapper = new ResponseWrapperV2<>();
        PolicyGroupDto policyGroupDto = new PolicyGroupDto();
        policyGroupDto.setPartnerId("abc");
        policyGroupDto.setPartnerType("Auth_Partner");
        policyGroupDto.setPolicyGroupId("123");
        policyGroupDto.setPolicyGroupName("testGroup");
        policyGroupDto.setPolicyGroupDescription("testDesc");
        List<PolicyGroupDto> policyGroupDtoList = new ArrayList<>();
        policyGroupDtoList.add(policyGroupDto);
        responseWrapper.setResponse(policyGroupDtoList);
        Mockito.when(multiPartnerService.getApprovedPartnerIdsWithPolicyGroups()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<PolicyGroupDto>> response = multiPartnerServiceController.getApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getApiKeysForAuthPartners() throws Exception {
        ResponseWrapperV2<List<ApiKeyResponseDto>> responseWrapper =  new ResponseWrapperV2<>();
        ApiKeyResponseDto apiKeyResponseDto = new ApiKeyResponseDto();
        apiKeyResponseDto.setStatus("ACTIVE");
        apiKeyResponseDto.setApiKeyLabel("test");
        apiKeyResponseDto.setPolicyId("policy123");
        apiKeyResponseDto.setPolicyName("policy123name");
        apiKeyResponseDto.setPolicyDescription("policy123desc");
        apiKeyResponseDto.setPolicyGroupId("policygroup000");
        apiKeyResponseDto.setPolicyGroupName("policygroup000name");
        apiKeyResponseDto.setPolicyGroupDescription("policygroup000desc");
        List<ApiKeyResponseDto> apiKeyResponseDtoList = new ArrayList<>();
        apiKeyResponseDtoList.add(apiKeyResponseDto);
        responseWrapper.setResponse(apiKeyResponseDtoList);
        Mockito.when(multiPartnerService.getAuthPartnerApiKeys()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<ApiKeyResponseDto>> response = multiPartnerServiceController.getAuthPartnerApiKeys();
    }


    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void approvedDeviceProviderIdsTest() throws Exception {
        ResponseWrapperV2<List<DeviceProviderDto>> responseWrapper = new ResponseWrapperV2<>();
        List<DeviceProviderDto> deviceProviderDtos = new ArrayList<>();
        DeviceProviderDto deviceProviderDto = new DeviceProviderDto();
        deviceProviderDtos.add(deviceProviderDto);
        responseWrapper.setResponse(deviceProviderDtos);
        Mockito.when(multiPartnerService.approvedDeviceProviderIds()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<DeviceProviderDto>> response = multiPartnerServiceController.approvedDeviceProviderIds();
    }

    @Test
    @WithMockUser(roles = {"FTM_PROVIDER"})
    public void approvedFTMProviderIdsTest() throws Exception {
        ResponseWrapperV2<List<FtmProviderDto>> responseWrapper = new ResponseWrapperV2<>();
        List<FtmProviderDto> ftmProviderDtoList = new ArrayList<>();
        FtmProviderDto ftmProviderDto = new FtmProviderDto();
        ftmProviderDtoList.add(ftmProviderDto);
        responseWrapper.setResponse(ftmProviderDtoList);
        Mockito.when(multiPartnerService.approvedFTMProviderIds()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<FtmProviderDto>> response = multiPartnerServiceController.approvedFTMProviderIds();
    }
}
