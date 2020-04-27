package io.mosip.pmp.partnermanagement.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PartnersPolicyMappingRequest {
	
		@NotNull(message = "Please provide oldPolicyID")
		public String oldPolicyID;
		@NotNull(message = "Please provide newPolicyID")
		public String newPolicyID;
}
