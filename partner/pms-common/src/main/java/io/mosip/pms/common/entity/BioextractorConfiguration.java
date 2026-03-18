package io.mosip.pms.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "bioextractor_configuration")
public class BioextractorConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name = "config_name")
	private String configName;

	@Column(name = "bioextractor_provider_name")
	private String bioextractorProviderName;

	@Column(name = "bioextractor_provider_version")
	private String bioextractorProviderVersion;

	@Column(name = "bio_modality")
	private String bioModality;

	@Column(name = "cr_by")
	private String crBy;

	@Column(name = "cr_dtimes")
	private Timestamp crDtimes;
}

