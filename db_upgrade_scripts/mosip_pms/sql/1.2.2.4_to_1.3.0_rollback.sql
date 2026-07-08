\c mosip_pms

-- -------------------------------------------------------------------------------------------------
-- Rollback for Partner_Admin partner type removal
-- -------------------------------------------------------------------------------------------------

INSERT INTO pms.partner_type (code, partner_description, is_policy_required, is_active, cr_by, cr_dtimes)
VALUES ('Partner_Admin', 'Partner Admin', FALSE, TRUE, 'superadmin', now())
ON CONFLICT (code) DO NOTHING;

-- -------------------------------------------------------------------------------------------------
-- Rollback for MISP License surrogate PK migration
-- -------------------------------------------------------------------------------------------------

ALTER TABLE IF EXISTS pms.misp_license DROP CONSTRAINT IF EXISTS uk_mlic;

ALTER TABLE IF EXISTS pms.misp_license DROP CONSTRAINT IF EXISTS pk_mlic;

ALTER TABLE IF EXISTS pms.misp_license ADD CONSTRAINT pk_mlic PRIMARY KEY (misp_id, license_key);

ALTER TABLE IF EXISTS pms.misp_license DROP COLUMN IF EXISTS misp_license_id;

-- -------------------------------------------------------------------------------------------------
-- Rollback for Bioextractor configuration soft delete support
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
-- Rollback for CRVS attributes: remove declaredAsDeceased from mpolicy-default-auth
-- -------------------------------------------------------------------------------------------------

UPDATE pms.auth_policy
SET policy_file_id='{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":true},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":true},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":true},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":true},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":true},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":true},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":true},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":true},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":true},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":true},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":true},{"attributeName":"zone","source":[{"attribute":"zone"}],"encrypted":true},{"attributeName":"preferredLang","source":[{"attribute":"preferredLang"}],"encrypted":false},{"attributeName":"individualBiometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics"}],"encrypted":true,"format":"extraction"}],"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"}}',
    upd_by='admin',
    upd_dtimes=now()
WHERE id='mpolicy-default-auth';

UPDATE pms.partner_policy_bioextract
SET attribute_name = 'individualBiometrics',
    upd_by = 'admin',
    upd_dtimes = now()
WHERE part_id = 'mpartner-default-auth'
  AND biometric_modality = 'face';

-- Rollback: Revert typeOfShare from "Data Share" back to "direct"

UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_faceQR';


UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_QR';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_faceQR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_QR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-PDFCard'
AND eff_dtimes='2023-11-14 05:59:00.000';

DROP TABLE IF EXISTS pms.partner_policy_bioextract_request CASCADE;

DROP TABLE IF EXISTS pms.bioextractor_configuration;

DROP TABLE IF EXISTS pms.partner_policy_credential_type_request CASCADE;

-- Rollback script for the additional_config column in oidc_client table
ALTER TABLE pms.oidc_client DROP COLUMN additional_config;

-- Rollback script for the license_key_name column in misp_license table
ALTER TABLE pms.misp_license DROP COLUMN license_key_name;

-- Remove NOT NULL constraint from email_id
ALTER TABLE pms.partner
    ALTER COLUMN email_id DROP NOT NULL;

-- Create the otp_transaction table
CREATE TABLE pms.otp_transaction (
    id character varying(36) NOT NULL,
    ref_id character varying(64) NOT NULL,
    otp_hash character varying(512) NOT NULL,
    generated_dtimes timestamp,
    expiry_dtimes timestamp,
    validation_retry_count smallint,
    status_code character varying(36),
    is_active boolean NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,
    is_deleted boolean DEFAULT FALSE,
    del_dtimes timestamp,
    CONSTRAINT pk_otpt_id PRIMARY KEY (id)
);

COMMENT ON TABLE pms.otp_transaction IS 'OTP Transaction: All OTP related data and validation details are maintained here for partner management service.';
COMMENT ON COLUMN pms.otp_transaction.id IS 'ID: Unique transaction id for each OTP transaction request';
COMMENT ON COLUMN pms.otp_transaction.ref_id IS 'Reference ID: Reference information received from OTP requester used while validating the OTP.';
COMMENT ON COLUMN pms.otp_transaction.otp_hash IS 'OTP Hash: Hash of id, ref_id and otp based on configuration setup and sent to requester module.';
COMMENT ON COLUMN pms.otp_transaction.generated_dtimes IS 'Generated Date Time: Timestamp when the OTP was generated';
COMMENT ON COLUMN pms.otp_transaction.expiry_dtimes IS 'Expiry Date Time: Timestamp when the OTP expires';
COMMENT ON COLUMN pms.otp_transaction.validation_retry_count IS 'Validation Retry Count: Number of OTP validation retries.';
COMMENT ON COLUMN pms.otp_transaction.status_code IS 'Status Code: Status of the OTP (active or expired).';
COMMENT ON COLUMN pms.otp_transaction.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
COMMENT ON COLUMN pms.otp_transaction.cr_by IS 'Created By : ID or name of the user who created the record';
COMMENT ON COLUMN pms.otp_transaction.cr_dtimes IS 'Created DateTimestamp : When the record was inserted';
COMMENT ON COLUMN pms.otp_transaction.upd_by IS 'Updated By : ID or name of the user who updated the record';
COMMENT ON COLUMN pms.otp_transaction.upd_dtimes IS 'Updated DateTimestamp : When the record was last updated';
COMMENT ON COLUMN pms.otp_transaction.is_deleted IS 'IS_Deleted : Soft delete flag';
COMMENT ON COLUMN pms.otp_transaction.del_dtimes IS 'Deleted DateTimestamp : When the record was soft deleted';

alter table pms.batch_job_execution drop constraint job_inst_exec_fk;
alter table pms.batch_job_execution_params drop constraint job_exec_params_fk;
alter table pms.batch_step_execution drop constraint job_exec_step_fk;
alter table pms.batch_step_execution_context drop constraint step_exec_ctx_fk;
alter table pms.batch_job_execution_context drop constraint job_exec_ctx_fk;

drop table if exists pms.batch_job_execution_context;
drop table if exists pms.batch_step_execution_context;
drop table if exists pms.batch_step_execution;
drop table if exists pms.batch_job_execution_params;
drop table if exists pms.batch_job_execution;
drop table if exists pms.batch_job_instance;

drop sequence if exists pms.batch_step_execution_seq;
drop sequence if exists pms.batch_job_execution_seq;
drop sequence if exists pms.batch_job_seq;

drop table if exists pms.notifications;

ALTER TABLE pms.user_details DROP COLUMN notifications_seen_dtimes;

UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":[{"Left Thumb","Right Thumb"}]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-qrcode';

UPDATE pms.auth_policy
SET  policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-euin';
