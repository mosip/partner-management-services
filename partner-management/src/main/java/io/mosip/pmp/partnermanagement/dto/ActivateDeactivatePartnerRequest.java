package io.mosip.pmp.partnermanagement.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */
@Data
public class ActivateDeactivatePartnerRequest {
	
		@NotEmpty
		private String status;
}
