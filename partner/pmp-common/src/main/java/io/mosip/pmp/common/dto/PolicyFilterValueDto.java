package io.mosip.pmp.common.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class PolicyFilterValueDto extends FilterValueDto {

	@NotNull
	public String policyType;
}
