package io.mosip.pms.partner.request.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.mosip.pms.common.dto.SearchDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class PartnerSearchDto extends SearchDto {

	@NotBlank
	@NotNull
	private String partnerType;
	
}
