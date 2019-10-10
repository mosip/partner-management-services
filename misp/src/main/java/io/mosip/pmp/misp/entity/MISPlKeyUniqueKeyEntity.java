package io.mosip.pmp.misp.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
