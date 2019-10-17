package io.mosip.pmp.misp.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FilterType {
	
	/**
	 * field to hold the declared filter types
	 * 
	 * @return filter types
	 */
	public FilterTypeEnum[] types();
}
