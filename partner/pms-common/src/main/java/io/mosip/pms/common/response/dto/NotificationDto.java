package io.mosip.pms.common.response.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificationDto {

	private String emailId;

	private String partnerId;

	private String partnerName;

	private String partnerStatus;

	private String policyId;

	private String policyName;

	private String policyStatus;

	private String apiKey;

	private String apiKeyStatus;

	private LocalDateTime apiKeyExpiryDate;

	private LocalDateTime policyExpiryDateTime;

	private String emailSubjectTemplate;

	private String emailBodyTemplate;
	
	private String langCode;
}