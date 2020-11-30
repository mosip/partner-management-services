package io.mosip.pmp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor()
@NoArgsConstructor
public class SearchFilter {
	private String value;

	// @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$",
	// message = "Invalid date time pattern")
	private String fromValue;

	// @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$",
	// message = "Invalid date time pattern")
	private String toValue;

	// @NotBlank
	private String columnName;

	// @NotNull
	private String type;

}
