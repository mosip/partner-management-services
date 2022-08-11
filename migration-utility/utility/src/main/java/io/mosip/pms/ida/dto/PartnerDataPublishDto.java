package io.mosip.pms.ida.dto;

import lombok.Data;

@Data
public class PartnerDataPublishDto {

	private String partnerId;
	
	private String partnerName;
	
	private String certificateData;
	
	private String partnerStatus;
}
