package io.mosip.testrig.apirig.partner.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.TestNG;

import io.mosip.testrig.apirig.dataprovider.BiometricDataProvider;
import io.mosip.testrig.apirig.dbaccess.DBManager;
import io.mosip.testrig.apirig.partner.utils.PMSConfigManager;
import io.mosip.testrig.apirig.partner.utils.PMSUtil;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.testrunner.ExtractResource;
import io.mosip.testrig.apirig.testrunner.HealthChecker;
import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.AuthTestsUtil;
import io.mosip.testrig.apirig.utils.CertsUtil;
import io.mosip.testrig.apirig.utils.DependencyResolver;
import io.mosip.testrig.apirig.utils.GlobalConstants;
import io.mosip.testrig.apirig.utils.GlobalMethods;
import io.mosip.testrig.apirig.utils.JWKKeyUtil;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.apirig.utils.KeyCloakUserAndAPIKeyGeneration;
import io.mosip.testrig.apirig.utils.KeycloakUserManager;
import io.mosip.testrig.apirig.utils.MispPartnerAndLicenseKeyGeneration;
import io.mosip.testrig.apirig.utils.OutputValidationUtil;
import io.mosip.testrig.apirig.utils.PartnerRegistration;
import io.mosip.testrig.apirig.utils.SkipTestCaseHandler;

/**
 * Class to initiate mosip api test execution
 * 
 * @author Vignesh
 *
 */
public class MosipTestRunner {
	private static final Logger LOGGER = Logger.getLogger(MosipTestRunner.class);
	private static String cachedPath = null;
	private static String generateDependency;

	public static String jarUrl = MosipTestRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath();

	/**
	 * C Main method to start mosip test execution
	 * 
	 * @param arg
	 */
	public static void main(String[] arg) {

		try {
			LOGGER.info("** ------------- API Test Rig Run Started --------------------------------------------- **");

			BaseTestCase.setRunContext(getRunType(), jarUrl);
			ExtractResource.removeOldMosipTestTestResource();
			if (getRunType().equalsIgnoreCase("JAR")) {
				ExtractResource.extractCommonResourceFromJar();
			} else {
				ExtractResource.copyCommonResources();
			}
			AdminTestUtil.init();
			PMSConfigManager.init();
			suiteSetup(getRunType());
			SkipTestCaseHandler.loadTestcaseToBeSkippedList("testCaseSkippedList.txt");
			GlobalMethods.setModuleNameAndReCompilePattern(PMSConfigManager.getproperty("moduleNamePattern"));
			setLogLevels();

			HealthChecker healthcheck = new HealthChecker();
			healthcheck.setCurrentRunningModule(GlobalConstants.PARTNER_MANAGEMENT_SERVICE);
			Thread trigger = new Thread(healthcheck);
			trigger.start();

			KeycloakUserManager.removeUser();
			KeycloakUserManager.createUsers();
			KeycloakUserManager.closeKeycloakInstance();
			
			generateDependency = PMSConfigManager.getproperty("generateDependencyJson");

			if (!"yes".equalsIgnoreCase(generateDependency)) {

				String testCasesToExecute = PMSConfigManager.getproperty("testCasesToExecute");
				LOGGER.info("Testcases to execute as per config: " + testCasesToExecute);

				if (testCasesToExecute != null && !testCasesToExecute.isBlank()) {
					DependencyResolver
							.loadDependencies(getGlobalResourcePath() + "/config/testCaseInterDependency.json");

					PMSUtil.testCasesInRunScope = DependencyResolver.getDependencies(testCasesToExecute);
				}
			}

			startTestRunner();
			PMSUtil.DbCleanRevamp();
		} catch (Exception e) {
			LOGGER.error("Exception " + e.getMessage());
		}

		KeycloakUserManager.removeUser();
		KeycloakUserManager.closeKeycloakInstance();
		
		HealthChecker.bTerminate = true;
		
		// Used for generating the test case interdependency JSON file
		if ("yes".equalsIgnoreCase(generateDependency)) {
			LOGGER.info("Generating test case inter-dependencies");
			AdminTestUtil.generateTestCaseInterDependencies(BaseTestCase.getTestCaseInterDependencyPath());
		} else {
			LOGGER.info("Skipping dependency generation");
		}
		
		System.exit(0);

	}

