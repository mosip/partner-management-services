package io.mosip.pmp.misp.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "misp", schema = "pmp")
//@IdClass(CodeAndLanguageCodeID.class)
public class MISPEntity extends BaseEntity implements Serializable {
	

	private static final long serialVersionUID = -8541947597557590379L;
	
	@Id	
	@Column(name ="id")
	private String ID;	
	
	@Column(name ="name")
	private String name;
	
	@Column(name ="address")
	private String address;
	
	@Column(name="contact_no")
	private String contactNumber;
	
	@Column(name ="email_id")
	private String emailId;
	
	@Column(name ="user_id")
	private String userID; 

	@OneToOne(cascade = CascadeType.ALL)	
	@JoinColumns({@JoinColumn(name = "id", insertable = false, updatable = false), })
	private MISPLicenseEntity mispLicenses;

}
