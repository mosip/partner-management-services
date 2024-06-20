package io.mosip.pms.common.entity;

import java.io.Serializable;
import jakarta.persistence.*;

/**
 * The primary key class for the partner_h database table.
 * 
 */
@Embeddable
public class PartnerHPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eff_dtimes")
	private java.util.Date effDtimes;

	public PartnerHPK() {
	}
	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public java.util.Date getEffDtimes() {
		return this.effDtimes;
	}
	public void setEffDtimes(java.util.Date effDtimes) {
		this.effDtimes = effDtimes;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PartnerHPK)) {
			return false;
		}
		PartnerHPK castOther = (PartnerHPK)other;
		return 
			this.id.equals(castOther.id)
			&& this.effDtimes.equals(castOther.effDtimes);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.id.hashCode();
		hash = hash * prime + this.effDtimes.hashCode();
		
		return hash;
	}
}