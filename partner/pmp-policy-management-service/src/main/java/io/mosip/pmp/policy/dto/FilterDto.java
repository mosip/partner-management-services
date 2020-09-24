package io.mosip.pmp.policy.dto;

import java.util.List;

import lombok.Data;

@Data
public class FilterDto {
	
	private String type;
	
	private List<String> subType;

}
