package io.mosip.pms.batchjob.constants;

import lombok.Getter;

@Getter
public enum ErrorCodes {
    API_NOT_ACCESSIBLE("PMS-BJ-001", "API not accessible"),
    API_NULL_RESPONSE("PMS-BJ-002", "API returned a null response"),
    PARTNER_CERTIFICATE_FETCH_ERROR("PMS-BJ-003", "Error while fetching partner certificate"),
    UNABLE_TO_PROCESS("PMS-BJ-004", "Error occurred while processing request"),
    INVALID_TEMPLATE_TYPE("PMS-BJ-005", "Invalid template type"),
    EMAIL_SEND_FAILED("PMS-BJ-006", "Failed to send email"),
    ROOT_CERTIFICATE_FETCH_ERROR("PMS-BJ-004", "Error while fetching root certificate"),
	TRUST_CERTIFICATES_FETCH_ERROR("PMS_CERTIFICATE_ERROR_010", "Error while fetching trust certificates."),
	NOTIFICATION_CREATE_ERROR("PMS-BJ-007", "Error while creating the notification.");;
	
    private final String code;
    private final String message;

    ErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
