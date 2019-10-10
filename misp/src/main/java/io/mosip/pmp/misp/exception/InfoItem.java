package io.mosip.pmp.misp.exception;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
class InfoItem implements Serializable {

	private static final long serialVersionUID = -779695043380592601L;

	@Getter
	@Setter
	public String errorCode = null;

	@Getter
	@Setter
	public String errorText = null;

}
