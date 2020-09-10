package io.mosip.pmp.authdevice.constants;

public enum FoundationalTrustProviderErrorMessages {
	FTP_PROVIDER_NOT_EXISTS("PMP_AUT-030","ftp provider not exists."),
	FTP_CHIP_ID_NOT_EXISTS("PMP_AUT_031","ftp chip id not exists."),
	FTP_PROVIDER_MAKE_MODEL_EXISTS("PMP_AUT_032","Given provider,make and model already exists.");
	
	private final String errorCode;
	private final String errorMessage;

	private FoundationalTrustProviderErrorMessages(final String errorCode, final String errorMessage) {
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
