package io.mosip.pms.device.constant;

public enum FoundationalTrustProviderErrorMessages {
	FTP_PROVIDER_NOT_EXISTS("PMP_AUT_030","ftp provider not exists."),
	FTP_CHIP_ID_NOT_EXISTS("PMP_AUT_031","ftp chip id not exists."),
	FTP_PROVIDER_MAKE_MODEL_EXISTS("PMP_AUT_032","Given provider,make and model already exists."),
	FTP_CERT_NOT_UPLOADED("PMP_AUT_033","Certificate is not uploaded."),
	FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED("PMP_AUT_034","Ftp chip detail id and ftp provider id not mappped. "),
	FTP_PROVIDER_DETAILS_EXISTS("PMP_AUT_037","Given ftp chip details already registered.");
	
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
