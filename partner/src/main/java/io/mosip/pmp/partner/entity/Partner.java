
package io.mosip.pmp.partner.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * Entity class for PartnerId generator.
 * @author sanjeev.shrivastava
 *
 */
@Entity
@Table(name = "partner")
@Data
public class Partner {

	/**
	 * The partnerId generated.
	 */
	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "policy_group_id")
	private String policy_group_id;

	@Column(name = "name")
	private String name;

	@Column(name = "address")
	private String address;

	@Column(name = "contact_no")
	private String contact_no;
	
	@Column(name = "email_id")
	private String email_id;

	@Column(name = "public_key")
	private String public_key;
	
	@Column(name = "user_id")
	private String user_id;
	
	@Column(name = "is_active")
	private String is_active;

}