package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.config.Config;
import io.mosip.pms.partner.controller.MultiPartnerServiceController;
import io.mosip.pms.partner.dto.*;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.response.dto.DeviceDetailResponseDto;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

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

    @MockBean
    Config config;

    public static final String VERSION = "1.0";

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
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void getAllSBIDetailsTest() throws Exception {
        List<SbiDetailsDto> sbiDetailsDtoList = new ArrayList<>();
        SbiDetailsDto sbiDetailsDto = new SbiDetailsDto();
        sbiDetailsDtoList.add(sbiDetailsDto);
        Mockito.when(multiPartnerService.getAllSBIDetails()).thenReturn(sbiDetailsDtoList);
        ResponseWrapper<List<SbiDetailsDto>> response = multiPartnerServiceController.getAllSBIDetails();
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void getAllApprovedDeviceProviderIdsTest() throws Exception {
        List<PartnerDto> partnerDtos = new ArrayList<>();
        PartnerDto partnerDto = new PartnerDto();
        partnerDtos.add(partnerDto);
        Mockito.when(multiPartnerService.getAllApprovedDeviceProviderIds()).thenReturn(partnerDtos);
        ResponseWrapper<List<PartnerDto>> response = multiPartnerServiceController.getAllApprovedDeviceProviderIds();
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

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void addInactiveDeviceMappingToSbi() throws Exception {
        RequestWrapper<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequesttime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        Mockito.when(multiPartnerService.addInactiveDeviceMappingToSbi(requestWrapper.getRequest())).thenReturn(true);
        doNothing().when(requestValidator).validateId(anyString(), anyString());
        doNothing().when(requestValidator).validate(any());
        ResponseWrapper<Boolean> response = multiPartnerServiceController.addInactiveDeviceMappingToSbi(requestWrapper);
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void getAllDevicesForSBITest() throws Exception {
        ResponseWrapper<List<DeviceDetailDto>> responseWrapper = new ResponseWrapper<>();
        List<DeviceDetailDto> deviceDetailDtoList = new ArrayList<>();
        DeviceDetailDto deviceDetailDto = new DeviceDetailDto();
        deviceDetailDtoList.add(deviceDetailDto);
        Mockito.when(multiPartnerService.getAllDevicesForSBI(Mockito.any())).thenReturn(deviceDetailDtoList);
        ResponseWrapper<List<DeviceDetailDto>> response = multiPartnerServiceController.getAllDevicesForSBI("abc");
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void deactivateDeviceTest() throws Exception {
        DeviceDetailResponseDto deviceDetailResponseDto = new DeviceDetailResponseDto();
        Mockito.when(multiPartnerService.deactivateDevice(Mockito.any())).thenReturn(deviceDetailResponseDto);
        ResponseWrapper<DeviceDetailResponseDto> response = multiPartnerServiceController.deactivateDevice("abc");
    }
}
