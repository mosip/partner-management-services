package io.mosip.pms.policy.errorMessages;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;

public class PolicyServiceLogger {
	
	private static final Logger logger = PMSLogger.getLogger(PolicyServiceLogger.class);
	
	public static void error(String message){
		
		logger.error("ERROR : " + message);
	}
	
	public static void error(String errorCode, String errorMessage){
		
		logger.error("ERROR : errorCode --> " + errorCode + " errorMessage --> " + errorMessage );
	}
	
	public static void info(String message){
		logger.info("INFO : " + message);
	}
	
	public static void warn(String message){
		logger.warn("WARN : " + message);
	}
	
	public static void logStackTrace(Exception e){
		logger.error(e.getMessage(), e.getCause());		
	}
}
