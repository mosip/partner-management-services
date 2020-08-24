package io.mosip.pmp.registration.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "foundational_trust_provider")
@NamedQuery(name = "FoundationalTrustProvider.findAll", query = "SELECT a FROM FoundationalTrustProvider a")
public class FoundationalTrustProvider {
	@Id
	public String id;

	@Column(name = "partner_org_name")
	public String partnerOrgName;

	@Column(name = "is_active")
	public boolean isActive;

	@Column(name = "cr_by")
	public String crBy;

	@Column(name = "cr_dtimes")
	public Timestamp crDtimes;

	@Column(name = "upd_by")
	public String updBy;

	@Column(name = "upd_dtimes")
	public Timestamp updDtimes;

	@Column(name = "is_deleted")
	public boolean isDeleted;

	@Column(name = "del_dtimes")
	public Timestamp delDtimes;
}
