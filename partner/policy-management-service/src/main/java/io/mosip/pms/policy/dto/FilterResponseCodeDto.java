package io.mosip.pms.policy.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterResponseCodeDto {
	private List<ColumnCodeValue> filters;

}
