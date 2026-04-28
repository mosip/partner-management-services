package io.mosip.pms.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bioextractor_configuration")
public class BioextractorConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name = "config_name", nullable = false, unique = true)
	private String configName;

	@Column(name = "bioextractor_provider_name", nullable = false)
	private String bioextractorProviderName;

	@Column(name = "bioextractor_provider_version")
	private String bioextractorProviderVersion;

	@Column(name = "bio_modality", nullable = false)
	private String bioModality;

	@Column(name = "cr_by", nullable = false)
	private String crBy;

	@Column(name = "cr_dtimes", nullable = false)
	private Timestamp crDtimes;
	
	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted = false;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getBioextractorProviderName() {
		return bioextractorProviderName;
	}

	public void setBioextractorProviderName(String bioextractorProviderName) {
		this.bioextractorProviderName = bioextractorProviderName;
	}

	public String getBioextractorProviderVersion() {
		return bioextractorProviderVersion;
	}

	public void setBioextractorProviderVersion(String bioextractorProviderVersion) {
		this.bioextractorProviderVersion = bioextractorProviderVersion;
	}

	public String getBioModality() {
		return bioModality;
	}

	public void setBioModality(String bioModality) {
		this.bioModality = bioModality;
	}

	public String getCrBy() {
		return crBy;
	}

	public void setCrBy(String crBy) {
		this.crBy = crBy;
	}

	public Timestamp getCrDtimes() {
		return crDtimes;
	}

	public void setCrDtimes(Timestamp crDtimes) {
		this.crDtimes = crDtimes;
	}
	
	public boolean isDeleted() {
		return isDeleted;
	}
	
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
