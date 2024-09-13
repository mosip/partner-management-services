package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
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
    public void getAllCertificateDetails() throws Exception {
        ResponseWrapperV2<List<CertificateDto>> responseWrapper = new ResponseWrapperV2<>();

        CertificateDto certificateDto = new CertificateDto();
        certificateDto.setPartnerId("abc");
        certificateDto.setPartnerType("Auth_Partner");
        certificateDto.setIsCertificateAvailable(false);

        List<CertificateDto> certificateDtoList = new ArrayList<>();
        certificateDtoList.add(certificateDto);

        responseWrapper.setResponse(certificateDtoList);
        Mockito.when(multiPartnerService.getAllCertificateDetails()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<CertificateDto>> response = multiPartnerServiceController.getAllCertificateDetails();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllPoliciesRequestList() throws Exception {
        ResponseWrapperV2<List<PolicyDto>> responseWrapper = new ResponseWrapperV2<>();
        PolicyDto policyDto = new PolicyDto();
        policyDto.setPartnerId("abc");
        policyDto.setPartnerType("Auth_Partner");
        policyDto.setPolicyGroupName("123");
        policyDto.setPolicyName("test");
        List<PolicyDto> policyDtoList = new ArrayList<>();
        policyDtoList.add(policyDto);
        responseWrapper.setResponse(policyDtoList);
        Mockito.when(multiPartnerService.getAllRequestedPolicies()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<PolicyDto>> response = multiPartnerServiceController.getAllRequestedPolicies();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllApprovedPoliciesTest() throws Exception {
        ResponseWrapperV2<List<ApprovedPolicyDto>> responseWrapper = new ResponseWrapperV2<>();
        ApprovedPolicyDto policyDto = new ApprovedPolicyDto();
        policyDto.setPartnerId("abc");
        policyDto.setPartnerType("Auth_Partner");
        policyDto.setPolicyGroupName("123");
        List<ApprovedPolicyDto> policyDtoList = new ArrayList<>();
        policyDtoList.add(policyDto);
        responseWrapper.setResponse(policyDtoList);
        Mockito.when(multiPartnerService.getAllApprovedAuthPartnerPolicies()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<ApprovedPolicyDto>> response = multiPartnerServiceController.getAllApprovedAuthPartnerPolicies();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllApprovedPolicyGroupsList() throws Exception {
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
        Mockito.when(multiPartnerService.getAllApprovedPartnerIdsWithPolicyGroups()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<PolicyGroupDto>> response = multiPartnerServiceController.getAllApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllApiKeysForAuthPartners() throws Exception {
        ResponseWrapperV2<List<ApiKeyResponseDto>> responseWrapper =  new ResponseWrapperV2<>();
        ApiKeyResponseDto apiKeyResponseDto = new ApiKeyResponseDto();
        apiKeyResponseDto.setStatus("ACTIVE");
        apiKeyResponseDto.setApiKeyLabel("test");
        apiKeyResponseDto.setPolicyId("policy123");
        apiKeyResponseDto.setPolicyName("policy123name");
        apiKeyResponseDto.setPolicyNameDescription("policy123desc");
        apiKeyResponseDto.setPolicyGroupId("policygroup000");
        apiKeyResponseDto.setPolicyGroupName("policygroup000name");
        apiKeyResponseDto.setPolicyGroupDescription("policygroup000desc");
        List<ApiKeyResponseDto> apiKeyResponseDtoList = new ArrayList<>();
        apiKeyResponseDtoList.add(apiKeyResponseDto);
        responseWrapper.setResponse(apiKeyResponseDtoList);
        Mockito.when(multiPartnerService.getAllApiKeysForAuthPartners()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<ApiKeyResponseDto>> response = multiPartnerServiceController.getAllApiKeysForAuthPartners();
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void sbiDetailsTest() throws Exception {
        ResponseWrapperV2<List<SbiDetailsDto>> responseWrapper = new ResponseWrapperV2<>();
        List<SbiDetailsDto> sbiDetailsDtoList = new ArrayList<>();
        SbiDetailsDto sbiDetailsDto = new SbiDetailsDto();
        sbiDetailsDtoList.add(sbiDetailsDto);
        responseWrapper.setResponse(sbiDetailsDtoList);
        Mockito.when(multiPartnerService.sbiDetails()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<SbiDetailsDto>> response = multiPartnerServiceController.sbiDetails();
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
    public void saveUserConsentGiven() throws Exception {
        ResponseWrapperV2<UserDetailsDto> responseWrapper = new ResponseWrapperV2<>();
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        responseWrapper.setResponse(userDetailsDto);
        Mockito.when(multiPartnerService.saveUserConsentGiven()).thenReturn(responseWrapper);
        ResponseWrapperV2<UserDetailsDto> response = multiPartnerServiceController.saveUserConsentGiven();
    }

    @Test
    public void isUserConsentGiven() throws Exception {
        ResponseWrapperV2<UserDetailsDto> responseWrapper = new ResponseWrapperV2<>();
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        responseWrapper.setResponse(userDetailsDto);
        Mockito.when(multiPartnerService.isUserConsentGiven()).thenReturn(responseWrapper);
        ResponseWrapperV2<UserDetailsDto> response = multiPartnerServiceController.isUserConsentGiven();
    }

    @Test
    public void getConfigValuesTest() throws Exception {
        multiPartnerServiceController.getConfigValues();
    }

    @Test
    @WithMockUser(roles = {"FTM_PROVIDER"})
    public void ftmChipDetailsTest() throws Exception {
        ResponseWrapperV2<List<FtmChipDetailsDto>> responseWrapper = new ResponseWrapperV2<>();
        List<FtmChipDetailsDto> ftmChipDetailsDtoList = new ArrayList<>();
        FtmChipDetailsDto ftmChipDetailsDto = new FtmChipDetailsDto();
        ftmChipDetailsDtoList.add(ftmChipDetailsDto);
        responseWrapper.setResponse(ftmChipDetailsDtoList);
        Mockito.when(multiPartnerService.ftmChipDetails()).thenReturn(responseWrapper);
        ResponseWrapperV2<List<FtmChipDetailsDto>> response = multiPartnerServiceController.ftmChipDetails();
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
