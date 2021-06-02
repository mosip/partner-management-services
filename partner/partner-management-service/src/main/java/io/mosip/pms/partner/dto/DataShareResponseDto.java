package io.mosip.pms.partner.dto;

import java.util.List;

import io.mosip.pms.common.request.dto.ErrorResponse;
import lombok.Data;

@Data
public class DataShareResponseDto {

	/** The id. */
	private String id;
	
	/** The ver. */
	private String version;
	
	/** The timestamp. */
	private String responsetime;
	
	 private DataShareDto dataShare;
	 
	 private List<ErrorResponse> errors;
}
