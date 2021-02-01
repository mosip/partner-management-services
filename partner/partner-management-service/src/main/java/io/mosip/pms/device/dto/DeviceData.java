package io.mosip.pms.device.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.pms.device.validator.ValidFoundational;
import io.mosip.pms.device.validator.ValidPurpose;
import lombok.Data;

@Data
@ValidFoundational(baseField = "certificationLevel", matchField = { "foundationalTrustProviderId" })
public class DeviceData {

	
	@Size(min = 1, max = 36)
	private String deviceId;

	@ValidPurpose(message = "Invalid Purpose received")
	private String purpose;
	
	@NotNull
	private String deviceInfo;	
	
	private String foundationalTrustProviderId;
}
