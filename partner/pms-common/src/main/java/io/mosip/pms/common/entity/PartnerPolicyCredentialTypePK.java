package io.mosip.pms.common.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PartnerPolicyCredentialTypePK  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4823244343998154358L;
	
	@Column(name="part_id", insertable=false, updatable=false)
	private String partId;

	@Column(name="policy_id", insertable=false, updatable=false)
	private String policyId;

	@Column(name="credential_type")
	private String credentialType;

	public PartnerPolicyCredentialTypePK() {
	}
	public String getPartId() {
		return this.partId;
	}
	public void setPartId(String partId) {
		this.partId = partId;
	}
	public String getPolicyId() {
		return this.policyId;
	}
	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}
	public String getCredentialType() {
		return this.credentialType;
	}
	public void setCredentialType(String credentialType) {
		this.credentialType = credentialType;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PartnerPolicyCredentialTypePK)) {
			return false;
		}
		PartnerPolicyCredentialTypePK castOther = (PartnerPolicyCredentialTypePK)other;
		return 
			this.partId.equals(castOther.partId)
			&& this.policyId.equals(castOther.policyId)
			&& this.credentialType.equals(castOther.credentialType);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.partId.hashCode();
		hash = hash * prime + this.policyId.hashCode();
		hash = hash * prime + this.credentialType.hashCode();
		
		return hash;
	}

}
