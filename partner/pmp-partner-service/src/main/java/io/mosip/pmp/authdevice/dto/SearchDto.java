package io.mosip.pmp.authdevice.dto;

import java.util.List;

import lombok.Data;

@Data
public class SearchDto {

	private List<SearchFilter> filters;

	private List<SearchSort> sort;
	
	private Pagination pagination;
}
