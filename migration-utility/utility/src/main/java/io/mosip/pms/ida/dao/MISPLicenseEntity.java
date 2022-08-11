package io.mosip.pms.ida.dao;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
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
@Entity
@Table(name = "misp_license")
public class MISPLicenseEntity implements Serializable {
	
	private static final long serialVersionUID = -8541947597557590399L;	
	
	@Id	
	@Column(name = "misp_id")	
	private String mispId;	
	
	@NotNull
    @Column(name = "license_key")
	private String licenseKey;
	
	@Column(name = "valid_from_date")
	private LocalDateTime validFromDate;	

	@Column(name = "valid_to_date")	
	private LocalDateTime validToDate;
	
	@Column(name = "is_active")
	public Boolean isActive;

	@Column(name = "cr_by", nullable = false, length = 256)
	public String createdBy;

	@Column(name = "cr_dtimes", nullable = false) 
	public LocalDateTime createdDateTime;

	@Column(name = "upd_by", length = 256)
	public String updatedBy;

	@Column(name = "upd_dtimes")
	public LocalDateTime updatedDateTime;

	@Column(name = "is_deleted")
	public Boolean isDeleted;

	@Column(name = "del_dtimes")
	public LocalDateTime deletedDateTime;

	
}
