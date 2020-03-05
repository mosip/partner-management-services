package io.mosip.pmp.misp.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * @since 2019-Oct
 *<br/>
 *
 * Encapsulates the response details of the misp creation. Specifies the details of the created misp. 
 */
@Data
@ApiModel(value = "MISPCreateResponse", description = "MISP Craete response representation")
public class MISPCreateResponseDto {
	
	/**
	 *  Carries the misp status(Active OR De-Active) 
	 */
	private String mispStatus;
	
	/**
	 * Carries the misp id
	 */
	private String mispID;
}
