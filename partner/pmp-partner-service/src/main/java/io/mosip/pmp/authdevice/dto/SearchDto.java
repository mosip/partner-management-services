package io.mosip.pmp.authdevice.dto;

import java.util.List;

import io.mosip.pmp.authdevice.constants.Purpose;
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

	private Purpose purpose;
}
