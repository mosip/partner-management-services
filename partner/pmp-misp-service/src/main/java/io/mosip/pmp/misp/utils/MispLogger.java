package io.mosip.pmp.misp.utils;

import org.apache.log4j.Logger;

public class MispLogger { 
	
	private static final Logger logger = Logger.getLogger(MispLogger.class.getName());
	
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
