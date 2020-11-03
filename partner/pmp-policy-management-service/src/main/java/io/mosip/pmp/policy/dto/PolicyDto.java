package io.mosip.pmp.policy.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PolicyDto {
	
	private String policyId;
	
	private String policyName;
	
	private String policyDesc;
	
	private LocalDateTime publishDate;
	
	private LocalDateTime validTill;
	
	private String status;
	
	private String version;
	
	private String schema;
	
	private Boolean is_Active;
	
	private String cr_by;
	
	private LocalDateTime cr_dtimes;
	
	private String up_by;
	
	private LocalDateTime upd_dtimes;
	
	private String authTokenType;
	
	private List<AuthPolicyDto> allowedAuthTypes;
	
	private List<ShareableAttributesDto> shareableAttributes;
	
	private List<AllowedKycDto> allowedKYCAttributes;
	
	private DataShareDto dataSharePolicies;
}
