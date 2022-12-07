-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : pms.misp_license
-- Purpose    : MISP License: License key issued to MISP, using which an individual''s authentication request that is initiated from partners are authenticated.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Aug-2020             Sadanndegowda       Updated Schema name 
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted flag not null changes for 1.1.5  
-- Sep-2022		Nagarjuna 		Added column policy id changes for 1.2.1
-- ------------------------------------------------------------------------------------------

-- object: pms.misp_license | type: TABLE --
-- DROP TABLE IF EXISTS pms.misp_license CASCADE;
CREATE TABLE pms.misp_license(
	misp_id character varying(36) NOT NULL,
	license_key character varying(128) NOT NULL,
	policy_id character varying(128) NULL,
	valid_from_date timestamp NOT NULL,
	valid_to_date timestamp,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_mlic PRIMARY KEY (misp_id,license_key)

);
-- ddl-end --
COMMENT ON TABLE pms.misp_license IS 'MISP License: License key issued to MISP, using which an individual''s authentication request that is initiated from partners are authenticated.';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.misp_id IS 'MISP ID: MISP ID, refers to pmp.misp .id';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.license_key IS 'License Key: A system generated number assigned to MISP as License key. It will be used by MISP application to be appended to auth request which is received by partners.';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.valid_from_date IS 'Valid From Date: Datetime from when the license key is valid.';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.valid_to_date IS 'Valid To Date: Datetime when the license key will be expired.';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.misp_license.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
