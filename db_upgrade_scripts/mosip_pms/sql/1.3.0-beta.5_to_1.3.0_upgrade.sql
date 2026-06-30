\c mosip_pms

-- -------------------------------------------------------------------------------------------------
-- Bioextractor configuration soft delete support (1.3.0)
-- -------------------------------------------------------------------------------------------------

ALTER TABLE IF EXISTS pms.bioextractor_configuration
    ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false;

-- Allow recreation of a configuration name after soft-delete
ALTER TABLE IF EXISTS pms.bioextractor_configuration
    DROP CONSTRAINT IF EXISTS uq_bioextractor_configuration_config_name;

-- Enforce uniqueness among active (non-deleted) configurations only (case-insensitive)
CREATE UNIQUE INDEX IF NOT EXISTS uq_bioextractor_configuration_config_name_active
ON pms.bioextractor_configuration (lower(config_name))
WHERE is_deleted = false;

-- -------------------------------------------------------------------------------------------------
-- MISP License: replace composite PK (misp_id, license_key) with surrogate PK misp_license_id (1.3.0-GA)
-- -------------------------------------------------------------------------------------------------

ALTER TABLE pms.misp_license ADD COLUMN IF NOT EXISTS misp_license_id character varying(36);

UPDATE pms.misp_license SET misp_license_id = gen_random_uuid()::character varying WHERE misp_license_id IS NULL;

ALTER TABLE pms.misp_license ALTER COLUMN misp_license_id SET NOT NULL;

ALTER TABLE pms.misp_license DROP CONSTRAINT IF EXISTS pk_mlic;

ALTER TABLE pms.misp_license ADD CONSTRAINT pk_mlic PRIMARY KEY (misp_license_id);

ALTER TABLE pms.misp_license ADD CONSTRAINT uk_mlic UNIQUE (misp_id, license_key);

COMMENT ON COLUMN pms.misp_license.misp_license_id IS 'MISP License ID: Unique surrogate identifier (primary key) for the license record.';

