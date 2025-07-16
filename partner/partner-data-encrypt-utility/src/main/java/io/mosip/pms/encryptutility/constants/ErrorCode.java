package io.mosip.pms.encryptutility.constants;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ENCRYPTION_FAILED("ENCRYPTION_FAILED", "Failed to encrypt data using Key Manager."),
    API_NULL_RESPONSE("API_NULL_RESPONSE", "API returned a null response.");

    private final String errorCode;
    private final String errorMessage;

    ErrorCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
