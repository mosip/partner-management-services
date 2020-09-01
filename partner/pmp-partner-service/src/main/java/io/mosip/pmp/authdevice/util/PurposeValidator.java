package io.mosip.pmp.authdevice.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.client.RestClientException;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pmp.authdevice.constants.RegisteredDeviceErrorCode;
import io.mosip.pmp.authdevice.exception.RequestException;
import lombok.Data;
@Data
public class PurposeValidator implements ConstraintValidator<ValidPurpose, String>{
	@Override
	public boolean isValid(String statusCode, ConstraintValidatorContext context) {
		if (EmptyCheckUtils.isNullEmpty(statusCode)) {
			return false;
		} else {
			try {

				for (String string : RegisteredDeviceConstant.PURPOSEARR) {
					if (statusCode.equalsIgnoreCase(string)) {
						return true;
					}
				}
			} catch (RestClientException e) {
				throw new RequestException(RegisteredDeviceErrorCode.PURPOSE_VALIDATION_EXCEPTION.getErrorCode(),
						RegisteredDeviceErrorCode.PURPOSE_VALIDATION_EXCEPTION.getErrorMessage() + " "
								+ e.getMessage());
			}
			return false;
		}
	}
}
