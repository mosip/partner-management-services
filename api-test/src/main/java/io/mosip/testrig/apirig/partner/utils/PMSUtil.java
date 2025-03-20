package io.mosip.testrig.apirig.partner.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.SkipException;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;

public class PMSUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(PMSUtil.class);
	
	public static void setLogLevel() {
		if (PMSConfigManger.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	
	public static String isTestCaseValidForExecution(TestCaseDTO testCaseDTO) {
		String testCaseName = testCaseDTO.getTestCaseName();
		
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
		return jsonString;
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

		DBManager.executeDBQueries(PMSConfigManger.getKMDbUrl(), PMSConfigManger.getKMDbUser(),
				PMSConfigManger.getKMDbPass(), PMSConfigManger.getKMDbSchema(),
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
	
}