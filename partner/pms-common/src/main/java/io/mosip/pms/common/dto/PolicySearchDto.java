package io.mosip.pms.common.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class PolicySearchDto extends SearchDto {

	@NotNull
	public String policyType;
}
