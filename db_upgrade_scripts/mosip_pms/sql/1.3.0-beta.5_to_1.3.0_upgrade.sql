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

-- -------------------------------------------------------------------------------------------------
-- CRVS attributes: add declaredAsDeceased to mpolicy-default-auth (1.3.0)
-- -------------------------------------------------------------------------------------------------

UPDATE pms.auth_policy
SET policy_file_id='{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":true},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":true},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":true},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":true},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":true},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":true},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":true},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":true},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":true},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":true},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":true},{"attributeName":"zone","source":[{"attribute":"zone"}],"encrypted":true},{"attributeName":"preferredLang","source":[{"attribute":"preferredLang"}],"encrypted":false},{"attributeName":"individualBiometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics"}],"encrypted":true,"format":"extraction"},{"attributeName":"declaredAsDeceased","source":[{"attribute":"declaredAsDeceased"}],"encrypted":true}],"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"}}',
    upd_by='admin',
    upd_dtimes=now()
WHERE id='mpolicy-default-auth';

UPDATE pms.partner_policy_bioextract
SET attribute_name = 'photo',
    upd_by = 'admin',
    upd_dtimes = now()
WHERE part_id = 'mpartner-default-auth'
  AND biometric_modality = 'face';

-- -------------------------------------------------------------------------------------------------
-- Remove unused Partner_Admin partner type
-- -------------------------------------------------------------------------------------------------

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pms.partner WHERE partner_type_code = 'Partner_Admin') THEN
        RAISE WARNING 'Skipping removal of partner_type Partner_Admin: existing pms.partner rows still reference it. Manual cleanup required: reassign or remove the referencing pms.partner rows, then delete pms.partner_type row with code = ''Partner_Admin''.';
    ELSE
        DELETE FROM pms.partner_type WHERE code = 'Partner_Admin';
    END IF;
END $$;
