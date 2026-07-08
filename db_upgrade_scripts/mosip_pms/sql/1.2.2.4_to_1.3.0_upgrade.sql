\c mosip_pms

--- These tables are required by Spring Batch framework

create table pms.batch_job_instance (
    job_instance_id bigint not null primary key,
    version bigint,
    job_name varchar(100) not null,
    job_key varchar(32) not null,
    constraint job_inst_un unique (job_name, job_key)
);

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.batch_job_instance TO pmsuser;

create table pms.batch_job_execution (
    job_execution_id bigint not null primary key,
    version bigint,
    job_instance_id bigint not null,
    create_time timestamp not null,
    start_time timestamp default null,
    end_time timestamp default null,
    status varchar(10),
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    constraint job_inst_exec_fk foreign key (job_instance_id)
    references pms.batch_job_instance(job_instance_id)
);

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.batch_job_execution TO pmsuser;

create table pms.batch_job_execution_params (
    job_execution_id bigint not null,
    parameter_name varchar(100) not null,
    parameter_type varchar(100) not null,
    parameter_value varchar(2500),
    identifying char(1) not null,
    constraint job_exec_params_fk foreign key (job_execution_id)
    references pms.batch_job_execution(job_execution_id)
);

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.batch_job_execution_params TO pmsuser;

create table pms.batch_step_execution (
    step_execution_id bigint not null primary key,
    version bigint not null,
    step_name varchar(100) not null,
    job_execution_id bigint not null,
    create_time timestamp not null,
    start_time timestamp default null,
    end_time timestamp default null,
    status varchar(10),
    commit_count bigint,
    read_count bigint,
    filter_count bigint,
    write_count bigint,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    rollback_count bigint,
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    constraint job_exec_step_fk foreign key (job_execution_id)
    references pms.batch_job_execution(job_execution_id)
);

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.batch_step_execution TO pmsuser;

create table pms.batch_step_execution_context (
    step_execution_id bigint not null primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint step_exec_ctx_fk foreign key (step_execution_id)
    references pms.batch_step_execution(step_execution_id)
);

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.batch_step_execution_context TO pmsuser;

create table pms.batch_job_execution_context (
    job_execution_id bigint not null primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint job_exec_ctx_fk foreign key (job_execution_id)
    references batch_job_execution(job_execution_id)
);

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.batch_job_execution_context TO pmsuser;

create sequence pms.batch_step_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence pms.batch_job_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence pms.batch_job_seq maxvalue 9223372036854775807 no cycle;

grant usage, select on all sequences in schema pms to pmsuser;

