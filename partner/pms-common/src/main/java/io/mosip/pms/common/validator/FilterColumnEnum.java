package io.mosip.pms.common.validator;

public enum FilterColumnEnum {

	UNIQUE("unique"), ALL("all"), EMPTY("");

	private String filterColumn;

	private FilterColumnEnum(String filterColumn) {
		this.filterColumn = filterColumn;
	}

	@Override
	public String toString() {
		return filterColumn;
	}
}