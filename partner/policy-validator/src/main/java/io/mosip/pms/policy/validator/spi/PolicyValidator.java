package io.mosip.pms.policy.validator.spi;

import io.mosip.pms.policy.validator.exception.InvalidPolicySchemaException;
import io.mosip.pms.policy.validator.exception.PolicyIOException;
import io.mosip.pms.policy.validator.exception.PolicyObjectValidationFailedException;

public interface PolicyValidator {

	public boolean validatePolicies(String policySchema,String policies) throws InvalidPolicySchemaException, PolicyIOException, PolicyObjectValidationFailedException;
	
}
