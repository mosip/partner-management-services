package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.controller.MultiPartnerServiceController;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.service.MultiPartnerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class MultiPartnerServiceControllerTest {

    @InjectMocks
    private MultiPartnerServiceController multiPartnerServiceController;

    @Mock
    private MultiPartnerService multiPartnerService;

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getPartnerCertificates() throws Exception {
        CertificateDto certificateDto = new CertificateDto();
        certificateDto.setPartnerId("abc");
        certificateDto.setPartnerType("Auth_Partner");
        certificateDto.setIsCertificateAvailable(false);

        List<CertificateDto> certificateDtoList = new ArrayList<>();
        certificateDtoList.add(certificateDto);

        Mockito.when(multiPartnerService.getPartnerCertificates()).thenReturn(certificateDtoList);
        ResponseWrapperV2<List<CertificateDto>> response = multiPartnerServiceController.getPartnerCertificates();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getPolicyRequestsTest() throws Exception {
        PolicyDto policyDto = new PolicyDto();
        policyDto.setPartnerId("abc");
        policyDto.setPartnerType("Auth_Partner");
        policyDto.setPolicyGroupName("123");
        policyDto.setPolicyName("test");
        List<PolicyDto> policyDtoList = new ArrayList<>();
        policyDtoList.add(policyDto);
        Mockito.when(multiPartnerService.getPolicyRequests()).thenReturn(policyDtoList);
        ResponseWrapperV2<List<PolicyDto>> response = multiPartnerServiceController.getPolicyRequests();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAuthPartnerPoliciesTest() throws Exception {
        ApprovedPolicyDto policyDto = new ApprovedPolicyDto();
        policyDto.setPartnerId("abc");
        policyDto.setPolicyGroupName("123");
        List<ApprovedPolicyDto> policyDtoList = new ArrayList<>();
        policyDtoList.add(policyDto);
        Mockito.when(multiPartnerService.getAuthPartnerPolicies()).thenReturn(policyDtoList);
        ResponseWrapperV2<List<ApprovedPolicyDto>> response = multiPartnerServiceController.getAuthPartnerPolicies();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getApprovedPartnerIdsWithPolicyGroupsTest() throws Exception {
        PolicyGroupDto policyGroupDto = new PolicyGroupDto();
        policyGroupDto.setPartnerId("abc");
        policyGroupDto.setPartnerType("Auth_Partner");
        policyGroupDto.setPolicyGroupId("123");
        policyGroupDto.setPolicyGroupName("testGroup");
        policyGroupDto.setPolicyGroupDescription("testDesc");
        List<PolicyGroupDto> policyGroupDtoList = new ArrayList<>();
        policyGroupDtoList.add(policyGroupDto);
        Mockito.when(multiPartnerService.getApprovedPartnerIdsWithPolicyGroups()).thenReturn(policyGroupDtoList);
        ResponseWrapperV2<List<PolicyGroupDto>> responseWrapper = multiPartnerServiceController.getApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getApiKeysForAuthPartners() throws Exception {
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
        Mockito.when(multiPartnerService.getApiKeysForAuthPartners()).thenReturn(apiKeyResponseDtoList);
        ResponseWrapperV2<List<ApiKeyResponseDto>> response = multiPartnerServiceController.getApiKeysForAuthPartners();
    }

    @Test
    public void saveUserConsent() throws Exception {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        Mockito.when(multiPartnerService.saveUserConsent()).thenReturn(userDetailsDto);
        ResponseWrapperV2<UserDetailsDto> response = multiPartnerServiceController.saveUserConsent();
    }

    @Test
    public void isUserConsentGiven() throws Exception {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        Mockito.when(multiPartnerService.isUserConsentGiven()).thenReturn(userDetailsDto);
        ResponseWrapperV2<UserDetailsDto> response = multiPartnerServiceController.isUserConsentGiven();
    }

    @Test
    public void getConfigValuesTest() throws Exception {
        multiPartnerServiceController.getConfigValues();
    }
}
