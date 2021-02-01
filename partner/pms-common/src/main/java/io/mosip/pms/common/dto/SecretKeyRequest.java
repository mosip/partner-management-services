package io.mosip.pms.common.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class SecretKeyRequest {
	public String clientId;
	public String secretKey;
	public String appId;
}
