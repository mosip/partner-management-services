-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : pms.partner_policy_bioextract_request
-- Purpose    : Partner Policy Bio Extract Request: Stores partner requests for biometric extraction configuration.
--
-- object: pms.partner_policy_bioextract_request | type: TABLE --
-- DROP TABLE IF EXISTS pms.partner_policy_bioextract_request CASCADE;
CREATE TABLE pms.partner_policy_bioextract_request (
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
-- ddl-end --

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

