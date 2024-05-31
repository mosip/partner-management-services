package io.mosip.pms.device.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.web.client.RestClientException;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.device.constant.RegisteredDeviceErrorCode;
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
