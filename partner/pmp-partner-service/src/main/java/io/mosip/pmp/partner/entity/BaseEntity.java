package io.mosip.pmp.partner.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Nagarjuna Kuchi <br/> 
 * @version 1.0 <br/>
 * @since 2019-Oct-11 <br/>
 * 
 * 
 * Defines an object to provide a wrapper for all common properties to  misp entities. <br/>
 * All the misp entities must extend from this class.<br/>
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

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

