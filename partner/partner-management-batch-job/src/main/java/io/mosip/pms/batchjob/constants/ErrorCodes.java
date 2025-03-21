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
	NOTIFICATION_CREATE_ERROR("PMS-BJ-007", "Error while creating the notification."),
    FETCH_PARTNER_ADMIN_USER_IDS_ERROR("PMS-BJ-008", "Unable to fetch Partner Admin user IDs"),
    TEMPLATE_FETCH_ERROR("PMS-BJ-009", "Failed to fetch email template"),
    UNABLE_TO_DECODE_CERTIFICATE("PMS-BJ-010", "Unable to decode the certificate data"),
    INVALID_CERTIFICATE_TYPE("PMS-BJ-011", "Invalid certificate type");

    private final String code;
    private final String message;

    ErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
