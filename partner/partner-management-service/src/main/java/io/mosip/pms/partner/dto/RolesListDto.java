package io.mosip.pms.partner.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @since 1.2.0
 */
public class RolesListDto implements Serializable {
	private static final long serialVersionUID = -5863653796023079898L;

	List<Role> roles;

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
