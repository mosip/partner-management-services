package io.mosip.testrig.apirig.partner.utils;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.SkipException;

import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.dto.TestCaseDTO;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.RestClient;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;
import io.restassured.response.Response;

public class PMSRevampUtil extends AdminTestUtil {

	private static final Logger logger = Logger.getLogger(PMSRevampUtil.class);
	
	public static void setLogLevel() {
		if (PMSRevampConfigManger.IsDebugEnabled())
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.ERROR);
	}
	
	private static String keyManagerEnvVersion = "";
	public static boolean isKeyManagerLatest = false;

	public static boolean isKeyManagerTargetEnvVersionValid() {
	    // Only fetch if not already retrieved
	    if (keyManagerEnvVersion.isEmpty() && !isKeyManagerLatest == false) {
	        try {
	            String url = ApplnURI + PMSRevampConfigManger.getproperty("keyManagerAuditActuatorEndpoint");

	            // Make the GET request
	            Response response = RestClient.getRequest(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

	            // Parse response
	            org.json.JSONObject responseJson = new org.json.JSONObject(response.getBody().asString());

	            // Extract version
	            keyManagerEnvVersion = responseJson.getJSONObject("build").getString("version");

	            // Check if version is >= 1.1.5.5
	            isKeyManagerLatest = isVersionGreaterOrEqual(keyManagerEnvVersion, "1.1.5.5");

	        } catch (Exception e) {
	            logger.error(GlobalConstants.EXCEPTION_STRING_2 + e);
	        }
	    }

	    return isKeyManagerLatest;
	}

	private static boolean isVersionGreaterOrEqual(String version1, String version2) {
	    // Remove any suffixes like "-SNAPSHOT" from the versions
	    version1 = version1.split("-")[0];
	    version2 = version2.split("-")[0];

	    String[] v1 = version1.split("\\.");
	    String[] v2 = version2.split("\\.");

	    int length = Math.max(v1.length, v2.length);

	    for (int i = 0; i < length; i++) {
	        int v1Part = i < v1.length ? Integer.parseInt(v1[i]) : 0;
	        int v2Part = i < v2.length ? Integer.parseInt(v2[i]) : 0;

	        if (v1Part < v2Part) {
	            return false;
	        } else if (v1Part > v2Part) {
	            return true;
	        }
	    }
	    return true; // versions are equal
	}	
	
	public static String isTestCaseValidForExecution(TestCaseDTO testCaseDTO) {
		String testCaseName = testCaseDTO.getTestCaseName();
		
		if (SkipTestCaseHandler.isTestCaseInSkippedList(testCaseName)) {
			throw new SkipException(GlobalConstants.KNOWN_ISSUES);
		}
		
		if ((testCaseName.equals("Pms_UploadCACertificate_with_invalid_data")
				|| testCaseName.equals("Pms_CreateOIDCClient_ForNeg_all_Valid_sid")
				|| testCaseName.equals("Pms_CreateOIDCClient_all_Valid_Smoke_sid")
				|| testCaseName.equals("Pms_GetOriginalPartnerCertificates_DeviceProvider_allValid_smoke")
				|| testCaseName.equals("Pms_GetOriginalPartnerCertificates_allValid_smoke")
				|| testCaseName.equals("Pms_GetOriginalPartnerCertificates_Before_Uploading_Partner_Cert_Neg")
				|| testCaseName.equals("Pms_GetOriginalPartnerCertificates_forPendingforCertUpload_Neg")
				|| testCaseName.equals("Pms_GetOriginalPartnerCertificates_allValid_smoke")
				|| testCaseName.equals("Pms_GetAllOauthClient_All_Valid_Smoke")
				|| testCaseName.equals("Pms_GetOriginalFtmCertifacte_AfterDeactiveFtm_Neg")
				|| testCaseName.equals("Pms_DownloadRootCertificate_allValid_smoke")
				|| testCaseName.equals("Pms_UpdateOIDCClient_all_Valid_Smoke_sid")
				|| testCaseName.equals("Pms_DeactivateOIDCClient_Missing_clientNameLangMap_Neg")
				|| testCaseName.equals("Pms_DeactivateOIDCClient_Missing_clientName_Neg")
				|| testCaseName.equals("Pms_DeactivateOIDCClient_Missing_logoUri_Neg")
				|| testCaseName.equals("Pms_DeactivateOIDCClient_Missing_redirectUris_Neg")
				|| testCaseName.equals("Pms_DeactivateOIDCClient_Missing_status_Neg")
				|| testCaseName.equals("Pms_DeactivateOIDCClient_Retry_with_Deactivated_ClientId_Neg")
				|| testCaseName.equals("Pms_DeactivateOIDCClient_all_Valid_Smoke_sid"))) {
			throw new SkipException(PMSRevampConstants.FEATURE_NOT_SUPPORTED_PMSREVAMP);
		}
		
		
		
		return testCaseName;
}
	
	public static void DbCleanRevamp() {
		BaseTestCase.currentModule = GlobalConstants.PARTNERNEW;
		DBManager.executeDBQueries(PMSRevampConfigManger.getPMSDbUrl(), PMSRevampConfigManger.getPMSDbUser(),
				PMSRevampConfigManger.getPMSDbPass(), PMSRevampConfigManger.getPMSDbSchema(),
				getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueries.txt");

		if (!isKeyManagerTargetEnvVersionValid()) {
			DBManager.executeDBQueries(PMSRevampConfigManger.getKeymangrDbUrl(), PMSRevampConfigManger.getKeymangrDbUser(),
					PMSRevampConfigManger.getKeymangrDbPass(), PMSRevampConfigManger.getKeymangrDbSchema(),
					getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueriesForKeyMgr.txt");
		} else {
			DBManager.executeDBQueries(PMSRevampConfigManger.getKMDbUrl(), PMSRevampConfigManger.getKMDbUser(),
					PMSRevampConfigManger.getKMDbPass(), PMSRevampConfigManger.getKMDbSchema(),
					getGlobalResourcePath() + "/" + "config/partnerRevampDataDeleteQueriesForKeyMgr.txt");
		}

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
