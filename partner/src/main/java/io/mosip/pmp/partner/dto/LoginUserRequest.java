package io.mosip.pmp.partner.dto;

import lombok.Data;

@Data
public class LoginUserRequest {
		
	private String password;
	private String appId;
	private String userName;
	
}
