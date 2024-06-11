package io.mosip.pms.device.validator;
//package io.mosip.pmp.authdevice.util;
//
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.ConstraintValidatorContext;
//
//import org.springframework.web.client.RestClientException;
//
//import io.mosip.kernel.core.util.EmptyCheckUtils;
//import io.mosip.pmp.authdevice.constants.RegisteredDeviceErrorCode;
//import io.mosip.pmp.authdevice.dto.DeviceData;
//import io.mosip.pmp.authdevice.dto.DeviceInfo;
//import io.mosip.pmp.authdevice.exception.RequestException;
//
//public class FoundationalValidator implements ConstraintValidator<ValidFoundational, DeviceInfo> {
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see jakarta.validation.ConstraintValidator#isValid(java.lang.Object,
//	 * jakarta.validation.ConstraintValidatorContext)
//	 */
//	@Override
//	public boolean isValid(DeviceInfo deviceInfo, ConstraintValidatorContext context) {
//		if (deviceInfo == null || deviceInfo.getCertification() == null)
//				{
//			return false;
//		} else {
//			try {
//				if (deviceInfo.getCertification().equals(RegisteredDeviceConstant.L1)) {
//					if (EmptyCheckUtils.isNullEmpty(deviceInfo.getFoundationalTrustProviderId())
//					/*
//					 * || EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustSignature()) ||
//					 * EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustCertificate())
//					 */
//					)
//						return false;
//				} else if (deviceData.getDeviceInfo().getCertification().equals(RegisteredDeviceConstant.L0)) {
//					if (EmptyCheckUtils.isNullEmpty(deviceData.getFoundationalTrustProviderId())
//					/*
//					 * || EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustSignature()) ||
//					 * EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustCertificate())
//					 */ )
//						return true;
//				} else {
//					if (EmptyCheckUtils.isNullEmpty(deviceData.getFoundationalTrustProviderId())
//					/*
//					 * || EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustSignature()) ||
//					 * EmptyCheckUtils.isNullEmpty(value.getFoundationalTrustCertificate())
//					 */ )
//						return true;
//				}
//
//			} catch (RestClientException e) {
//				throw new RequestException(RegisteredDeviceErrorCode.FOUNDATIONAL_VALUE.getErrorCode(),
//						RegisteredDeviceErrorCode.FOUNDATIONAL_VALUE.getErrorMessage() + " " + e.getMessage());
//			}
//			return true;
//		}
//	}
//}
