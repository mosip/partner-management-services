\c mosip_pms

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
