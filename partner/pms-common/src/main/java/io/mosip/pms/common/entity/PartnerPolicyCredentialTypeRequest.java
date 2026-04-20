package io.mosip.pms.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity for {@code pms.partner_policy_credential_type_request}.
 */
@Data
@Entity
@Table(name = "partner_policy_credential_type_request")
public class PartnerPolicyCredentialTypeRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name = "partner_policy_request_id", nullable = false)
	private String partnerPolicyRequestId;

	@Column(name = "part_id", nullable = false)
	private String partId;

	@Column(name = "policy_id", nullable = false)
	private String policyId;

	@Column(name = "credential_type", nullable = false)
	private String credentialType;

	@Column(name = "status_code", nullable = false)
	private String statusCode;

	@Column(name = "cr_by", nullable = false)
	private String crBy;

	@Column(name = "cr_dtimes", nullable = false)
	private Timestamp crDtimes;

	@Column(name = "upd_by")
	private String updBy;

	@Column(name = "upd_dtimes")
	private Timestamp updDtimes;
}

