-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : pms.partner_policy_credential_type_request
-- Purpose    : Partner Policy Credential Type Request: Stores partner requests for credential type mapping.
--
-- object: pms.partner_policy_credential_type_request | type: TABLE --
-- DROP TABLE IF EXISTS pms.partner_policy_credential_type_request CASCADE;
CREATE TABLE pms.partner_policy_credential_type_request (
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

    is_deleted boolean DEFAULT FALSE,
    del_dtimes timestamp,

    CONSTRAINT pk_ppctr_id PRIMARY KEY (id),

    CONSTRAINT chk_ppctr_status
        CHECK (status_code IN ('InProgress', 'approved', 'rejected'))
);

COMMENT ON TABLE pms.partner_policy_credential_type_request IS 'Partner Policy Credential Type Request: Stores partner requests for credential type mapping.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.id IS 'ID: Unique id for partner policy credential type request.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.partner_policy_request_id IS 'Partner Policy Request ID: Refers to pms.partner_policy_request.id';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.part_id IS 'Partner ID: Refers to pms.partner.id';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.policy_id IS 'Policy ID: Refers to pms.auth_policy.id';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.credential_type IS 'Credential Type: Credential type being requested.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.status_code IS 'Status Code: Request status.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN pms.partner_policy_credential_type_request.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
