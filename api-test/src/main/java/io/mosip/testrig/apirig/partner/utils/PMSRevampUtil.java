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

public class PMSRevampUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(PMSRevampUtil.class);
	
	public static void setLogLevel() {
		if (PMSRevampConfigManger.IsDebugEnabled())
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
	
	public static void DbCleanRevamp() {
		BaseTestCase.currentModule = GlobalConstants.PARTNERNEW;
		DBManager.executeDBQueries(PMSRevampConfigManger.getPMSDbUrl(), PMSRevampConfigManger.getPMSDbUser(),
				PMSRevampConfigManger.getPMSDbPass(), PMSRevampConfigManger.getPMSDbSchema(),
				getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueries.txt");

		DBManager.executeDBQueries(PMSRevampConfigManger.getKMDbUrl(), PMSRevampConfigManger.getKMDbUser(),
				PMSRevampConfigManger.getKMDbPass(), PMSRevampConfigManger.getKMDbSchema(),
				getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueriesForKeyMgr.txt");

		DBManager.executeDBQueries(PMSRevampConfigManger.getIdaDbUrl(), PMSRevampConfigManger.getIdaDbUser(),
				PMSRevampConfigManger.getPMSDbPass(), PMSRevampConfigManger.getIdaDbSchema(),
				getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueriesForIDA.txt");
	}
	
	public static String inputstringKeyWordHandeler(String jsonString, String testCaseName) {
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
}
