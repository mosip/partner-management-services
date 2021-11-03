package io.mosip.pms.common.dto;

import java.util.List;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDto<T> {
	private long fromRecord;
	private long toRecord;
	private long totalRecord;
	@Valid
	private List<T> data;
}