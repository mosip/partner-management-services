package io.mosip.testrig.apirig.partner.utils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.SkipException;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;
import io.restassured.response.Response;

public class PMSUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(PMSUtil.class);
	
	public static List<String> testCasesInRunScope = new ArrayList<>();
	
	public static void setLogLevel() {
		if (PMSConfigManger.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	
	public static String isTestCaseValidForExecution(TestCaseDTO testCaseDTO) {
		String testCaseName = testCaseDTO.getTestCaseName();
		currentTestCaseName = testCaseName;
		
		int indexof = testCaseName.indexOf("_");
		String modifiedTestCaseName = testCaseName.substring(indexof + 1);

		addTestCaseDetailsToMap(modifiedTestCaseName, testCaseDTO.getUniqueIdentifier());
				
		if (!testCasesInRunScope.isEmpty()
				&& testCasesInRunScope.contains(testCaseDTO.getUniqueIdentifier()) == false) {
			throw new SkipException(GlobalConstants.NOT_IN_RUN_SCOPE_MESSAGE);
		}
		
		if (SkipTestCaseHandler.isTestCaseInSkippedList(testCaseName)) {
			throw new SkipException(GlobalConstants.KNOWN_ISSUES);
		}
		return testCaseName;
	}
	
	public static String inputStringKeyWordHandeler(String jsonString, String testCaseName) {
		if (jsonString.contains("$IDPREDIRECTURI$")) {
			jsonString = replaceKeywordValue(jsonString, "$IDPREDIRECTURI$",
					ApplnURI.replace(GlobalConstants.API_INTERNAL, "healthservices") + "/userprofile");
		}
		if (jsonString.contains("$LICENSE_KEY_NAME$")) {
			String licenseKeyName = "MISP_API_Automation_" + UUID.randomUUID().toString().substring(0, 8);
			jsonString = jsonString.replace("$LICENSE_KEY_NAME$", licenseKeyName);
		    logger.info("Generated dynamic licenseKeyName: " + licenseKeyName);
		}
		if (jsonString.contains("$FUTUREDATE:") || jsonString.contains("$FUTUREDATE_YEARS:")) {
			jsonString = replaceFutureDates(jsonString);
		}
		return jsonString;
	}
	
	public static String replaceFutureDates(String json) {

	    json = processFutureDate(json, "$FUTUREDATE:", true);     // days
	    json = processFutureDate(json, "$FUTUREDATE_YEARS:", false); // years

	    return json;
	}

	public static String processFutureDate(String json, String token, boolean isDays) {

	    while (json.contains(token)) {

	        int start = json.indexOf(token) + token.length();
	        int end = json.indexOf("$", start);

	        String numberStr = json.substring(start, end);
	        
	        if (end == -1) {
	            throw new IllegalArgumentException(
	                "Invalid token format: missing closing '$' for token: " + token);
	        }
	        
	        int number;
	        try {
	        	number = Integer.parseInt(numberStr);
	        } catch (NumberFormatException e) {
	            throw new IllegalArgumentException(
	                "Invalid number inside token: " + token + numberStr + "$", e);
	        }

	        // Always convert to UTC, end of day, with .000Z
	        OffsetDateTime newDate = OffsetDateTime.now(ZoneOffset.UTC)
	                .withHour(23).withMinute(59).withSecond(59).withNano(0);

	        newDate = isDays ? newDate.plusDays(number) : newDate.plusYears(number);

	        // Format in ISO UTC with milliseconds and Z
	        DateTimeFormatter formatter =
	                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	        String isoUtc = newDate.format(formatter);

	        json = json.replace(token + numberStr + "$", isoUtc);
	    }
	    return json;
	}
	
	public static String replaceKeywordValue(String jsonString, String keyword, String value) {
		if (value != null && !value.isEmpty())
			return jsonString.replace(keyword, value);
		else {
			if (keyword.contains("$ID:"))
				throw new SkipException("Marking testcase as skipped as required field is empty " + keyword
						+ " please check the results of testcase: " + getTestCaseIDFromKeyword(keyword));
			else
				throw new SkipException("Marking testcase as skipped as required field is empty " + keyword);
		}

	}
	
	public static void DbCleanRevamp() {
		BaseTestCase.currentModule = GlobalConstants.PARTNER_MANAGEMENT_SERVICE;
		DBManager.executeDBQueries(PMSConfigManger.getPMSDbUrl(), PMSConfigManger.getPMSDbUser(),
				PMSConfigManger.getPMSDbPass(), PMSConfigManger.getPMSDbSchema(),
				getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueries.txt");
				
		DBManager.executeDBQueries(PMSConfigManger.getKeymangrDbUrl(), PMSConfigManger.getKeymangrDbUser(),
				PMSConfigManger.getKeymangrDbPass(), PMSConfigManger.getKMDbSchema(),
				getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueriesForKeyMgr.txt");

		DBManager.executeDBQueries(PMSConfigManger.getIdaDbUrl(), PMSConfigManger.getIdaDbUser(),
				PMSConfigManger.getPMSDbPass(), PMSConfigManger.getIdaDbSchema(),
				getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueriesForIDA.txt");
		
		DBManager.executeDBQueries(PMSConfigManger.getPMSDbUrl(), PMSConfigManger.getPMSDbUser(),
				PMSConfigManger.getPMSDbPass(), PMSConfigManger.getPMSDbSchema(),
				getGlobalResourcePath() + "/" + "config/pmsDataDeleteQueries.txt");
		
		DBManager.executeDBQueries(PMSConfigManger.getKMDbUrl(), PMSConfigManger.getKMDbUser(),
				PMSConfigManger.getKMDbPass(), PMSConfigManger.getKMDbSchema(),
				getGlobalResourcePath() + "/" + "config/keyManagerDataDeleteQueries.txt");
	}
	
	public void validateResponse(Response response, String testCaseName, String idKeyName) {
		if (response != null
				&& (response.asString().contains("PMS_FEATURE_001") || response.asString().contains("PMS_FEATURE_002")
						|| response.asString().contains("PMS_FEATURE_003"))) {
			if (idKeyName != null) {
				String[] fieldNames = idKeyName.split(",");
				for (String fieldName : fieldNames) {
					writeAutoGeneratedId(testCaseName, fieldName, "randomData");
				}
			}
			throw new SkipException(PMSConstants.FEATURE_NOT_SUPPORTED_PMSREVAMP);

		}
	}

	public void validateResponse(Response response, String testCaseName) {
		validateResponse(response, testCaseName, null);
	}
}