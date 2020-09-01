package io.mosip.pmp.authdevice.exception;

import io.mosip.pmp.authdevice.constants.AuthDeviceErrorMessages;
import io.mosip.pmp.partner.exception.BaseUncheckedException;

public class TokenGenerationFailedException  extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new ABIS abort exception.
	 */
	public TokenGenerationFailedException() {
		super();
	}

	/**
	 * Instantiates a new ABIS abort exception.
	 *
	 * @param errorMessage
	 *            the error message
	 */
	public TokenGenerationFailedException(String errorMessage) {
		super(AuthDeviceErrorMessages.RPR_BDD_ABIS_ABORT.getErrorCode() + EMPTY_SPACE, errorMessage);
	}

	/**
	 * Instantiates a new ABIS abort exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public TokenGenerationFailedException(String message, Throwable cause) {
		super(AuthDeviceErrorMessages.RPR_BDD_ABIS_ABORT.getErrorCode() + EMPTY_SPACE, message, cause);
	}
}


