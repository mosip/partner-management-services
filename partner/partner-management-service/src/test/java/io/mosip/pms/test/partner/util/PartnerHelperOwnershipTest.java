package io.mosip.pms.test.partner.util;

import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.util.PartnerHelper;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PartnerHelperOwnershipTest {

	@InjectMocks
	private PartnerHelper partnerHelper;

	@Mock
	private SearchHelper searchHelper;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@Mock
	private AuthUserDetails authUserDetails;

	@Before
	public void setUp() {
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		// Normal behaviour: the ownership check fails for any other partner id.
		when(searchHelper.isLoggedInUserMismatch("other-partner")).thenReturn(true);
		ReflectionTestUtils.setField(partnerHelper, "ownershipExemptServiceRoles",
				List.of("CREDENTIAL_REQUEST", "CREDENTIAL_ISSUANCE"));
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void givenRoles(String... roles) {
		Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();
		for (String role : roles) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		when(authUserDetails.getAuthorities()).thenReturn((Collection) authorities);
	}

	@Test
	public void serviceRoleSkipsOwnershipCheck() {
		givenRoles("CREDENTIAL_REQUEST");
		partnerHelper.validateLoggedInUserAuthorizationOrServiceRole("other-partner");
	}

	@Test
	public void serviceRoleWithRolePrefixSkipsOwnershipCheck() {
		givenRoles("ROLE_CREDENTIAL_ISSUANCE");
		partnerHelper.validateLoggedInUserAuthorizationOrServiceRole("other-partner");
	}

	@Test(expected = PartnerServiceException.class)
	public void otherRoleStillFailsOwnershipCheck() {
		givenRoles("PARTNER");
		partnerHelper.validateLoggedInUserAuthorizationOrServiceRole("other-partner");
	}

	@Test(expected = PartnerServiceException.class)
	public void emptyPropertyKeepsOwnershipCheck() {
		ReflectionTestUtils.setField(partnerHelper, "ownershipExemptServiceRoles", List.of());
		givenRoles("CREDENTIAL_REQUEST");
		partnerHelper.validateLoggedInUserAuthorizationOrServiceRole("other-partner");
	}
}
