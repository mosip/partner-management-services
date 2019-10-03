package io.mosip.pmp.partner.logger;

import io.mosip.pmp.partner.appender.RollingFileAppender;

/**
 * PartnerServiceLogger logger.
 * 
 * @author 
 * @since 1.0.0
 *
 */
public final class PartnerServiceLogger {

	private static RollingFileAppender mosipRollingFileAppender;

	static {
		mosipRollingFileAppender = new RollingFileAppender();
		mosipRollingFileAppender.setAppend(true);
		mosipRollingFileAppender.setAppenderName("fileappender");
		mosipRollingFileAppender.setFileName("logs/Partner-Service.log");
		mosipRollingFileAppender.setFileNamePattern("logs/Partner-Service-%d{yyyy-MM-dd}-%i.log");
		mosipRollingFileAppender.setImmediateFlush(true);
		mosipRollingFileAppender.setMaxFileSize("1mb");
		mosipRollingFileAppender.setMaxHistory(3);
		mosipRollingFileAppender.setPrudent(false);
		mosipRollingFileAppender.setTotalCap("10mb");
	}

	/**
	 * Instantiates a new logger.
	 */
	private PartnerServiceLogger() {
	}

	/**
	 * Method to get the rolling file logger for the class provided.
	 *
	 * @param clazz the clazz
	 * @return the logger
	 */
	public static Logger getLogger(Class<?> clazz) {
		return Logfactory.getDefaultRollingFileLogger(mosipRollingFileAppender, clazz);
	}
}