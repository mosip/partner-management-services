package io.mosip.pms.common.constant;

public enum Purpose {
	REGISTRATION("REGISTRATION"),AUTH("AUTH");
	
	private String purpose;

	private Purpose(String purpose) {
		this.purpose = purpose;
	}

	@Override
	public String toString() {
		return purpose;
	}
}
