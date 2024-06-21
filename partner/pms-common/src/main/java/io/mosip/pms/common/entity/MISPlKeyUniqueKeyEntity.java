package io.mosip.pms.common.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Nagarjuna Kuchi <br/> 
 * @version 1.0 <br/>
 *  
 * Defines an object to hold the misp license unique key.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
//@Entity
@Embeddable
public class MISPlKeyUniqueKeyEntity implements Serializable {

	
	private static final long serialVersionUID = -8541947597557590399L;	
	
	@NotNull
	@Column(insertable= false, updatable = false)
	private String misp_id;	
	
	@NotNull
	private String license_key;
}
