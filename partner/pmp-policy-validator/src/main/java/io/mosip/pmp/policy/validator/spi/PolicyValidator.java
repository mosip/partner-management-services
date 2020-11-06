package io.mosip.pmp.policy.validator.spi;

import io.mosip.pmp.policy.validator.exception.InvalidPolicySchemaException;
import io.mosip.pmp.policy.validator.exception.PolicyIOException;
import io.mosip.pmp.policy.validator.exception.PolicyObjectValidationFailedException;

public interface PolicyValidator {

	public boolean validatePolicies(String policySchema,String policies) throws InvalidPolicySchemaException, PolicyIOException, PolicyObjectValidationFailedException;
	
}
