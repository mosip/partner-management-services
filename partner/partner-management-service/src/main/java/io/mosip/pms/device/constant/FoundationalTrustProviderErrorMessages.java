package io.mosip.pms.device.constant;

public enum FoundationalTrustProviderErrorMessages {
	FTP_PROVIDER_NOT_EXISTS("PMP_AUT_030","The FTP provider is either inactive or does not exist."),
	FTP_CHIP_ID_NOT_EXISTS("PMP_AUT_031","ftp chip id not exists."),
	FTP_PROVIDER_MAKE_MODEL_EXISTS("PMP_AUT_032","FTM Chip details already exists for the same make and model"),
	FTP_CERT_NOT_UPLOADED("PMP_AUT_033","Certificate is not uploaded."),
	FTP_CHIP_ID_PROVIDER_ID_NOT_MATCHED("PMP_AUT_034","Ftp chip detail id and ftp provider id not mappped. "),
	FTP_PROVIDER_DETAILS_EXISTS("PMP_AUT_037","Given ftp chip details already registered."),
	INVALID_FTP_CHIP_STATUS("PMP_AUT_038","The certificate can only be uploaded for FTM that is either pending certificate upload or has an approved status."),
	FTP_CHIP_DEACTIVATED("PMP_AUT_039","FTM chip is deactivated.");
	
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
