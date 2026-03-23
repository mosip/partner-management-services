\c mosip_pms

-- Create bioextractor_configuration table for biometric extractor provider configurations
CREATE TABLE IF NOT EXISTS pms.bioextractor_configuration(
	id character varying(36) NOT NULL,
	config_name character varying(128) NOT NULL,
	bioextractor_provider_name character varying(128) NOT NULL,
	bioextractor_provider_version character varying(36),
	bio_modality character varying(64) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	CONSTRAINT pk_bioextractor_configuration PRIMARY KEY (id),
	CONSTRAINT uq_bioextractor_configuration_config_name UNIQUE (config_name)
);
