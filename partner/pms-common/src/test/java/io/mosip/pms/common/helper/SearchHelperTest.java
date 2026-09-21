package io.mosip.pms.common.helper;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.exception.RequestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SearchHelperTest {

	private SearchHelper searchHelper;

	@Before
	public void setUp() {
		searchHelper = new SearchHelper();
		ReflectionTestUtils.setField(searchHelper, "requiredroles", List.of("PARTNER_ADMIN"));
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void mockLoggedInUser(String userId, String... roles) {
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId(userId);
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, userId);
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		for (String role : roles) {
			authorities.add(new SimpleGrantedAuthority(role));
		}
		authUserDetails.addRoleAuthorities(authorities);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(authUserDetails, null, authorities));
	}

	@Test
	public void isLoggedInUserFilterRequired_whenCallerIsPartnerAdmin_returnsFalse() {
		mockLoggedInUser("admin-1", "PARTNER_ADMIN");
		assertFalse(searchHelper.isLoggedInUserFilterRequired());
	}

	@Test
	public void isLoggedInUserFilterRequired_whenCallerIsStandardPartnerRole_returnsTrue() {
		mockLoggedInUser("partner-1", "PARTNER");
		assertTrue(searchHelper.isLoggedInUserFilterRequired());
	}

	@Test
	public void isLoggedInUserFilterRequired_whenCallerHasMultipleNonAdminRoles_returnsTrue() {
		mockLoggedInUser("partner-1", "AUTH_PARTNER", "CREDENTIAL_PARTNER");
		assertTrue(searchHelper.isLoggedInUserFilterRequired());
	}

	@Test
	public void isLoggedInUserFilterRequired_whenCallerHasNoAuthorities_returnsTrue() {
		mockLoggedInUser("partner-1");
		assertTrue(searchHelper.isLoggedInUserFilterRequired());
	}

	@Test
	public void isLoggedInUserFilterRequired_whenCallerIsOneOfMultipleAdminRoles_returnsFalse() {
		ReflectionTestUtils.setField(searchHelper, "requiredroles", List.of("PARTNER_ADMIN", "RESIDENT"));
		mockLoggedInUser("resident-1", "RESIDENT");
		assertFalse(searchHelper.isLoggedInUserFilterRequired());
	}

	private SearchFilter partnerIdFilter(String value) {
		SearchFilter filter = new SearchFilter();
		filter.setColumnName("partnerId");
		filter.setValue(value);
		return filter;
	}

	@Test(expected = RequestException.class)
	public void validateLoggedInUserFilter_whenNonAdminFiltersOnAnotherUser_throws() {
		mockLoggedInUser("authp25", "PARTNER");
		searchHelper.validateLoggedInUserFilter(List.of(partnerIdFilter("auth-p1")), "partnerId");
	}

	@Test
	public void validateLoggedInUserFilter_whenNonAdminFiltersOnOwnId_doesNotThrow() {
		mockLoggedInUser("authp25", "PARTNER");
		searchHelper.validateLoggedInUserFilter(List.of(partnerIdFilter("authp25")), "partnerId");
	}

	@Test
	public void validateLoggedInUserFilter_whenNoFilterOnThatColumn_doesNotThrow() {
		mockLoggedInUser("authp25", "PARTNER");
		SearchFilter unrelated = new SearchFilter();
		unrelated.setColumnName("status");
		unrelated.setValue("true");
		searchHelper.validateLoggedInUserFilter(List.of(unrelated), "partnerId");
	}

	@Test
	public void validateLoggedInUserFilter_whenCallerIsAdmin_doesNotThrowEvenOnMismatch() {
		mockLoggedInUser("admin-1", "PARTNER_ADMIN");
		searchHelper.validateLoggedInUserFilter(List.of(partnerIdFilter("someone-else")), "partnerId");
	}

	@Test
	public void search_withPartnerIdColumn_whenNonAdminFiltersOnAnotherUser_throws() {
		mockLoggedInUser("authp25", "PARTNER");
		io.mosip.pms.common.dto.SearchDto searchDto = new io.mosip.pms.common.dto.SearchDto();
		searchDto.setFilters(new ArrayList<>(List.of(partnerIdFilter("auth-p1"))));
		searchDto.setSort(new ArrayList<>());
		searchDto.setPagination(new io.mosip.pms.common.dto.Pagination(0, 10));
		try {
			searchHelper.search(TestPartnerScopedEntity.class, searchDto, "partnerId");
			org.junit.Assert.fail("Expected RequestException");
		} catch (RequestException expected) {
			// expected: caller-supplied partnerId doesn't match the logged-in user
		}
	}

	@jakarta.persistence.Entity
	private static class TestPartnerScopedEntity {
		@jakarta.persistence.Id
		private String id;
		private String partnerId;
	}
}
