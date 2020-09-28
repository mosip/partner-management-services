package io.mosip.pmp.policy.validator.impl;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;


import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.pmp.policy.validator.constants.PolicyValidatorErrorConstant;
import io.mosip.pmp.policy.validator.exception.InvalidPolicySchemaException;
import io.mosip.pmp.policy.validator.exception.PolicyIOException;
import io.mosip.pmp.policy.validator.spi.PolicyValidator;

@Component
public class PolicySchemaValidator implements PolicyValidator{

	private static final Logger LOGGER = LoggerFactory.getLogger(PolicySchemaValidator.class);

	
	
	@Override
	public boolean validatePolicies(String policySchema,String policies) throws InvalidPolicySchemaException, PolicyIOException {
		try {
			JsonSchema jsonSchema=getJsonSchema(policySchema);
			JSONObject json = new JSONObject(policies);
			
			  JsonNode jsonIdObjectNode = JsonLoader.fromString(json.toString());   
				ProcessingReport report = jsonSchema.validate(jsonIdObjectNode, true);
				LOGGER.debug("schema validation report generated : " + report);
			  return report.isSuccess(); 
			}catch (IOException e) {
				ExceptionUtils.logRootCause(e);
				throw new PolicyIOException(PolicyValidatorErrorConstant.SCHEMA_IO_EXCEPTION, e);
			}catch (ProcessingException e) {
				ExceptionUtils.logRootCause(e);
				throw new PolicyIOException(PolicyValidatorErrorConstant.POLICY_VALIDATION_FAILED, e);
			} 

	}

	private JsonSchema getJsonSchema(String policySchema) throws InvalidPolicySchemaException  {
		
		try {
			if (policySchema == null) {
				throw new InvalidPolicySchemaException(PolicyValidatorErrorConstant.INVALID_POLICY_SCHEMA.getErrorCode(),
						PolicyValidatorErrorConstant.INVALID_POLICY_SCHEMA.getMessage());  
			}
			JSONObject schema = new JSONObject(policySchema);
			JsonNode jsonIdSchemaNode = JsonLoader.fromString(schema.toString());
			if (jsonIdSchemaNode.size() <= 0
					|| !(jsonIdSchemaNode.hasNonNull("$schema") && jsonIdSchemaNode.hasNonNull("type"))) {
				throw new InvalidPolicySchemaException(PolicyValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getErrorCode(),
						PolicyValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getMessage()); 
			}
			final JsonSchemaFactory factory = JsonSchemaFactory.byDefault(); 
			return  factory.getJsonSchema(jsonIdSchemaNode);
		} catch (IOException | ProcessingException e) {
			throw new InvalidPolicySchemaException(PolicyValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getErrorCode(),
					PolicyValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getMessage());
		}

	
	}

}
