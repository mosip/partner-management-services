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
}
