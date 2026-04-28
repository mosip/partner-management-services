\c mosip_pms

-- -------------------------------------------------------------------------------------------------
-- Rollback for Bioextractor configuration soft delete support (1.3.0-GA)
-- -------------------------------------------------------------------------------------------------

-- Re-add unique constraint (may fail if duplicate config_name rows exist)
ALTER TABLE IF EXISTS pms.bioextractor_configuration
    ADD CONSTRAINT uq_bioextractor_configuration_config_name UNIQUE (config_name);

ALTER TABLE IF EXISTS pms.bioextractor_configuration
    DROP COLUMN IF EXISTS is_deleted;

