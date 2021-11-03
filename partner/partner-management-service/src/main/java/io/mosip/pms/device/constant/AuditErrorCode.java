package io.mosip.pms.device.constant;

public enum AuditErrorCode {
	
	AUDIT_PARSE_EXCEPTION("PMP-AUT-020", "Parse Error exception"), 
	AUDIT_EXCEPTION("PMP-AUT-021", "Audit Exception from client::");	

	private final String errorCode;
	private final String errorMessage;

	private AuditErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
