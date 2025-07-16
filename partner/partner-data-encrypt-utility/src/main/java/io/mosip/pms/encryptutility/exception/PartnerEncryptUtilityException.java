package io.mosip.pms.encryptutility.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * @author Nagarjuna
 *
 */

public class PartnerEncryptUtilityException extends BaseUncheckedException {

    private static final long serialVersionUID = 1L;

    public PartnerEncryptUtilityException() {}

    public PartnerEncryptUtilityException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
