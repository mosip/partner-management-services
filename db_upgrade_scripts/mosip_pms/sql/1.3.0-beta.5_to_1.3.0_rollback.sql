\c mosip_pms

-- -------------------------------------------------------------------------------------------------
-- Rollback for MISP License surrogate PK migration (1.3.0)
-- -------------------------------------------------------------------------------------------------

ALTER TABLE IF EXISTS pms.misp_license DROP CONSTRAINT IF EXISTS uk_mlic;

ALTER TABLE IF EXISTS pms.misp_license DROP CONSTRAINT IF EXISTS pk_mlic;

ALTER TABLE IF EXISTS pms.misp_license ADD CONSTRAINT pk_mlic PRIMARY KEY (misp_id, license_key);

ALTER TABLE IF EXISTS pms.misp_license DROP COLUMN IF EXISTS misp_license_id;

-- -------------------------------------------------------------------------------------------------
-- Rollback for Bioextractor configuration soft delete support (1.3.0)
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

-- -------------------------------------------------------------------------------------------------
-- Rollback for CRVS attributes: remove declaredAsDeceased from mpolicy-default-auth (1.3.0)
-- -------------------------------------------------------------------------------------------------

UPDATE pms.auth_policy
SET policy_file_id='{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":true},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":true},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":true},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":true},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":true},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":true},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":true},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":true},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":true},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":true},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":true},{"attributeName":"individualBiometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics"}],"encrypted":true,"format":"extraction"}],"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"}}',
    upd_by='admin',
    upd_dtimes=now()
WHERE id='mpolicy-default-auth';

