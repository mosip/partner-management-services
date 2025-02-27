package io.mosip.pms.partner.management.batchjob.constants;

import lombok.Getter;

@Getter
public enum ErrorCodes {
    API_NOT_ACCESSIBLE("PMS-BJ-001", "API not accessible"),
    API_NULL_RESPONSE("PMS-BJ-002", "API returned a null response"),
    PARTNER_CERTIFICATE_FETCH_ERROR("PMS-BJ-003", "Error while fetching partner certificate");

    private final String code;
    private final String message;

    ErrorCodes(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
