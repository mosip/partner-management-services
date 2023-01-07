package io.mosip.pms.common.util;

import java.util.Objects;

import org.springframework.security.core.context.SecurityContextHolder;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;


public class UserDetailUtil {
	
	/**
	 * 
	 * @return
	 */
	public static AuthUserDetails getLoggedInUserDetails() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		} else {
			return null;
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static String getLoggedInUserId() {
		AuthUserDetails loggedInUser = getLoggedInUserDetails();
		return loggedInUser != null ? loggedInUser.getUserId() : null;
	}
	
	public static String getLoggedInUser() {
		String loggedInUser = getLoggedInUserId();
		if (loggedInUser != null) {
			return loggedInUser;
		}
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())) {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		}

		return null;
	}
}
