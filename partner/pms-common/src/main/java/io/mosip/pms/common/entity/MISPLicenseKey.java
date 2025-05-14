package io.mosip.pms.common.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class MISPLicenseKey implements Serializable{
	
	private static final long serialVersionUID = -1848346550159175040L;

	@NotNull
	@Column(insertable= false, updatable = false)
	private String misp_id;	
	
	@NotNull
	private String license_key;

}
