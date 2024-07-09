package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapper;
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
    public void getAllCertificateDetails() throws Exception {
        CertificateDto certificateDto = new CertificateDto();
        certificateDto.setPartnerId("abc");
        certificateDto.setPartnerType("Auth_Partner");
        certificateDto.setIsCertificateAvailable(false);

        List<CertificateDto> certificateDtoList = new ArrayList<>();
        certificateDtoList.add(certificateDto);

        Mockito.when(multiPartnerService.getAllCertificateDetails()).thenReturn(certificateDtoList);
        ResponseWrapper<List<CertificateDto>> response = multiPartnerServiceController.getAllCertificateDetails();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllPoliciesRequestList() throws Exception {
        PolicyDto policyDto = new PolicyDto();
        policyDto.setPartnerId("abc");
        policyDto.setPartnerType("Auth_Partner");
        policyDto.setPolicyGroupName("123");
        policyDto.setPolicyName("test");
        List<PolicyDto> policyDtoList = new ArrayList<>();
        policyDtoList.add(policyDto);
        Mockito.when(multiPartnerService.getAllRequestedPolicies()).thenReturn(policyDtoList);
        ResponseWrapper<List<PolicyDto>> response = multiPartnerServiceController.getAllRequestedPolicies();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllApprovedPoliciesTest() throws Exception {
        ApprovedPolicyDto policyDto = new ApprovedPolicyDto();
        policyDto.setPartnerId("abc");
        policyDto.setPartnerType("Auth_Partner");
        policyDto.setPolicyGroupName("123");
        List<ApprovedPolicyDto> policyDtoList = new ArrayList<>();
        policyDtoList.add(policyDto);
        Mockito.when(multiPartnerService.getAllApprovedAuthPartnerPolicies()).thenReturn(policyDtoList);
        ResponseWrapper<List<ApprovedPolicyDto>> response = multiPartnerServiceController.getAllApprovedAuthPartnerPolicies();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllApprovedPolicyGroupsList() throws Exception {
        PolicyGroupDto policyGroupDto = new PolicyGroupDto();
        policyGroupDto.setPartnerId("abc");
        policyGroupDto.setPartnerType("Auth_Partner");
        policyGroupDto.setPolicyGroupId("123");
        policyGroupDto.setPolicyGroupName("testGroup");
        policyGroupDto.setPolicyGroupDescription("testDesc");
        List<PolicyGroupDto> policyGroupDtoList = new ArrayList<>();
        policyGroupDtoList.add(policyGroupDto);
        Mockito.when(multiPartnerService.getAllApprovedPartnerIdsWithPolicyGroups()).thenReturn(policyGroupDtoList);
        ResponseWrapper<List<PolicyGroupDto>> responseWrapper = multiPartnerServiceController.getAllApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getAllApiKeysForAuthPartners() throws Exception {
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
        Mockito.when(multiPartnerService.getAllApiKeysForAuthPartners()).thenReturn(apiKeyResponseDtoList);
        ResponseWrapper<List<ApiKeyResponseDto>> response = multiPartnerServiceController.getAllApiKeysForAuthPartners();
    }

    @Test
    public void saveUserConsentGiven() throws Exception {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        Mockito.when(multiPartnerService.saveUserConsentGiven()).thenReturn(userDetailsDto);
        ResponseWrapper<UserDetailsDto> response = multiPartnerServiceController.saveUserConsentGiven();
    }

    @Test
    public void isUserConsentGiven() throws Exception {
        UserDetailsDto userDetailsDto = new UserDetailsDto();
        Mockito.when(multiPartnerService.isUserConsentGiven()).thenReturn(userDetailsDto);
        ResponseWrapper<UserDetailsDto> response = multiPartnerServiceController.isUserConsentGiven();
    }

    @Test
    public void getConfigValuesTest() throws Exception {
        multiPartnerServiceController.getConfigValues();
    }
}
