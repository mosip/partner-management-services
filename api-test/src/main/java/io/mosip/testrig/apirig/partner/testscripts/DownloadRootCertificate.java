package io.mosip.testrig.apirig.partner.testscripts;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import io.mosip.testrig.apirig.dto.OutputValidationDto;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.partner.utils.PMSRevampConfigManger;
import io.mosip.testrig.apirig.partner.utils.PMSRevampConstants;
import io.mosip.testrig.apirig.partner.utils.PMSRevampUtil;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.testrunner.JsonPrecondtion;
import io.mosip.testrig.apirig.utils.AdminTestException;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.AuthenticationTestException;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.ReportUtil;
import io.mosip.testrig.apirig.utils.RestClient;
import io.restassured.response.Response;

public class DownloadRootCertificate extends PMSRevampUtil implements ITest {
	private static final Logger logger = Logger.getLogger(DownloadRootCertificate.class);
	protected String testCaseName = "";
	public Response response = null;
	public boolean auditLogCheck = false;

	@BeforeClass
	public static void setLogLevel() {
		if (PMSRevampConfigManger.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}

	/**
	 * get current testcaseName
	 */
	@Override
	public String getTestName() {
		return testCaseName;
	}

	/**
	 * Data provider class provides test case list
	 * 
	 * @return object of data provider
	 */
	@DataProvider(name = "testcaselist")
	public Object[] getTestCaseList(ITestContext context) {
		String ymlFile = context.getCurrentXmlTest().getLocalParameters().get("ymlFile");
		logger.info("Started executing yml: " + ymlFile);
		return getYmlTestData(ymlFile);
	}

	/**
	 * Test method for OTP Generation execution
	 * 
	 * @param objTestParameters
	 * @param testScenario
	 * @param testcaseName
	 * @throws AuthenticationTestException
	 * @throws AdminTestException
	 */
	@Test(dataProvider = "testcaselist")
	public void test(TestCaseDTO testCaseDTO) throws AuthenticationTestException, AdminTestException {
		testCaseName = testCaseDTO.getTestCaseName();
		testCaseName = PMSRevampUtil.isTestCaseValidForExecution(testCaseDTO);
		if (HealthChecker.signalTerminateExecution) {
			throw new SkipException(
					GlobalConstants.TARGET_ENV_HEALTH_CHECK_FAILED + HealthChecker.healthCheckFailureMapS);
		}
		auditLogCheck = testCaseDTO.isAuditLogCheck();
		
		
		
		response = getWithPathParamAndCookie(ApplnURI + "/v1/partnermanager/trust-chain-certificates",
				getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate()), auditLogCheck, COOKIENAME,
				"partneradmin", testCaseDTO.getTestCaseName());
		validateResponse(response, testCaseName);
		String responseBody = response.getBody().asString();
		JSONObject jsonObject = new JSONObject(responseBody);
		JSONArray dataArray = jsonObject.getJSONObject("response").getJSONArray("data");

		String certId = "";

		if (dataArray.length() > 0) {
			JSONObject firstDataObject = dataArray.getJSONObject(0);
			certId = firstDataObject.getString("certId");
			System.out.println("First certId: " + certId);
		} else {
			System.out.println("No data available.");
		}

		if (certId != null) {
			System.out.println("Using certId in another place: " + certId);
		}
		String url = "/v1/partnermanager/trust-chain-certificates/" + certId + "/certificateFile";

		response = getWithPathParamAndCookie(ApplnURI + url,
				getJsonFromTemplate(testCaseDTO.getInput(), testCaseDTO.getInputTemplate()), auditLogCheck, COOKIENAME,
				"partneradmin", testCaseDTO.getTestCaseName());
		validateResponse(response, testCaseName);

		Map<String, List<OutputValidationDto>> ouputValid = null;
		if (testCaseName.contains("_StatusCode")) {

			OutputValidationDto customResponse = customStatusCodeResponse(String.valueOf(response.getStatusCode()),
					testCaseDTO.getOutput());

			ouputValid = new HashMap<>();
			ouputValid.put(GlobalConstants.EXPECTED_VS_ACTUAL, List.of(customResponse));
		} else {
			ouputValid = OutputValidationUtil.doJsonOutputValidation(response.asString(),
					getJsonFromTemplate(testCaseDTO.getOutput(), testCaseDTO.getOutputTemplate()), testCaseDTO,
					response.getStatusCode());
		}

		Reporter.log(ReportUtil.getOutputValidationReport(ouputValid));

		if (!OutputValidationUtil.publishOutputResult(ouputValid)) {
			if (response.asString().contains("IDA-OTA-001"))
				throw new AdminTestException(
						"Exceeded number of OTP requests in a given time, Increase otp.request.flooding.max-count");
			else
				throw new AdminTestException("Failed at otp output validation");
		}
	}
		

	/**
	 * The method ser current test name to result
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		try {
			Field method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}
}
