package io.mosip.pms.common.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Nagarjuna Kuchi <br/> 
 * @version 1.0 <br/>
 * @since 2019-Oct-11 <br/>
 * 
 * 
 * Defines an object to hold the misp details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "misp")
public class MISPEntity extends BaseEntity implements Serializable {
	

	private static final long serialVersionUID = -8541947597557590379L;
	
	@Id	
	@Column(name ="id")	
	public String ID;	
	
	@Column(name ="name")
	public String name;
	
	@Column(name ="address")
	public String address;
	
	@Column(name="contact_no")
	public String contactNumber;
	
	@Column(name ="email_id")
	public String emailId;
	
	@Column(name ="user_id")
	public String userID;
	
	@Column(name ="status_code")
	public String status_code;
}
