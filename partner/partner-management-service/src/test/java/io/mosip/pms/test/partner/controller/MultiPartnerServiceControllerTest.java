package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.controller.MultiPartnerServiceController;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.dto.PolicyDto;
import io.mosip.pms.partner.dto.PolicyGroupDto;
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
        Mockito.when(multiPartnerService.getAllPolicies()).thenReturn(policyDtoList);
        ResponseWrapper<List<PolicyDto>> response = multiPartnerServiceController.getAllPolicies();
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
}
