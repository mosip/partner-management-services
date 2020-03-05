package io.mosip.pmp.misp.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Nagarjuna Kuchi <br/> 
 * @version 1.0 <br/>
 *  
 *   Defines an object to hold the misp license details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "misp_license", schema = "pmp")
public class MISPLicenseEntity extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = -8541947597557590399L;
	
	@Id
	@Column(insertable= false, updatable = false)
	private String misp_id;	
	
	private String license_key;
	
	@Column(name = "valid_from_date")
	private LocalDateTime validFromDate;	

	@Column(name = "valid_to_date")	
	private LocalDateTime validToDate;
	
}
