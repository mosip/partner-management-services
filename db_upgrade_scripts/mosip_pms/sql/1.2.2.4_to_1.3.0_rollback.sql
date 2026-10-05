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
ALTER TABLE pms.oidc_client DROP COLUMN IF EXISTS additional_config;

-- Rollback script for the license_key_name column in misp_license table
ALTER TABLE pms.misp_license DROP COLUMN IF EXISTS license_key_name;

-- Remove NOT NULL constraint from email_id
ALTER TABLE pms.partner
    ALTER COLUMN email_id DROP NOT NULL;

-- Rollback script for the email_id_hash column added to partner, partner_h and partner_contact tables
ALTER TABLE pms.partner DROP COLUMN IF EXISTS email_id_hash;
ALTER TABLE pms.partner_h DROP COLUMN IF EXISTS email_id_hash;
ALTER TABLE pms.partner_contact DROP COLUMN IF EXISTS email_id_hash;

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

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.otp_transaction TO pmsuser;

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
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-qrcode';

UPDATE pms.auth_policy
SET  policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-euin';

-- -------------------------------------------------------------------------------------------------
-- Rollback for removal of pms.misp and pms.tspid_seq tables
-- NOTE: structure only; any data that existed before the upgrade drop is not restored.
-- -------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS pms.misp(
	id character varying(36) NOT NULL,
	name character varying(128) NOT NULL,
	address character varying(2000),
	contact_no character varying(16),
	email_id character varying(256),
	user_id character varying(256) NOT NULL,
	status_code character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_misp PRIMARY KEY (id),
	CONSTRAINT uk_misp UNIQUE (name)
);
COMMENT ON TABLE pms.misp IS 'MISP: MISP, acronym for MOSIP Identity Service Provider, stores the master list of MISPs.';
COMMENT ON COLUMN pms.misp.id IS 'MISP ID: Unique ID generated / assigned for a MISP.';
COMMENT ON COLUMN pms.misp.name IS 'Name: Name of the MISP orgranization.';
COMMENT ON COLUMN pms.misp.address IS 'Address: Address of the MISP organization.';
COMMENT ON COLUMN pms.misp.contact_no IS 'Contact Number: Contact number of the MISP organization';
COMMENT ON COLUMN pms.misp.email_id IS 'Email ID: Email ID of the MISP organization / contact person';
COMMENT ON COLUMN pms.misp.user_id IS 'User ID: Login ID assigned by MOSIP to MISP Admin. It is a general/common id for a MISP that is generated and assigned when the MISP is created.';
COMMENT ON COLUMN pms.misp.status_code IS 'Status Code: Status of MISP';
COMMENT ON COLUMN pms.misp.is_active IS 'Active Flag: Flag to mark whether the record is Active or In-active';
COMMENT ON COLUMN pms.misp.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN pms.misp.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.misp.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.misp.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN pms.misp.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN pms.misp.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.misp TO pmsuser;

CREATE TABLE IF NOT EXISTS pms.tspid_seq(
	curr_seq_no integer NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	CONSTRAINT pk_tspidseq_id PRIMARY KEY (curr_seq_no)
);
COMMENT ON TABLE pms.tspid_seq IS 'Trusted Service Provider ID Sequence : Maintains latest sequence number available for TSP ID  generation';
COMMENT ON COLUMN pms.tspid_seq.curr_seq_no IS 'Current Sequence Number : Latest sequence number available for TSP (Trusted Service Provider) ID generation';
COMMENT ON COLUMN pms.tspid_seq.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN pms.tspid_seq.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.tspid_seq.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.tspid_seq.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.tspid_seq TO pmsuser;

-- -------------------------------------------------------------------------------------------------
-- Rollback for default policy history (pms.auth_policy_h) changes: restore the 1.2.2.4 values
-- -------------------------------------------------------------------------------------------------

UPDATE pms.auth_policy_h
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-euin'
AND eff_dtimes='2020-11-13 05:58:00.000';
