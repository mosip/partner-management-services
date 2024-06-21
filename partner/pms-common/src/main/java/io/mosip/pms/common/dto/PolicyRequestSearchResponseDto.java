package io.mosip.pms.common.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import lombok.Data;


@Data
public class PolicyRequestSearchResponseDto implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	private String apikeyRequestId;

	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;

	@Column(name="is_deleted")
	private Boolean isDeleted;

	@Column(name="policy_id")
	private String policyId;

	@Column(name="request_datetimes")
	private Timestamp requestDatetimes;

	@Column(name="request_detail")
	private String requestDetail;

	@Column(name="status_code")
	private String statusCode;

	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;

	@Column(name="part_id")
	private String partnerId;
	
	private String partnerName;
	
	private String policyName;


}
