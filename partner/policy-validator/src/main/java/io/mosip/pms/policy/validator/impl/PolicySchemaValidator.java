package io.mosip.pms.policy.validator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.json.JSONObject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.pms.policy.validator.constants.PolicyValidatorErrorConstant;
import io.mosip.pms.policy.validator.exception.InvalidPolicySchemaException;
import io.mosip.pms.policy.validator.exception.PolicyIOException;
import io.mosip.pms.policy.validator.exception.PolicyObjectValidationFailedException;
import io.mosip.pms.policy.validator.exception.ServiceError;
import io.mosip.pms.policy.validator.spi.PolicyValidator;

@Component
public class PolicySchemaValidator implements PolicyValidator{	

	public static final String LEVEL = "level";
	public static final String MESSAGE = "message";
	public static final String WARNING = "warning";
	public static final String INSTANCE = "instance";
	public static final String POINTER = "pointer";
	public static final String KEYWORD = "keyword";
	public static final String VALIDATORS = "validators";
	public static final String ENUM = "enum";
	public static final String AT = " at ";
	public static final String ERROR = "error";
	public static final String PATH_SEPERATOR = "/";	
	private static final String MISSING = "missing";
	private static final String UNWANTED = "unwanted";
	
	/**
	 * 
	 */
	@Override
	public boolean validatePolicies(String policySchema,String policies) throws InvalidPolicySchemaException, PolicyIOException, PolicyObjectValidationFailedException {
		try {
			JsonSchema jsonSchema=getJsonSchema(policySchema);			
			JSONObject json = new JSONObject(policies);
			
			  JsonNode jsonIdObjectNode = JsonLoader.fromString(json.toString());
				ProcessingReport report = jsonSchema.validate(jsonIdObjectNode, true);
				if(!report.isSuccess()) {
					List<ServiceError> errorList = getErrorsList(report);
					if(!errorList.isEmpty()) {						
						throw new PolicyObjectValidationFailedException(PolicyValidatorErrorConstant.POLICY_VALIDATION_FAILED, errorList);
					}
				}
			  return report.isSuccess(); 
			}catch (IOException e) {
				ExceptionUtils.logRootCause(e);
				throw new PolicyIOException(PolicyValidatorErrorConstant.SCHEMA_IO_EXCEPTION, e);
			}catch (ProcessingException e) {
				ExceptionUtils.logRootCause(e);
				throw new PolicyIOException(PolicyValidatorErrorConstant.POLICY_VALIDATION_FAILED, e);
			} 

	}

	/**
	 * 
	 * @param policySchema
	 * @return
	 * @throws InvalidPolicySchemaException
	 */
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
	
	/**
	 * 
	 * @param report
	 * @return
	 */
	private List<ServiceError> getErrorsList(ProcessingReport report) {
		List<ServiceError> errorList = new ArrayList<>();
	    for (ProcessingMessage processingMessage : report) {
	        if(LogLevel.ERROR.equals(processingMessage.getLogLevel())) {
	        	JsonNode processingMessageAsJson = processingMessage.asJson();
	        	if (processingMessageAsJson.hasNonNull(INSTANCE)
						&& processingMessageAsJson.get(INSTANCE).hasNonNull(POINTER)) {
					if (processingMessageAsJson.has(MISSING) && !processingMessageAsJson.get(MISSING).isNull()) {
						buildErrorMessages(errorList, processingMessageAsJson, PolicyValidatorErrorConstant.MISSING_INPUT_PARAMETER, MISSING);
					} else {						
						buildErrorMessages(errorList, processingMessageAsJson, PolicyValidatorErrorConstant.INVALID_INPUT_PARAMETER, UNWANTED);
					}
					if (processingMessageAsJson.hasNonNull(KEYWORD) && processingMessageAsJson.get(KEYWORD).asText().contentEquals(ENUM)) {						
						buildErrorMessages(errorList, processingMessageAsJson, PolicyValidatorErrorConstant.INVALID_INPUT_PARAMETER, KEYWORD);
					}
				}
	        } 
	    }
	    return errorList;
	}
	
	/**
	 * 
	 * @param errorList
	 * @param processingMessageAsJson
	 * @param errorConstant
	 * @param field
	 */
	private void buildErrorMessages(List<ServiceError> errorList, JsonNode processingMessageAsJson,
			PolicyValidatorErrorConstant errorConstant, String field) {
		if (processingMessageAsJson.hasNonNull(field)) {
			if (field.contentEquals(KEYWORD)) {
				errorList.add(new ServiceError(errorConstant.getErrorCode(), String.format(errorConstant.getMessage(),
						StringUtils.strip(processingMessageAsJson.get(INSTANCE).get(POINTER).asText(), "/"))));
			} else {
				StreamSupport.stream(((ArrayNode) processingMessageAsJson.get(field)).spliterator(), false)
						.forEach(
								element -> errorList
										.add(new ServiceError(errorConstant.getErrorCode(),
												String.format(errorConstant.getMessage(),
														StringUtils.strip(
																processingMessageAsJson.get(INSTANCE).get(POINTER)
																		.asText() + PATH_SEPERATOR + element.asText(),
																"/")))));
			}
		}
	}
}
