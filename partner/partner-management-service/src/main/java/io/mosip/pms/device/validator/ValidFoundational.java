package io.mosip.pms.device.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Payload;

@Documented
//@Constraint(validatedBy = FoundationalValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFoundational {
	String message() default "If certification level received is L1 then FoundationalTPId OR FoundationalTrustSignature OR FoundationalTrustCertificate should not be null or empty";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String baseField();

	String[] matchField();

}
