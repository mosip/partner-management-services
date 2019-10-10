package io.mosip.pmp.misp.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "MISPCreateResponse", description = "MISP Craete response representation")
public class MISPCreateResponseDto {

	
	private String mispStatus;
	private String mispLicenseKey;
	private String mispLicenseKeyExpiry;
	private String mispLicenseKeyStatus;
	private String mispID;
}
