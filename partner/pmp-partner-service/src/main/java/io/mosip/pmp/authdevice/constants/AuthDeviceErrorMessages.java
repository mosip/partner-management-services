package io.mosip.pmp.authdevice.constants;

public enum AuthDeviceErrorMessages {
	RPR_BDD_ABIS_ABORT("PMP_AUT-021","");
	private final String errorCode;
	private final String errorMessage;

	private AuthDeviceErrorMessages(final String errorCode, final String errorMessage) {
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
