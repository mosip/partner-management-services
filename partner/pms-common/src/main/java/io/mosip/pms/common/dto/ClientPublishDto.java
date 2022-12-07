package io.mosip.pms.common.dto;

import java.util.List;

import lombok.Data;

@Data
public class ClientPublishDto {

	private String clientId;
	
	private String clientName;
	
	private String clientStatus;
	
	private List<String> userClaims;
	
	private List<String> authContextRefs;
	
	private List<String> clientAuthMethods;
}