	public static void suiteSetup(String runType) {
		if (PMSConfigManager.IsDebugEnabled())
			LOGGER.setLevel(Level.ALL);
		else
			LOGGER.info("Test Framework for Mosip api Initialized");
		BaseTestCase.initialize();
		LOGGER.info("Done with BeforeSuite and test case setup! su TEST EXECUTION!\n\n");

		if (!runType.equalsIgnoreCase("JAR")) {
			AuthTestsUtil.removeOldMosipTempTestResource();
		}
		BaseTestCase.currentModule = BaseTestCase.runContext + GlobalConstants.PARTNER_MANAGEMENT_SERVICE;
		LOGGER.info("Current Running Module set to: " + BaseTestCase.currentModule);
		PMSUtil.DbCleanRevamp();

		AdminTestUtil.copyPmsTestResource();
	}

	private static void setLogLevels() {
		AdminTestUtil.setLogLevel();
		OutputValidationUtil.setLogLevel();
		PartnerRegistration.setLogLevel();
		KeyCloakUserAndAPIKeyGeneration.setLogLevel();
		MispPartnerAndLicenseKeyGeneration.setLogLevel();
		JWKKeyUtil.setLogLevel();
		CertsUtil.setLogLevel();
		KernelAuthentication.setLogLevel();
		BaseTestCase.setLogLevel();
		PMSUtil.setLogLevel();
		KeycloakUserManager.setLogLevel();
		DBManager.setLogLevel();
		BiometricDataProvider.setLogLevel();
	}

	/**
	 * The method to start mosip testng execution
	 */
	public static void startTestRunner() {
		File homeDir = null;
		String os = System.getProperty("os.name");
		LOGGER.info(os);
		if (getRunType().contains("IDE") || os.toLowerCase().contains("windows")) {
			homeDir = new File(System.getProperty("user.dir") + "/testNgXmlFiles");
			LOGGER.info("IDE :" + homeDir);
		} else {
			File dir = new File(System.getProperty("user.dir"));
			homeDir = new File(dir.getParent() + "/mosip/testNgXmlFiles");
			LOGGER.info("ELSE :" + homeDir);
		}
		File[] files = homeDir.listFiles();
		if (files != null) {
			for (File file : files) {
				TestNG runner = new TestNG();
				List<String> suitefiles = new ArrayList<>();
				if (file.getName().toLowerCase().contains("mastertestsuite")) {
					BaseTestCase.setReportName(GlobalConstants.PARTNER_MANAGEMENT_SERVICE);
					suitefiles.add(file.getAbsolutePath());
					runner.setTestSuites(suitefiles);
					System.getProperties().setProperty("testng.outpur.dir", "testng-report");
					runner.setOutputDirectory("testng-report");
					runner.run();
				}
			}
		} else {
			LOGGER.error("No files found in directory: " + homeDir);
		}
	}

	/**
	 * The method to return class loader resource path
	 *
	 * @return String
	 */
	public static String getGlobalResourcePath() {
		if (cachedPath != null) {
			return cachedPath;
		}

		String path = null;
		if (getRunType().equalsIgnoreCase("JAR")) {
			path = new File(jarUrl).getParentFile().getAbsolutePath() + "/MosipTestResource/MosipTemporaryTestResource";
		} else if (getRunType().equalsIgnoreCase("IDE")) {
			path = new File(MosipTestRunner.class.getClassLoader().getResource("").getPath()).getAbsolutePath()
					+ "/MosipTestResource/MosipTemporaryTestResource";
			if (path.contains(GlobalConstants.TESTCLASSES))
				path = path.replace(GlobalConstants.TESTCLASSES, "classes");
		}

		if (path != null) {
			cachedPath = path;
			return path;
		} else {
			return "Global Resource File Path Not Found";
		}
	}

	/**
	 * The method will return mode of application started either from jar or eclipse
	 * ide
	 * 
	 * @return
	 */
	public static String getRunType() {
		if (MosipTestRunner.class.getResource("MosipTestRunner.class").getPath().contains(".jar"))
			return "JAR";
		else
			return "IDE";
	}

}
