package io.mosip.pmp.regdevice.service;

import io.mosip.pmp.authdevice.dto.ResponseDto;
import io.mosip.pmp.authdevice.dto.ValidateDeviceDto;

public interface DeviceValidationService {

	/**
	 * Validate device providers.
	 *
	 * @param validateDeviceDto the validate device dto
	 * @return {@link ResponseDto}
	 */
	public ResponseDto validateDeviceProviders(ValidateDeviceDto validateDeviceDto);
}
