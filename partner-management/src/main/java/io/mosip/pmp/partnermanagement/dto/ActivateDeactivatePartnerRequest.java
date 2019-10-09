package io.mosip.pmp.partnermanagement.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */
@Data
public class ActivateDeactivatePartnerRequest {
	
		@NotNull
		private String status;
}
