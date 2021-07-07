package io.mosip.pms.common.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


import lombok.Data;

@Data
public class FilterValueDto {
	
	@NotNull
	@Valid
	private List<FilterDto> filters;

	private List<SearchFilter> optionalFilters;
}
