-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : pms.partner_policy_bioextract
-- Purpose    : Partner Policy Bio Extract: Stores the partner policy biometric information with biometric extract provider details.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Oct-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted flag not null changes for 1.1.5  
-- ------------------------------------------------------------------------------------------
-- object: pms.partner_policy_bioextract | type: TABLE --
-- DROP TABLE IF EXISTS pms.partner_policy_bioextract CASCADE;
CREATE TABLE pms.partner_policy_bioextract(
	id character varying(36) NOT NULL,
	part_id character varying(36) NOT NULL,
	policy_id character varying(36) NOT NULL,
	attribute_name character varying(128) NOT NULL,
	extractor_provider character varying(128) NOT NULL,
	extractor_provider_version character varying(8),
	biometric_modality character varying(64) NOT NULL,
	biometric_sub_types character varying(64),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_ppbe_id PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE pms.partner_policy_bioextract IS 'Partner Policy Bio Extract: Stores the partner policy biometric information with biometric extract provider details.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.id IS 'ID: Unique id to store all the partner policy biometric extractor details';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.part_id IS 'Partner ID: Partner ID, refers to pmp.partner .id';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.policy_id IS 'Policy ID: Policy ID, refers to pmp.policy .id';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.attribute_name IS 'Attribute Name: Biometric attribute name like photo, face, iris, fingerprint...etc.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.extractor_provider IS 'Extractor Provider: Biometric extractor provider information.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.extractor_provider_version IS 'Extractor Provider Version: Version of biometric extractor provider';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.biometric_modality IS 'Biometric Modality: Biometric modality';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.biometric_sub_types IS 'Biometric Sub Type: Biometric sub type';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_policy_bioextract.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
