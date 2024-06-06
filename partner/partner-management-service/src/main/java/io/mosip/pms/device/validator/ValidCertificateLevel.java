package io.mosip.pms.device.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CertificateLevelValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCertificateLevel {
	String message() default "Certificate Level not supported";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}