\c mosip_pms

-- -------------------------------------------------------------------------------------------------
-- Bioextractor configuration soft delete support (1.3.0-GA)
-- -------------------------------------------------------------------------------------------------

ALTER TABLE IF EXISTS pms.bioextractor_configuration
    ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false;

-- Allow recreation of a configuration name after soft-delete
ALTER TABLE IF EXISTS pms.bioextractor_configuration
    DROP CONSTRAINT IF EXISTS uq_bioextractor_configuration_config_name;

