package io.mosip.pms.policy.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerPolicySearchDto {

	private String policyApiKey;

	private String crBy;

	private Timestamp crDtimes;

	private Timestamp delDtimes;

	private Boolean isActive;

	private Boolean isDeleted;

	private String policyId;

	private String updBy;

	private Timestamp updDtimes;

	private Timestamp validFromDatetime;

	private Timestamp validToDatetime;

}
