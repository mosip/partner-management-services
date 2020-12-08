package io.mosip.pmp.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto {

	
	private List<SearchFilter> filters;

	
	private List<SearchSort> sort;

	// @NotNull
	private Pagination pagination;

	

}
