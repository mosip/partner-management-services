package io.mosip.pms.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Nagarjuna Kuchi <br/> 
 * @version 1.0 <br/>
 *  
 * Defines an object to hold the misp license details.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "misp_license")
public class MISPLicenseReadEntity extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = -8541947597557590399L;
	
	@EmbeddedId
	public MISPlKeyUniqueKeyEntity mispUniqueEntity;	
	
	@Column(name = "valid_from_date")
	public LocalDateTime validFromDate;	

	@Column(name = "valid_to_date")	
	public LocalDateTime validToDate;
}
