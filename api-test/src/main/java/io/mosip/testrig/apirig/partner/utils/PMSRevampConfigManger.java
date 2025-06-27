package io.mosip.testrig.apirig.partner.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import io.mosip.testrig.apirig.partner.testrunner.MosipTestRunner;
import io.mosip.testrig.apirig.utils.ConfigManager;
public class PMSRevampConfigManger extends ConfigManager{
	private static final Logger LOGGER = Logger.getLogger(PMSRevampConfigManger.class);
	
	public static void init() {
		Logger configManagerLogger = Logger.getLogger(ConfigManager.class);
		configManagerLogger.setLevel(Level.WARN);

		Map<String, Object> moduleSpecificPropertiesMap = new HashMap<>();
		// Load scope specific properties
		try {
			String path = MosipTestRunner.getGlobalResourcePath() + "/config/pmsRevamp.properties";
			Properties props = getproperties(path);
			// Convert Properties to Map and add to moduleSpecificPropertiesMap
			for (String key : props.stringPropertyNames()) {
				String value = System.getenv(key) == null ? props.getProperty(key) : System.getenv(key);
				moduleSpecificPropertiesMap.put(key, value);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		// Add module specific properties as well.
		init(moduleSpecificPropertiesMap);
	}

	public static String getKeymangrDbUrl() {
		return "jdbc:postgresql://"
				+ (getproperty("km-db-server").isBlank() ? getproperty("db-server") : getproperty("km-db-server")) + ":"
				+ (getproperty("km-db-port").isBlank() ? getproperty("db-port") : getproperty("km-db-port"))
				+ "/mosip_keymgr";
	}

	public static String getKeymangrDbUser() {
		return getproperty("km-db-su-user").isBlank() ? getproperty("db-su-user") : getproperty("km-db-su-user");
	}

	public static String getKeymangrDbPass() {
		return getproperty("km-db-postgres-password").isBlank() ? getproperty("postgres-password")
				: getproperty("km-db-postgres-password");
	}
}
