-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name 	: pms.partner_type
-- Purpose    	: Partner Type: List of different partnet are allowed in partnet management.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- 
-- ------------------------------------------------------------------------------------------

-- object: pms.partner_type | type: TABLE --
-- DROP TABLE IF EXISTS pms.partner_type CASCADE;
CREATE TABLE pms.partner_type(
	code character varying(36) NOT NULL,
	partner_description character varying(128) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT pk_partyp_code PRIMARY KEY (code)

);
-- ddl-end --
COMMENT ON TABLE pms.partner_type IS 'Partner Type: List of different partnet are allowed in partnet management';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.code IS 'Partner ID : Unique ID generated / assigned for partner';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.partner_description IS 'Partner Description : Description of the partner type';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_type.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
