package io.mosip.pms.test.partner.service.impl;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.dto.UserDetails;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.service.impl.MultiPartnerServiceImpl;
import io.mosip.pms.test.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class MultiPartnerServiceImplTest {

    @InjectMocks
    MultiPartnerServiceImpl multiPartnerServiceImpl;

    @Mock
    RestUtil restUtil;

    @Mock
    PartnerServiceRepository partnerRepository;

    @Mock
    PolicyGroupRepository policyGroupRepository;

    @Mock
    AuthPolicyRepository authPolicyRepository;

    @Mock
    PartnerPolicyRepository partnerPolicyRepository;

    @Mock
    UserDetailsRepository userDetailsRepository;

    @Mock
    Environment environment;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

//    @Test
//    public void getPartnerCertificatesTest() throws Exception {
//
//        List<Partner> partnerList = new ArrayList<>();
//        Partner partner = new Partner();
//        partner.setId("123");
//        partner.setCertificateAlias("abs");
//        partnerList.add(partner);
//        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
//        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
//
//        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
//        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
//        SecurityContextHolder.setContext(securityContext);
//        when(authentication.getPrincipal()).thenReturn(authUserDetails);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//
//        Map<String, Object> apiResponse = new HashMap<>();
//        Map<String, Object> response = new HashMap<>();
//        response.put("certificateData", "-----BEGIN CERTIFICATE-----\n" +
//                "MIIFfTCCA2WgAwIBAgIUOVZNyD46U0OAEhaGC/Y7NXbu+OkwDQYJKoZIhvcNAQEL\n" +
//                "BQAwTjELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAk1IMQswCQYDVQQHDAJQTjELMAkG\n" +
//                "A1UECgwCQ0ExCzAJBgNVBAsMAkNBMQswCQYDVQQDDAJDQTAeFw0yNDA1MDkwNzI1\n" +
//                "MDJaFw0yOTA1MDkwNzI1MDJaME4xCzAJBgNVBAYTAklOMQswCQYDVQQIDAJNSDEL\n" +
//                "MAkGA1UEBwwCUE4xCzAJBgNVBAoMAkNBMQswCQYDVQQLDAJDQTELMAkGA1UEAwwC\n" +
//                "Q0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCzdWD2DvhSnmLqU3fX\n" +
//                "RT3z8ikS6qHxn5Hu/a2ijkuZxAZj0UCUJ83kM20NwocJDHT1qx6+yjdl+BECsgoI\n" +
//                "ro9MXgFOsHCphyR5KiP4mY95qRlE03h7WBfr4wDn/6f5tCbqCcBqdXMAQxUp34D+\n" +
//                "Pro0EwkXNulHNMTvz5hpoCEiGyfXUP48I4q2nb8rMXaplhqz+vAYgA4rsK6K9IUh\n" +
//                "uJDxtZRHdIfxnvbfjxDbuPkN0ehOQ1uQrDVY6ENCIUxdgR/p94kZ+CNsD21c57gJ\n" +
//                "2wYg+BceQn1rVSGnfpqMoogZCMUWFvaE4i91419VXxDLgeC/4Qw8n5onBY+dVHjW\n" +
//                "04OolR2DqotFyaPlZiVdpUys6+KZ7fS9mwWEY0kqtLzcBeb4g4nPvObfKnqSmVMZ\n" +
//                "DHRuAx6MG3oFZrnNuS6oIYGwLpoko6iqEiGohHsSxMulT43XOxoNgDq9noQc9SYv\n" +
//                "tzdzijBRLAxNBDTB0rgZra27tLIFlqP1TpqZtM3ThOmPJQn6JG8WeiVWnmUkpmXX\n" +
//                "6opGqhLWMM/u1n4fdf716h7340RbCPJoOpTPphYo/WedFQskqZvhTU6HMIj4JQAj\n" +
//                "OVVwgtrDOdx051ps2hhiSU5tL4LmjLHIsfyoCSuHkzBhVMZ/jKFm8C4Or2RRG85A\n" +
//                "wtzEANSxVZRjw6S1hsHsI+8m2QIDAQABo1MwUTAdBgNVHQ4EFgQUjDli1GMiclHK\n" +
//                "igNm2kuKh48AON8wHwYDVR0jBBgwFoAUjDli1GMiclHKigNm2kuKh48AON8wDwYD\n" +
//                "VR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAk6IWcDdBc1tngCaPNLhU\n" +
//                "c3pXRdTjDuLHMxHRiP/7Vi3V2xcKRak5ZMzYAJK6YThp3Z04V9d5jJoi/CDhMuPK\n" +
//                "RV1GmbdA7b24Jic2fQHWOJkgafT2Gx4yHmLo5ctSuDHPfSvzUgeghG0k3eNJgCai\n" +
//                "Ctr+wvCRZGvvbl2JnJUcWiHBxH/PaWJ4Jd1T4UKmhlFhTw26TXQGHuW/UJwgh8OR\n" +
//                "V8A+WeMXxKFsh38b8RnWVa6XdajIq9UAZvvd4Q16zjdnMWx/7zcIK5D1MDb/KmSJ\n" +
//                "yho1LKRZx5YtSeI4FWs8dzZ0nCCiTe7TrnnhlXThJ6rXeo5AshtM4fGrvizaf4n3\n" +
//                "7I9mJkqiccp1ml+2EcgsdX7HbnGE/R8VVbh3jUhWHuysLCiVSMbjnktCLWoXjSb9\n" +
//                "JqOYF3yo6JQslQB0fQMyKmvsn/FplQBbU0PUrg9vpAg9nZlZf3UHO5z072pXD6ky\n" +
//                "5pKjh+q0JOk00Eln9AoU6YuIyPBQ9mI3X8iYB5UhUBbgAPeg1pwWCWhdt40f0D5t\n" +
//                "JkVnICy+Gh1ps8QPA6coEaajbIq14Uh6eYEwxFHPsxlbn7pzjoCJG2v7N8VwgfuL\n" +
//                "DdGs4hFikdUAfBT/Diug/n9/ZgfdN6Ctf4U/SM65vZvfRqtLIoTIs4PcF3YtKK04\n" +
//                "m0UA3Sxxre0vVWYO4GmmZUY=\n" +
//                "-----END CERTIFICATE-----");
//        apiResponse.put("response", response);
//
//        when(environment.getProperty("pmp.partner.certificaticate.get.rest.uri")).thenReturn("uri");
//        when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
//        multiPartnerServiceImpl.getAllCertificateDetails();
//    }

    @Test(expected = PartnerServiceException.class)
    public void getPartnerCertificatesTestException() throws Exception {
        multiPartnerServiceImpl.getAllCertificateDetails();
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
        multiPartnerServiceImpl.getAllCertificateDetails();
    }

    @Test(expected = PartnerServiceException.class)
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
        multiPartnerServiceImpl.getAllCertificateDetails();
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
        multiPartnerServiceImpl.getAllRequestedPolicies();

        when(authPolicyRepository.findByPolicyGroupAndId(anyString(), anyString())).thenReturn(null);
        multiPartnerServiceImpl.getAllRequestedPolicies();
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

        multiPartnerServiceImpl.getAllRequestedPolicies();
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
        multiPartnerServiceImpl.getAllRequestedPolicies();
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
        multiPartnerServiceImpl.getAllRequestedPolicies();
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
        multiPartnerServiceImpl.getAllRequestedPolicies();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllPoliciesTestException() throws Exception {
        multiPartnerServiceImpl.getAllRequestedPolicies();
    }

    @Test(expected = PartnerServiceException.class)
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
        multiPartnerServiceImpl.getAllRequestedPolicies();
    }


    @Test
    public void getAllApprovedAuthPartnerPolicies() throws Exception {
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
        multiPartnerServiceImpl.getAllApprovedAuthPartnerPolicies();

        when(authPolicyRepository.findActivePoliciesByPolicyGroupId(anyString(), anyString())).thenReturn(null);
        multiPartnerServiceImpl.getAllApprovedAuthPartnerPolicies();
    }

    @Test
    public void getAllApprovedAuthPartnerPoliciesTest1() throws Exception {
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
        multiPartnerServiceImpl.getAllApprovedAuthPartnerPolicies();
    }

    @Test
    public void getAllApprovedAuthPartnerPoliciesTest2() throws Exception {
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
        multiPartnerServiceImpl.getAllApprovedAuthPartnerPolicies();
    }

    @Test
    public void getAllApprovedAuthPartnerPoliciesTest3() throws Exception {
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
        multiPartnerServiceImpl.getAllApprovedAuthPartnerPolicies();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllApprovedAuthPartnerPoliciesException() throws Exception {
        multiPartnerServiceImpl.getAllApprovedAuthPartnerPolicies();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllApprovedAuthPartnerPoliciesException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getAllApprovedAuthPartnerPolicies();
    }
    @Test
    public void getAllApprovedPolicyGroupsTest() throws Exception {
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
        multiPartnerServiceImpl.getAllApprovedPartnerIdsWithPolicyGroups();

        PolicyGroup policyGroup1 = new PolicyGroup();
        when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup1);
        multiPartnerServiceImpl.getAllApprovedPartnerIdsWithPolicyGroups();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllApprovedPolicyGroupsTestException() throws Exception {
        multiPartnerServiceImpl.getAllApprovedPartnerIdsWithPolicyGroups();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllApprovedPolicyGroupsTestException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getAllApprovedPartnerIdsWithPolicyGroups();
    }

    @Test
    public void getAllApiKeysForAuthPartnersTest() throws Exception{
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
        multiPartnerServiceImpl.getAllApiKeysForAuthPartners();
    }

    @Test
    public void getAllApiKeysForAuthPartnersTest1() throws Exception {
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
        multiPartnerServiceImpl.getAllApiKeysForAuthPartners();
    }

    @Test
    public void getAllApiKeysForAuthPartnersTest2() throws Exception{
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
        multiPartnerServiceImpl.getAllApiKeysForAuthPartners();

        AuthPolicy authPolicy = new AuthPolicy();
        Optional<AuthPolicy> authPolicyDetails = Optional.of(authPolicy);
        when(authPolicyRepository.findById(anyString())).thenReturn(authPolicyDetails);
        multiPartnerServiceImpl.getAllApiKeysForAuthPartners();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllApiKeysForAuthPartnersTestException() throws Exception {
        multiPartnerServiceImpl.getAllApiKeysForAuthPartners();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllApiKeysForAuthPartnersTestException1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.getAllApiKeysForAuthPartners();
    }

    @Test
    public void saveUserConsentGivenTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("123");
        userDetails.setId("abc");
        userDetails.setCrDtimes(LocalDateTime.now());
        userDetails.setCrBy("abc");
        Optional<UserDetails> optionalEntity = Optional.of(new UserDetails());
        when(userDetailsRepository.findByUserId(anyString())).thenReturn(optionalEntity);
        when(userDetailsRepository.save(any())).thenReturn(userDetails);
        multiPartnerServiceImpl.saveUserConsentGiven();
    }

    @Test
    public void saveUserConsentGivenTest1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("123");
        userDetails.setId("abc");
        userDetails.setCrDtimes(LocalDateTime.now());
        userDetails.setCrBy("abc");
        when(userDetailsRepository.save(any())).thenReturn(userDetails);
        multiPartnerServiceImpl.saveUserConsentGiven();
    }

    @Test(expected = PartnerServiceException.class)
    public void saveUserConsentGivenExceptionTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.saveUserConsentGiven();
    }
    @Test(expected = PartnerServiceException.class)
    public void saveUserConsentGivenExceptionTest1() throws Exception {
        multiPartnerServiceImpl.saveUserConsentGiven();
    }

    @Test
    public void isUserConsentGivenTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partnerList.add(partner);
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("123");
        userDetails.setId("abc");
        userDetails.setCrDtimes(LocalDateTime.now());
        userDetails.setCrBy("abc");
        userDetails.setConsentGiven("YES");
        userDetails.setConsentGivenDtimes(LocalDateTime.now());
        Optional<UserDetails> optionalEntity = Optional.of(userDetails);
        when(userDetailsRepository.findByUserId(anyString())).thenReturn(optionalEntity);
        multiPartnerServiceImpl.isUserConsentGiven();
    }

    @Test(expected = PartnerServiceException.class)
    public void isUserConsentGivenExceptionTest() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
        when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
        multiPartnerServiceImpl.isUserConsentGiven();
    }

    @Test(expected = PartnerServiceException.class)
    public void isUserConsentGivenExceptionTest1() throws Exception {
        multiPartnerServiceImpl.isUserConsentGiven();
    }

    private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
