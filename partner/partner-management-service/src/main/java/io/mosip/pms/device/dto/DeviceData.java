package io.mosip.pms.device.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
