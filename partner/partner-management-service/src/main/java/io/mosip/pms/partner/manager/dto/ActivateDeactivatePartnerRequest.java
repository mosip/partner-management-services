package io.mosip.pms.partner.manager.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */
@Data
public class ActivateDeactivatePartnerRequest {
	
		@NotEmpty
		public String status;
}
