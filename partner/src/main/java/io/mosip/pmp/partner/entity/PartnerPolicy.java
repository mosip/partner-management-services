package io.mosip.pmp.partner.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */
@Entity
@Table(name = "partner_policy")
@Data
public class PartnerPolicy {
	
	@Id
	@Column(name = "policy_api_key")
	private String policy_api_key;
	
	@Column(name = "part_id")
	private String part_id;
	
	@Column(name = "policy_id")
	private String policy_id;
	
	@Column(name = "valid_from_datetime")
	private String valid_from_datetime;
	
	@Column(name = "valid_to_datetime")
	private String valid_to_datetime;
	
	@Column(name = "is_active")
	private String is_active;
	
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
