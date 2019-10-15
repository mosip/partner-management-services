package io.mosip.pmp.partner.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "partner_policy_request")
@Data
public class PartnerPolicyRequest {
	
	@Id
	@Column(name = "id")
	private String id;
	
	@Column(name = "part_id")
	private String part_id;
	
	@Column(name = "policy_id")
	private String policy_id;
	
	@Column(name = "request_datetimes")
	private String request_datetimes;
	
	@Column(name = "request_detail")
	private String request_detail;
	
	@Column(name = "status_code")
	private String status_code;
	
	@Column(name = "cr_by")
	private String cr_by;
	
	@Column(name = "cr_dtimes")
	private String cr_dtimes;
	
	@Column(name = "upd_by")
	private String upd_by;
	
	@Column(name = "upd_dtimes")
	private String upd_dtimes;
	
	@Column(name = "is_deleted")
	private String is_deleted;
	
	@Column(name = "del_dtimes")
	private String del_dtimes;
	
}
