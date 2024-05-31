package io.mosip.pms.test.partner.service.impl;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.partner.dto.PolicyDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
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
        multiPartnerServiceImpl.getAllCertificateDetails();
    }

    @Test(expected = PartnerServiceException.class)
    public void getPartnerCertificatesTestException() throws Exception {
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
        AuthPolicy authPolicy = new AuthPolicy();
        authPolicy.setName("policy123");
        when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);
        when(authPolicyRepository.findByPolicyGroupAndId(anyString(), anyString())).thenReturn(authPolicy);

        multiPartnerServiceImpl.getAllPolicies();
    }

    @Test(expected = PartnerServiceException.class)
    public void getAllPoliciesTestException() throws Exception {
        multiPartnerServiceImpl.getAllPolicies();
    }

    private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
