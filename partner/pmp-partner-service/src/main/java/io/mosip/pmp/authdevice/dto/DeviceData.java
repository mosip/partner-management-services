package io.mosip.pmp.authdevice.dto;

import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;

import io.mosip.pmp.authdevice.repository.RegisteredDeviceHistoryRepository;
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

	private DeviceInfo deviceInfo;

	private String foundationalTrustProviderId;
	
	private Boolean hotlisted;


}
