package io.mosip.pmp.authdevice.constants;

public enum Purpose {
	REGISTRATION("Registration"),AUTH("Auth");
	
	private String purpose;

	private Purpose(String purpose) {
		this.purpose = purpose;
	}

	@Override
	public String toString() {
		return purpose;
	}
}
