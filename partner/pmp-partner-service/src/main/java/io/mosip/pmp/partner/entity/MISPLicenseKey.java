package io.mosip.pmp.partner.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
@Embeddable
public class MISPLicenseKey implements Serializable{
	
	private static final long serialVersionUID = -1848346550159175040L;

	@NotNull
	@Column(insertable= false, updatable = false)
	private String misp_id;	
	
	@NotNull
	private String license_key;

}
