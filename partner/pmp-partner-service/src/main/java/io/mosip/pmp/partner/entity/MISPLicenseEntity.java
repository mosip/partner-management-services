package io.mosip.pmp.partner.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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
@Table(name = "misp_license")
public class MISPLicenseEntity extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = -8541947597557590399L;	
	
	@EmbeddedId
	public MISPLicenseKey mispLicenseUniqueKey;	
	
	@Column(name = "valid_from_date")
	private LocalDateTime validFromDate;	

	@Column(name = "valid_to_date")	
	private LocalDateTime validToDate;
	
}
