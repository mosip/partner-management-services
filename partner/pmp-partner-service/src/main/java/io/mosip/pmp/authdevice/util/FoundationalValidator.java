package io.mosip.pmp.authdevice.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.client.RestClientException;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pmp.authdevice.constants.RegisteredDeviceErrorCode;
import io.mosip.pmp.authdevice.dto.DeviceData;
import io.mosip.pmp.authdevice.exception.RequestException;

public class FoundationalValidator implements ConstraintValidator<ValidFoundational, DeviceData> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
	 * javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(DeviceData deviceData, ConstraintValidatorContext context) {
		if (deviceData.getDeviceInfo() == null || deviceData.getDeviceInfo().getCertification() == null
				|| deviceData.getFoundationalTrustProviderId() == null) {
			return false;
		} else {
			try {
				if (deviceData.getDeviceInfo().getCertification().equals(RegisteredDeviceConstant.L1)) {
					if (EmptyCheckUtils.isNullEmpty(deviceData.getFoundationalTrustProviderId())
					/*
					 * || EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustSignature()) ||
					 * EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustCertificate())
					 */
					)
						return false;
				} else if (deviceData.getDeviceInfo().getCertification().equals(RegisteredDeviceConstant.L0)) {
					if (EmptyCheckUtils.isNullEmpty(deviceData.getFoundationalTrustProviderId())
					/*
					 * || EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustSignature()) ||
					 * EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustCertificate())
					 */ )
						return true;
				} else {
					if (EmptyCheckUtils.isNullEmpty(deviceData.getFoundationalTrustProviderId())
					/*
					 * || EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustSignature()) ||
					 * EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustCertificate())
					 */ )
						return true;
				}

			} catch (RestClientException e) {
				throw new RequestException(RegisteredDeviceErrorCode.FOUNDATIONAL_VALUE.getErrorCode(),
						RegisteredDeviceErrorCode.FOUNDATIONAL_VALUE.getErrorMessage() + " " + e.getMessage());
			}
			return true;
		}
	}
}
