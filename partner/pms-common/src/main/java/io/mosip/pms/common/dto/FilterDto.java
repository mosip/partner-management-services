package io.mosip.pms.common.dto;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.mosip.pms.common.validator.FilterColumn;
import io.mosip.pms.common.validator.FilterColumnEnum;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FilterDto {

	@NotBlank
	private String columnName;

	@NotNull
	@FilterColumn(columns = { FilterColumnEnum.ALL, FilterColumnEnum.UNIQUE, FilterColumnEnum.EMPTY })
	private String type = FilterColumnEnum.UNIQUE.toString();

	private String text;

	@Builder
	private FilterDto(String columnName, String type, String text) {
		this.columnName = columnName;
		this.type = Optional.ofNullable(type).orElse(this.type);
		this.text = text;
	}
}
