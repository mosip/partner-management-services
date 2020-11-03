package io.mosip.pmp.authdevice.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.client.RestClientException;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pmp.authdevice.constants.RegisteredDeviceErrorCode;
import io.mosip.pmp.authdevice.exception.RequestException;
import lombok.Data;

@Data
public class CertificateLevelValidator implements ConstraintValidator<ValidCertificateLevel, String> {
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
	 * javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(String certificationLevel, ConstraintValidatorContext context) {
		if (EmptyCheckUtils.isNullEmpty(certificationLevel) || certificationLevel.trim().length() > 3) {
			return false;
		} else {
			try {

				for (String string : RegisteredDeviceConstant.CERTIFICATELEVELARR) {
					if (certificationLevel.equals(string)) {
						return true;
					}
				}
			} catch (RestClientException e) {
				throw new RequestException(
						RegisteredDeviceErrorCode.CERTIFICATION_LEVEL_VALIDATION_EXCEPTION.getErrorCode(),
						RegisteredDeviceErrorCode.CERTIFICATION_LEVEL_VALIDATION_EXCEPTION.getErrorMessage() + " "
								+ e.getMessage());
			}
			return false;
		}
	}
}

