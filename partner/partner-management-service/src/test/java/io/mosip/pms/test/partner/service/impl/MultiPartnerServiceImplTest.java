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
    Environment environment;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @Test
    public void getPartnerCertificatesTest() throws Exception {

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setCertificateAlias("abs");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("certificateData", "-----BEGIN CERTIFICATE-----\n" +
                "MIIFfTCCA2WgAwIBAgIUOVZNyD46U0OAEhaGC/Y7NXbu+OkwDQYJKoZIhvcNAQEL\n" +
                "BQAwTjELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAk1IMQswCQYDVQQHDAJQTjELMAkG\n" +
                "A1UECgwCQ0ExCzAJBgNVBAsMAkNBMQswCQYDVQQDDAJDQTAeFw0yNDA1MDkwNzI1\n" +
                "MDJaFw0yOTA1MDkwNzI1MDJaME4xCzAJBgNVBAYTAklOMQswCQYDVQQIDAJNSDEL\n" +
                "MAkGA1UEBwwCUE4xCzAJBgNVBAoMAkNBMQswCQYDVQQLDAJDQTELMAkGA1UEAwwC\n" +
                "Q0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCzdWD2DvhSnmLqU3fX\n" +
                "RT3z8ikS6qHxn5Hu/a2ijkuZxAZj0UCUJ83kM20NwocJDHT1qx6+yjdl+BECsgoI\n" +
                "ro9MXgFOsHCphyR5KiP4mY95qRlE03h7WBfr4wDn/6f5tCbqCcBqdXMAQxUp34D+\n" +
                "Pro0EwkXNulHNMTvz5hpoCEiGyfXUP48I4q2nb8rMXaplhqz+vAYgA4rsK6K9IUh\n" +
                "uJDxtZRHdIfxnvbfjxDbuPkN0ehOQ1uQrDVY6ENCIUxdgR/p94kZ+CNsD21c57gJ\n" +
                "2wYg+BceQn1rVSGnfpqMoogZCMUWFvaE4i91419VXxDLgeC/4Qw8n5onBY+dVHjW\n" +
                "04OolR2DqotFyaPlZiVdpUys6+KZ7fS9mwWEY0kqtLzcBeb4g4nPvObfKnqSmVMZ\n" +
                "DHRuAx6MG3oFZrnNuS6oIYGwLpoko6iqEiGohHsSxMulT43XOxoNgDq9noQc9SYv\n" +
                "tzdzijBRLAxNBDTB0rgZra27tLIFlqP1TpqZtM3ThOmPJQn6JG8WeiVWnmUkpmXX\n" +
                "6opGqhLWMM/u1n4fdf716h7340RbCPJoOpTPphYo/WedFQskqZvhTU6HMIj4JQAj\n" +
                "OVVwgtrDOdx051ps2hhiSU5tL4LmjLHIsfyoCSuHkzBhVMZ/jKFm8C4Or2RRG85A\n" +
                "wtzEANSxVZRjw6S1hsHsI+8m2QIDAQABo1MwUTAdBgNVHQ4EFgQUjDli1GMiclHK\n" +
                "igNm2kuKh48AON8wHwYDVR0jBBgwFoAUjDli1GMiclHKigNm2kuKh48AON8wDwYD\n" +
                "VR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAk6IWcDdBc1tngCaPNLhU\n" +
                "c3pXRdTjDuLHMxHRiP/7Vi3V2xcKRak5ZMzYAJK6YThp3Z04V9d5jJoi/CDhMuPK\n" +
                "RV1GmbdA7b24Jic2fQHWOJkgafT2Gx4yHmLo5ctSuDHPfSvzUgeghG0k3eNJgCai\n" +
                "Ctr+wvCRZGvvbl2JnJUcWiHBxH/PaWJ4Jd1T4UKmhlFhTw26TXQGHuW/UJwgh8OR\n" +
                "V8A+WeMXxKFsh38b8RnWVa6XdajIq9UAZvvd4Q16zjdnMWx/7zcIK5D1MDb/KmSJ\n" +
                "yho1LKRZx5YtSeI4FWs8dzZ0nCCiTe7TrnnhlXThJ6rXeo5AshtM4fGrvizaf4n3\n" +
                "7I9mJkqiccp1ml+2EcgsdX7HbnGE/R8VVbh3jUhWHuysLCiVSMbjnktCLWoXjSb9\n" +
                "JqOYF3yo6JQslQB0fQMyKmvsn/FplQBbU0PUrg9vpAg9nZlZf3UHO5z072pXD6ky\n" +
                "5pKjh+q0JOk00Eln9AoU6YuIyPBQ9mI3X8iYB5UhUBbgAPeg1pwWCWhdt40f0D5t\n" +
                "JkVnICy+Gh1ps8QPA6coEaajbIq14Uh6eYEwxFHPsxlbn7pzjoCJG2v7N8VwgfuL\n" +
                "DdGs4hFikdUAfBT/Diug/n9/ZgfdN6Ctf4U/SM65vZvfRqtLIoTIs4PcF3YtKK04\n" +
                "m0UA3Sxxre0vVWYO4GmmZUY=\n" +
                "-----END CERTIFICATE-----");
        apiResponse.put("response", response);

        when(environment.getProperty("pmp.partner.certificaticate.get.rest.uri")).thenReturn("uri");
        when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
        multiPartnerServiceImpl.getPartnerCertificates();
    }

    @Test
    public void getPartnerCertificatesTestException() throws Exception {
        multiPartnerServiceImpl.getPartnerCertificates();
    }

    @Test
    public void getPartnerCertificatesTestException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("");
        partner.setCertificateAlias("");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getPartnerCertificates();
    }

    @Test
    public void getPartnerCertificatesTestException2() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getPartnerCertificates();
    }

    @Test
    public void getAllPoliciesTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("AUTH");
        partner.setPolicyGroupId("abc");
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

        String policyGroupName = "test";
        AuthPolicy authPolicy = new AuthPolicy();
        authPolicy.setName("policy123");
        when(policyGroupRepository.findPolicyGroupNameById(anyString())).thenReturn(policyGroupName);
        when(authPolicyRepository.findByPolicyGroupAndId(anyString(), anyString())).thenReturn(authPolicy);
        multiPartnerServiceImpl.getPolicyRequests();

        when(authPolicyRepository.findByPolicyGroupAndId(anyString(), anyString())).thenReturn(null);
        multiPartnerServiceImpl.getPolicyRequests();
    }

    @Test
    public void getAllPoliciesTest1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("AUTH");
        partner.setPolicyGroupId("abc");
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
        when(authPolicyRepository.findByPolicyGroupAndId(anyString(), anyString())).thenReturn(authPolicy);

        multiPartnerServiceImpl.getPolicyRequests();
    }

    @Test
    public void getAllPoliciesTest2() throws Exception {
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
        multiPartnerServiceImpl.getPolicyRequests();
    }

    @Test
    public void getAllPoliciesTest3() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setPolicyGroupId("abc");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getPolicyRequests();
    }

    @Test
    public void getAllPoliciesTest4() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("ddd");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setPolicyGroupId("");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getPolicyRequests();
    }

    @Test
    public void getAllPoliciesTestException() throws Exception {
        multiPartnerServiceImpl.getPolicyRequests();
    }

    @Test
    public void getAllPoliciesTestException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getPolicyRequests();
    }


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
    public void getApiKeysForAuthPartnersTest() throws Exception{
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

        PartnerPolicy partnerPolicy = new PartnerPolicy();
        partnerPolicy.setPolicyApiKey("apikey123");
        partnerPolicy.setLabel("request");
        partnerPolicy.setIsActive(true);
        partnerPolicy.setPartner(partner);
        partnerPolicy.setPolicyId("test");
        List<PartnerPolicy> partnerPolicies = new ArrayList<>();
        partnerPolicies.add(partnerPolicy);
        when(partnerPolicyRepository.findAPIKeysByPartnerId(anyString())).thenReturn(partnerPolicies);

        AuthPolicy authPolicy = new AuthPolicy();
        authPolicy.setId("test");
        authPolicy.setName("hjfvhd");
        authPolicy.setDescr("tttt");
        PolicyGroup policyGroup = new PolicyGroup();
        policyGroup.setId("abc");
        policyGroup.setName("group1");
        policyGroup.setDesc("group1d");
        authPolicy.setPolicyGroup(policyGroup);
        Optional<AuthPolicy> authPolicyDetails = Optional.of(authPolicy);
        when(authPolicyRepository.findById(anyString())).thenReturn(authPolicyDetails);
        multiPartnerServiceImpl.getApiKeysForAuthPartners();
    }

    @Test
    public void getApiKeysForAuthPartnersTest1() throws Exception {
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

        PartnerPolicy partnerPolicy = new PartnerPolicy();
        partnerPolicy.setPolicyApiKey("apikey123");
        partnerPolicy.setLabel("request");
        partnerPolicy.setIsActive(false);
        partnerPolicy.setPartner(partner);
        partnerPolicy.setPolicyId("test");
        List<PartnerPolicy> partnerPolicies = new ArrayList<>();
        partnerPolicies.add(partnerPolicy);
        when(partnerPolicyRepository.findAPIKeysByPartnerId(anyString())).thenReturn(partnerPolicies);

        AuthPolicy authPolicy = new AuthPolicy();
        authPolicy.setId("test");
        authPolicy.setName("hjfvhd");
        authPolicy.setDescr("tttt");
        PolicyGroup policyGroup = new PolicyGroup();
        policyGroup.setId("abc");
        policyGroup.setName("group1");
        policyGroup.setDesc("group1d");
        authPolicy.setPolicyGroup(policyGroup);
        Optional<AuthPolicy> authPolicyDetails = Optional.of(authPolicy);
        when(authPolicyRepository.findById(anyString())).thenReturn(authPolicyDetails);
        multiPartnerServiceImpl.getApiKeysForAuthPartners();
    }

    @Test
    public void getApiKeysForAuthPartnersTest2() throws Exception{
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

        PartnerPolicy partnerPolicy = new PartnerPolicy();
        partnerPolicy.setPolicyApiKey("apikey123");
        partnerPolicy.setLabel("request");
        partnerPolicy.setIsActive(true);
        partnerPolicy.setPartner(partner);
        partnerPolicy.setPolicyId("test");
        List<PartnerPolicy> partnerPolicies = new ArrayList<>();
        partnerPolicies.add(partnerPolicy);
        when(partnerPolicyRepository.findAPIKeysByPartnerId(anyString())).thenReturn(partnerPolicies);
        multiPartnerServiceImpl.getApiKeysForAuthPartners();

        AuthPolicy authPolicy = new AuthPolicy();
        PolicyGroup policyGroup = new PolicyGroup();
        authPolicy.setPolicyGroup(policyGroup);
        when(authPolicyRepository.findById(any())).thenReturn(Optional.of(authPolicy));
        multiPartnerServiceImpl.getApiKeysForAuthPartners();
    }

    @Test
    public void getApiKeysForAuthPartnersTestException() throws Exception {
        multiPartnerServiceImpl.getApiKeysForAuthPartners();
    }

    @Test
    public void getApiKeysForAuthPartnersTestException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getApiKeysForAuthPartners();
    }

    @Test
    public void sbiDetailsTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Device_Provider");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        List<SecureBiometricInterface> secureBiometricInterfaceList = new ArrayList<>();
        SecureBiometricInterface secureBiometricInterface = new SecureBiometricInterface();
        secureBiometricInterface.setSwCreateDateTime(LocalDateTime.now());
        secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now());
        secureBiometricInterface.setPartnerOrgName("ABC");
        secureBiometricInterface.setApprovalStatus("approved");
        secureBiometricInterface.setActive(true);
        secureBiometricInterface.setCrDtimes(LocalDateTime.now());
        secureBiometricInterfaceList.add(secureBiometricInterface);
        secureBiometricInterface.setSwVersion("1.0");
        when(secureBiometricInterfaceRepository.findByProviderId(anyString())).thenReturn(secureBiometricInterfaceList);
        List<DeviceDetailSBI> deviceDetailSBIList = new ArrayList<>();
        DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
        deviceDetailSBIList.add(deviceDetailSBI);
        when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiId(anyString(), anyString())).thenReturn(deviceDetailSBIList);

        multiPartnerServiceImpl.sbiDetails();
    }

    @Test
    public void sbiDetailsExceptionTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

        multiPartnerServiceImpl.sbiDetails();
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
    public void ftmChipDetailsTest() throws Exception {

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("FTM_Provider");
        partner.setApprovalStatus("approved");
        partner.setCertificateAlias("abs");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<FTPChipDetail> ftpChipDetailList = new ArrayList<>();
        FTPChipDetail ftpChipDetail = new FTPChipDetail();
        ftpChipDetail.setFtpChipDetailId("xxx");
        ftpChipDetail.setFtpProviderId("123");
        ftpChipDetail.setMake("make");
        ftpChipDetail.setModel("model");
        ftpChipDetail.setApprovalStatus("approved");
        ftpChipDetail.setActive(true);
        ftpChipDetail.setCrDtimes(LocalDateTime.now());
        ftpChipDetail.setCertificateAlias("");
        ftpChipDetailList.add(ftpChipDetail);
        when(ftpChipDetailRepository.findByProviderId(anyString())).thenReturn(ftpChipDetailList);
        multiPartnerServiceImpl.ftmChipDetails();
    }

    @Test
    public void ftmChipDetailsExceptionTest() throws Exception {
        multiPartnerServiceImpl.ftmChipDetails();
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
