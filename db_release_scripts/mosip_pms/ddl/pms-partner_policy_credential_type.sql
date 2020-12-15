-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name 	: pms.partner_policy_credential_type
-- Purpose    	: Partner Policy and Credential Type: Table to map partner, Partner policy and credential type for the partner.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- 
-- ------------------------------------------------------------------------------------------

-- object: pms.partner_policy_credential_type | type: TABLE --
-- DROP TABLE IF EXISTS pms.partner_policy_credential_type CASCADE;
CREATE TABLE pms.partner_policy_credential_type(
	part_id character varying(36) NOT NULL,
	policy_id character varying(36) NOT NULL,
	credential_type character varying(128) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT pk_ppctyp_id PRIMARY KEY (part_id,policy_id,credential_type)

);
-- ddl-end --
COMMENT ON TABLE pms.partner_policy_credential_type IS 'Partner Policy and Credential Type: Table to map partner, Partner policy and credential type for the partner.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.part_id IS 'Partner ID: Partner ID, refers to pmp.partner .id';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.policy_id IS 'Policy ID: Policy ID, refers to pmp.auth_policy .id';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.credential_type IS 'Credential Type: Credential type which is mapped to partner with specific policy';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_credential_type.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --