-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : pms.bioextractor_configuration
-- Purpose    : Bioextractor Configuration: Stores biometric extractor configuration details.
--
-- object: pms.bioextractor_configuration | type: TABLE --
-- DROP TABLE IF EXISTS pms.bioextractor_configuration CASCADE;
CREATE TABLE pms.bioextractor_configuration(
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
-- ddl-end --
COMMENT ON TABLE pms.bioextractor_configuration IS 'Bioextractor Configuration: Stores biometric extractor configuration details.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.id IS 'ID: Unique id for bioextractor configuration';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.config_name IS 'Configuration Name: Name of the configuration';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.bioextractor_provider_name IS 'Bioextractor Provider Name: Biometric extractor provider name';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.bioextractor_provider_version IS 'Bioextractor Provider Version: Provider version';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.bio_modality IS 'Biometric Modality: Modality for which configuration applies';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --