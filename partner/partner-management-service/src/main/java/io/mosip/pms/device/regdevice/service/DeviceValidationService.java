package io.mosip.pms.device.regdevice.service;

import io.mosip.pms.device.request.dto.ValidateDeviceDto;
import io.mosip.pms.device.response.dto.ResponseDto;

public interface DeviceValidationService {

	/**
	 * Validate device providers.
	 *
	 * @param validateDeviceDto the validate device dto
	 * @return {@link ResponseDto}
	 */
	public ResponseDto validateDeviceProviders(ValidateDeviceDto validateDeviceDto);
}
