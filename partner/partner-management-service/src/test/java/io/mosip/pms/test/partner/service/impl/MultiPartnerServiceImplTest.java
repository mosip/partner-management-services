package io.mosip.pms.test.partner.service.impl;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.partner.service.impl.MultiPartnerServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiPartnerServiceImplTest {

    @Autowired
    MultiPartnerServiceImpl multiPartnerServiceImpl;

    @MockBean
    RestUtil restUtil;

    @MockBean
    PartnerServiceRepository partnerRepository;

    @MockBean
    PolicyGroupRepository policyGroupRepository;

    @MockBean
    AuthPolicyRepository authPolicyRepository;

    @MockBean
    PartnerPolicyRepository partnerPolicyRepository;

    @MockBean
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @MockBean
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @MockBean
    FTPChipDetailRepository ftpChipDetailRepository;

    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @Test
    public void getAuthPartnersPolicies() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setPolicyGroupId("abc");
        partner.setApprovalStatus("approved");
        List<PartnerPolicyRequest> partnerPolicyRequestList = new ArrayList<>();
        PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
        partnerPolicyRequest.setPolicyId("xyz");
        partnerPolicyRequest.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
        partnerPolicyRequest.setStatusCode("approved");
        partnerPolicyRequestList.add(partnerPolicyRequest);
        partner.setPartnerPolicyRequests(partnerPolicyRequestList);
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        PolicyGroup policyGroup = new PolicyGroup();
        policyGroup.setId("abc");
        policyGroup.setName("group1");
        policyGroup.setDesc("dgvhsd");
        when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);

        AuthPolicy authPolicy = new AuthPolicy();
        authPolicy.setName("policy123");
        when(authPolicyRepository.findActivePoliciesByPolicyGroupId(anyString(), anyString())).thenReturn(authPolicy);
        multiPartnerServiceImpl.getAuthPartnersPolicies();

        when(authPolicyRepository.findActivePoliciesByPolicyGroupId(anyString(), anyString())).thenReturn(null);
        multiPartnerServiceImpl.getAuthPartnersPolicies();
    }

    @Test
    public void getAuthPartnersPoliciesTest1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setPolicyGroupId("abc");
        partner.setApprovalStatus("approved");
        List<PartnerPolicyRequest> partnerPolicyRequestList = new ArrayList<>();
        partner.setPartnerPolicyRequests(partnerPolicyRequestList);
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        PolicyGroup policyGroup = new PolicyGroup();
        policyGroup.setId("abc");
        policyGroup.setName("group1");
        policyGroup.setDesc("dgvhsd");
        when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);

        AuthPolicy authPolicy = new AuthPolicy();
        authPolicy.setName("policy123");
        when(authPolicyRepository.findActivePoliciesByPolicyGroupId(anyString(), anyString())).thenReturn(authPolicy);
        multiPartnerServiceImpl.getAuthPartnersPolicies();
    }

    @Test
    public void getAuthPartnersPoliciesTest2() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("");
        partner.setPolicyGroupId("abc");
        partner.setApprovalStatus("approved");
        List<PartnerPolicyRequest> partnerPolicyRequestList = new ArrayList<>();
        partner.setPartnerPolicyRequests(partnerPolicyRequestList);
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getAuthPartnersPolicies();
    }

    @Test
    public void getAuthPartnersPoliciesTest3() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setPolicyGroupId("abc");
        partner.setApprovalStatus("");
        List<PartnerPolicyRequest> partnerPolicyRequestList = new ArrayList<>();
        partner.setPartnerPolicyRequests(partnerPolicyRequestList);
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getAuthPartnersPolicies();
    }

    @Test
    public void getAuthPartnersPoliciesException() throws Exception {
        multiPartnerServiceImpl.getAuthPartnersPolicies();
    }

    @Test
    public void getAuthPartnersPoliciesException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getAuthPartnersPolicies();
    }
    @Test
    public void getApprovedPartnerIdsWithPolicyGroupssTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setPolicyGroupId("abc");
        partner.setApprovalStatus("approved");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        PolicyGroup policyGroup = new PolicyGroup();
        policyGroup.setId("abc");
        policyGroup.setName("group1");
        policyGroup.setDesc("dgvhsd");
        when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);
        multiPartnerServiceImpl.getApprovedPartnerIdsWithPolicyGroups();

        PolicyGroup policyGroup1 = new PolicyGroup();
        when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup1);
        multiPartnerServiceImpl.getApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    public void getApprovedPartnerIdsWithPolicyGroupssTestException() throws Exception {
        multiPartnerServiceImpl.getApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    public void getApprovedPartnerIdsWithPolicyGroupssTestException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    public void approvedDeviceProviderIdsTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Device_Provider");
        partner.setApprovalStatus("approved");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

        multiPartnerServiceImpl.approvedDeviceProviderIds();
    }

    @Test
    public void approvedDeviceProviderIdsExceptionTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

        multiPartnerServiceImpl.approvedDeviceProviderIds();
    }

    @Test
    public void approvedDeviceProviderIdsExceptionTest1() throws Exception {
        multiPartnerServiceImpl.approvedDeviceProviderIds();
    }

    @Test
    public void approvedFTMProviderIdsTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("FTM_Provider");
        partner.setApprovalStatus("approved");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

        multiPartnerServiceImpl.approvedFTMProviderIds();
    }

    @Test
    public void approvedFTMProviderIdsExceptionTest() throws Exception {
        multiPartnerServiceImpl.approvedFTMProviderIds();
    }

    private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
