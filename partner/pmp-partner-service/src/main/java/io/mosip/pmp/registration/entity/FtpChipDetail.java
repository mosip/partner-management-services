package io.mosip.pmp.registration.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "ftp_chip_detail")
@NamedQuery(name = "FtpChipDetail.findAll", query = "SELECT a FROM FtpChipDetail a")
public class FtpChipDetail {
	@Id
	public String id;
	
	@Column(name = "foundational_trust_provider_id")
	public String foundationalTrustProviderId;
	
	
	@Column(name = "make")
	public String make;

	@Column(name = "model")
	public String model;
	
	@Column(name = "certificate_alias")
	public String certificateAlias;
	
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
