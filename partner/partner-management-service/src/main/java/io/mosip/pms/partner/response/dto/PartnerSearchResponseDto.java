package io.mosip.pms.partner.response.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerSearchResponseDto {

	private String id;

	private String address;

	private String contactNo;

	private String crBy;

	private Timestamp crDtimes;

	private Timestamp delDtimes;

	private String emailId;

	private Boolean isActive;

	private Boolean isDeleted;

	private String name;

	private String policyGroupId;

	private String certificateAlias;

	private String partnerTypeCode;

	private String approvalStatus;

	private String updBy;

	private Timestamp updDtimes;

	private String userId;
}
