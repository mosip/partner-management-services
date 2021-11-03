package io.mosip.pms.partner.dto;

import java.io.Serializable;

/**
 * @since 1.2.0
 */
public class Role implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2706412783263468725L;

	private String roleId;

	private String roleName;

	private String roleDescription;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDescription() {
		return roleDescription;
	}

	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

}
