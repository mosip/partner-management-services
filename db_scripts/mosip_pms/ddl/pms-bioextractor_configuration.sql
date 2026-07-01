-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name 	: pms.bioextractor_configuration
-- Purpose    	: Bioextractor Configuration: Stores the configuration details for biometric extractor providers.
--
-- -------------------------------------------------------------------------------------------------

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
	is_deleted boolean NOT NULL DEFAULT false,
	CONSTRAINT pk_bioextractor_configuration PRIMARY KEY (id)
);
-- ddl-end --
COMMENT ON TABLE pms.bioextractor_configuration IS 'Bioextractor Configuration: Stores configuration details for biometric extractor providers.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.id IS 'ID: Unique identifier for the bioextractor configuration record.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.config_name IS 'Config Name: Unique name identifying this configuration.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.bioextractor_provider_name IS 'Bioextractor Provider Name: Name of the biometric extractor provider.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.bioextractor_provider_version IS 'Bioextractor Provider Version: Version of the biometric extractor provider.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.bio_modality IS 'Bio Modality: Biometric modality (e.g. face, finger, iris).';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.cr_by IS 'Created By: ID or name of the user who created the record.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.cr_dtimes IS 'Created DateTimestamp: Date and Timestamp when the record was created.';
-- ddl-end --
COMMENT ON COLUMN pms.bioextractor_configuration.is_deleted IS 'Is Deleted: Soft delete flag. true indicates logically deleted record.';
-- ddl-end --

CREATE UNIQUE INDEX uq_bioextractor_configuration_config_name_active
ON pms.bioextractor_configuration (lower(config_name))
WHERE is_deleted = false;

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.bioextractor_configuration TO pmsuser;