-- This table stores notifications for root, intermediate, and partner certificate expiry, as well as SBI expiry and API key expiry.
CREATE TABLE pms.notifications
(
    id character varying(128) NOT NULL,
    partner_id character varying(128) NOT NULL,
    notification_type character varying(36) NOT NULL,
    notification_status character varying(36) NOT NULL,
	notification_details_json character varying(50000) NOT NULL,
    email_id character varying(3000) NOT NULL,
    email_lang_code character varying(36) NOT NULL,
    email_sent boolean DEFAULT FALSE,
    email_sent_dtimes timestamp,
    cr_by character varying(128) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(128),
    upd_dtimes timestamp,
    CONSTRAINT notifications_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE pms.notifications IS 'This table stores notifications along with their details.';
COMMENT ON COLUMN pms.notifications.id IS 'ID: A unique identifier for the notification.';
COMMENT ON COLUMN pms.notifications.partner_id IS 'Partner ID: A unique identifier for the partner.';
COMMENT ON COLUMN pms.notifications.notification_type IS 'Notification Type: The type of notification generated. Examples include PARTNER_CERT_EXPIRY, ROOT_CERT_EXPIRY, and SBI_EXPIRY.';
COMMENT ON COLUMN pms.notifications.notification_status IS 'Notification Status: The current status of the notification. Possible values include ACTIVE and DISMISSED.';
COMMENT ON COLUMN pms.notifications.notification_details_json IS 'Notification Details (JSON): Detailed information about the notification in JSON format.';
COMMENT ON COLUMN pms.notifications.email_id IS 'Email ID: The email address of the partner to whom the notification is sent.';
COMMENT ON COLUMN pms.notifications.email_lang_code IS 'Email Language Code: The language code used for the email.';
COMMENT ON COLUMN pms.notifications.email_sent IS 'Email Sent: Indicates whether the email has been sent (TRUE) or not (FALSE).';
COMMENT ON COLUMN pms.notifications.email_sent_dtimes IS 'Email Sent Timestamp: The date and time when the email was sent.';
COMMENT ON COLUMN pms.notifications.cr_dtimes IS 'Created Timestamp: The date and time when the record was created.';
COMMENT ON COLUMN pms.notifications.cr_by IS 'Created By: The ID or name of the user who created the record.';
COMMENT ON COLUMN pms.notifications.upd_by IS 'Updated By: The ID or name of the user who last updated the record.';
COMMENT ON COLUMN pms.notifications.upd_dtimes IS 'Updated Timestamp: The date and time when any field in the record was last updated.';

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.notifications TO pmsuser;

-- add new columns in user_details table
ALTER TABLE pms.user_details Add COLUMN notifications_seen_dtimes timestamp;
COMMENT ON COLUMN pms.user_details.notifications_seen_dtimes IS 'Notifications Seen Timestamp: The date and time when the notifications was seen.';

UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-qrcode';

UPDATE pms.auth_policy
SET  policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-euin';

-- Add new column for email hash
ALTER TABLE pms.partner
ADD COLUMN email_id_hash VARCHAR(3000);

-- Update column sizes in pms.partner
ALTER TABLE pms.partner
    ALTER COLUMN contact_no TYPE character varying(1000),
    ALTER COLUMN email_id TYPE character varying(3000),
    ALTER COLUMN address TYPE character varying(10000);

-- Update column sizes in pms.partner_h
ALTER TABLE pms.partner_h
    ALTER COLUMN contact_no TYPE character varying(1000),
    ALTER COLUMN email_id TYPE character varying(3000),
    ALTER COLUMN address TYPE character varying(10000),
    ADD COLUMN email_id_hash character varying(3000);

-- Update column sizes in pms.partner_contact
ALTER TABLE pms.partner_contact
    ALTER COLUMN contact_no TYPE character varying(1000),
    ALTER COLUMN email_id TYPE character varying(3000),
    ALTER COLUMN address TYPE character varying(10000),
    ADD COLUMN email_id_hash character varying(3000);

-- Add new column for license_key_name in misp_license table
ALTER TABLE pms.misp_license ADD COLUMN license_key_name character varying(128);

-- Add NOT NULL constraint to email_id
ALTER TABLE pms.partner
    ALTER COLUMN email_id SET NOT NULL;

-- Update all records in pms.reg_device_sub_type to set cr_by = 'superadmin'
UPDATE pms.reg_device_sub_type
SET cr_by = 'superadmin';

-- Update all records in pms.reg_device_type to set cr_by = 'superadmin'
UPDATE pms.reg_device_type
SET cr_by = 'superadmin';

-- Drop the otp_transaction
DROP TABLE IF EXISTS pms.otp_transaction CASCADE;

-- Add new column for additional_config in oidc_client table
ALTER TABLE pms.oidc_client ADD COLUMN additional_config character varying;

-- Updated type of share from direct to data share for the below policies
UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_faceQR';


UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_QR';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_faceQR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_QR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-PDFCard'
AND eff_dtimes='2023-11-14 05:59:00.000';

CREATE TABLE IF NOT EXISTS pms.bioextractor_configuration(
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

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.bioextractor_configuration TO pmsuser;

-- -------------------------------------------------------------------------------------------------
-- Partner policy bio extract request table for 1.3.0-beta.5
-- -------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS pms.partner_policy_bioextract_request (
    id character varying(36) NOT NULL,
    partner_policy_request_id character varying(36) NOT NULL,
    part_id character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL,
    attribute_name character varying(128) NOT NULL,
    extractor_provider character varying(128) NOT NULL,
    extractor_provider_version character varying(8),
    biometric_modality character varying(64) NOT NULL,
    biometric_sub_types character varying(64),
    status_code character varying(20) NOT NULL DEFAULT 'InProgress',
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,
    CONSTRAINT pk_ppber_id PRIMARY KEY (id),
    CONSTRAINT chk_ppber_status CHECK (status_code IN ('InProgress', 'approved', 'rejected'))
);

COMMENT ON TABLE pms.partner_policy_bioextract_request IS 'Partner Policy Bio Extract Request: Stores partner requests for biometric extraction configuration.';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.id IS 'ID: Unique id for partner policy bio extract request.';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.partner_policy_request_id IS 'Partner Policy Request ID: Refers to pms.partner_policy_request.id';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.part_id IS 'Partner ID: Refers to pms.partner.id';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.policy_id IS 'Policy ID: Refers to pms.auth_policy.id';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.attribute_name IS 'Attribute Name: Biometric attribute name like photo, face, iris and fingerprint';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.extractor_provider IS 'Extractor Provider: Biometric extractor provider information.';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.extractor_provider_version IS 'Extractor Provider Version: Version of biometric extractor provider';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.biometric_modality IS 'Biometric Modality: Biometric modality';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.biometric_sub_types IS 'Biometric Sub Type: Biometric sub type';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.status_code IS 'Status Code: Request status.';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.partner_policy_bioextract_request.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

ALTER TABLE pms.partner_policy_bioextract_request ADD CONSTRAINT fk_ppber_request FOREIGN KEY (partner_policy_request_id)
REFERENCES pms.partner_policy_request (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_bioextract_request ADD CONSTRAINT fk_ppber_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_bioextract_request ADD CONSTRAINT fk_ppber_policy FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

CREATE UNIQUE INDEX IF NOT EXISTS uniq_ppber_modality
ON pms.partner_policy_bioextract_request (
   partner_policy_request_id,
   biometric_modality
);

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.partner_policy_bioextract_request TO pmsuser;

-- -------------------------------------------------------------------------------------------------
-- Partner policy credential type request table for 1.3.0-beta.5
-- -------------------------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS pms.partner_policy_credential_type_request (
    id character varying(36) NOT NULL,
    partner_policy_request_id character varying(36) NOT NULL,
    part_id character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL,
    credential_type character varying(128) NOT NULL,
    status_code character varying(20) NOT NULL DEFAULT 'InProgress',
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,
    CONSTRAINT pk_ppctr_id PRIMARY KEY (id),
    CONSTRAINT chk_ppctr_status CHECK (status_code IN ('InProgress', 'approved', 'rejected'))
);

COMMENT ON TABLE pms.partner_policy_credential_type_request IS 'Partner Policy Credential Type Request: Stores partner requests for credential type mapping.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.id IS 'ID: Unique id for partner policy credential type request.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.partner_policy_request_id IS 'Partner Policy Request ID: Refers to pms.partner_policy_request.id';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.part_id IS 'Partner ID: Refers to pms.partner.id';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.policy_id IS 'Policy ID: Refers to pms.auth_policy.id';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.credential_type IS 'Credential Type: Credential type requested for mapping.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.status_code IS 'Status Code: Request status.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';

ALTER TABLE pms.partner_policy_credential_type_request ADD CONSTRAINT fk_ppctr_request FOREIGN KEY (partner_policy_request_id)
REFERENCES pms.partner_policy_request (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_credential_type_request ADD CONSTRAINT fk_ppctr_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_credential_type_request ADD CONSTRAINT fk_ppctr_policy FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

CREATE UNIQUE INDEX IF NOT EXISTS uniq_ppctr_request
ON pms.partner_policy_credential_type_request (partner_policy_request_id);

-- Only one InProgress per (partner, credential_type)
CREATE UNIQUE INDEX IF NOT EXISTS uniq_partner_cred_inprogress
ON pms.partner_policy_credential_type_request (part_id, credential_type)
WHERE status_code = 'InProgress';

-- Only one approved per (partner, credential_type)
CREATE UNIQUE INDEX IF NOT EXISTS uniq_partner_cred_approved
ON pms.partner_policy_credential_type_request (part_id, credential_type)
WHERE status_code = 'approved';

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.partner_policy_credential_type_request TO pmsuser;

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