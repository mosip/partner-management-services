package io.mosip.pmp.authdevice.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.pmp.authdevice.util.ValidFoundational;
import io.mosip.pmp.authdevice.util.ValidPurpose;
import lombok.Data;

@Data
@ValidFoundational(baseField = "certificationLevel", matchField = { "foundationalTrustProviderId" })
public class DeviceData {

	
	@Size(min = 1, max = 36)
	private String deviceId;

	@ValidPurpose(message = "Invalid Purpose received")
	private String purpose;

	@Valid
	private DeviceInfo deviceInfo;
	@NotNull
	private String foundationalTrustProviderId;
	@NotNull
	private Boolean hotlisted;


}
