package io.mosip.pms.common.dto;

import lombok.Data;

@Data
public class PartnerDataPublishDto {

	private String partnerId;
	
	private String partnerName;
	
	private String certificateData;
	
	private String partnerStatus;
}
