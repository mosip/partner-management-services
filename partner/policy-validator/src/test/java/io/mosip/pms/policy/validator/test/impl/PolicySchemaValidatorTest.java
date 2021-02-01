package io.mosip.pms.policy.validator.test.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.policy.validator.exception.InvalidPolicySchemaException;
import io.mosip.pms.policy.validator.exception.PolicyIOException;
import io.mosip.pms.policy.validator.exception.PolicyObjectValidationFailedException;
import io.mosip.pms.policy.validator.impl.PolicySchemaValidator;


@RunWith(MockitoJUnitRunner.class)
public class PolicySchemaValidatorTest {
	
	private ClassLoader classLoader;
	
	private String schemaJson;
	
	
	/** The osi validator. */
	@InjectMocks
	PolicySchemaValidator policySchemaValidator;
	
	@Autowired
	ObjectMapper mapper;
	
	@Before
	public void setUp() throws IOException {
		classLoader = getClass().getClassLoader();
		File schemaFile = new File(classLoader.getResource("data-share-policy-schema.json").getFile());
		InputStream is = new FileInputStream(schemaFile);
		schemaJson = IOUtils.toString(is, "UTF-8");
	}
	
	@Test
	public void testValidatePoliciesSuccess() throws IOException, InvalidPolicySchemaException, PolicyIOException, PolicyObjectValidationFailedException {
		File policyFile = new File(classLoader.getResource("sample-qr-code-policy.json").getFile());
		InputStream policyStream = new FileInputStream(policyFile);
		String policyJson = IOUtils.toString(policyStream, "UTF-8");
		boolean isValid=policySchemaValidator.validatePolicies(schemaJson, policyJson);
		assertTrue(isValid);
	}
	
	@Test(expected = InvalidPolicySchemaException.class)
	public void testInvalidPolicySchemaException() throws IOException, InvalidPolicySchemaException, PolicyIOException, PolicyObjectValidationFailedException {
		File policyFile = new File(classLoader.getResource("sample-qr-code-policy.json").getFile());
		InputStream policyStream = new FileInputStream(policyFile);
		String policyJson = IOUtils.toString(policyStream, "UTF-8");
		File schemaFile = new File(classLoader.getResource("data-share-policy-schema-invalid.json").getFile());
		InputStream is = new FileInputStream(schemaFile);
		schemaJson = IOUtils.toString(is, "UTF-8");
       policySchemaValidator.validatePolicies(schemaJson, policyJson);
	
	}
	@Test(expected = InvalidPolicySchemaException.class)
	public void testValidateSchemaNull() throws IOException, InvalidPolicySchemaException, PolicyIOException, PolicyObjectValidationFailedException {
		File policyFile = new File(classLoader.getResource("sample-qr-code-policy.json").getFile());
		InputStream policyStream = new FileInputStream(policyFile);
		String policyJson = IOUtils.toString(policyStream, "UTF-8");
         policySchemaValidator.validatePolicies(null, policyJson);

	}
	
	@Test(expected = PolicyIOException.class)
	public void testInvalidPolicy() throws IOException, InvalidPolicySchemaException, PolicyIOException, PolicyObjectValidationFailedException {
		File policyFile = new File(classLoader.getResource("sample-qr-code-policy.json").getFile());
		InputStream policyStream = new FileInputStream(policyFile);
		String policyJson = IOUtils.toString(policyStream, "UTF-8");
		File schemaFile = new File(classLoader.getResource("data-share-policy-schema-invalid2.json").getFile());
		InputStream is = new FileInputStream(schemaFile);
		schemaJson = IOUtils.toString(is, "UTF-8");
	    policySchemaValidator.validatePolicies(schemaJson, policyJson);

	}

}
