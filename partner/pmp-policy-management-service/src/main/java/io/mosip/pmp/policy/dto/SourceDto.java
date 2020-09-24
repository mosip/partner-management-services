package io.mosip.pmp.policy.dto;

import java.util.List;

import lombok.Data;

@Data
public class SourceDto {
	
	private String attribute;
	
	private List<FilterDto> filter;

}
