package io.mosip.pmp.authdevice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchSort {

	private String sortField;

	private String sortType;
}

