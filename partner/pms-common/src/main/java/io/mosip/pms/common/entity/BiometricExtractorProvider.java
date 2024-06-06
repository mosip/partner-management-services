package io.mosip.pms.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="partner_policy_bioextract")
public class BiometricExtractorProvider implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;

	@Column(name = "part_id")
	private String partnerId;
	
	@Column(name="policy_id")
	private String policyId;
	
	@Column(name = "attribute_name")
	private String attributeName;
	
	@Column(name = "extractor_provider")
	private String extractorProvider;
	
	@Column(name = "extractor_provider_version")
	private String extractorProviderVersion;
	
	@Column(name = "biometric_modality")
	private String biometricModality;
	
	@Column(name = "biometric_sub_types")
	private String biometricSubTypes;
	
	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;

	@Column(name="is_deleted")
	private Boolean isDeleted;
	
	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;
}
