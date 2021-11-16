package io.mosip.pms.common.constant;

public enum FilterTypeEnum {
	CONTAINS("contains"), STARTSWITH("startsWith"), BETWEEN("between"), EQUALS("equals"), IN("in");

	private String type;

	private FilterTypeEnum(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}
