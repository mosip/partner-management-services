package io.mosip.pms.common.dto;

import lombok.Data;

@Data
public class FilterData {
	private String fieldCode;
	private String fieldValue;

	public FilterData(String fieldCode, String fieldValue) {
		this.fieldCode = fieldCode;
		this.fieldValue = fieldValue;
	}
}
