package io.mosip.pms.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor()
@NoArgsConstructor
public class SearchFilter {
	private String value;
	
	private List<String> values;

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
