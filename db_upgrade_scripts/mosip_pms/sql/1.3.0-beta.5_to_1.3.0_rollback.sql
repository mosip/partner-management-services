\c mosip_pms

-- -------------------------------------------------------------------------------------------------
-- Rollback for Bioextractor configuration soft delete support (1.3.0-GA)
-- -------------------------------------------------------------------------------------------------

-- Remove soft-deleted rows to safely restore uniqueness
DELETE FROM pms.bioextractor_configuration
WHERE is_deleted = true;

DROP INDEX IF EXISTS pms.uq_bioextractor_configuration_config_name_active;

-- Re-add unique constraint (original behavior; case-sensitive)
ALTER TABLE IF EXISTS pms.bioextractor_configuration
    ADD CONSTRAINT uq_bioextractor_configuration_config_name UNIQUE (config_name);

ALTER TABLE IF EXISTS pms.bioextractor_configuration
    DROP COLUMN IF EXISTS is_deleted;

