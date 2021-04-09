-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : pms.auth_policy_h
-- Purpose    : Authentication Policy History: This to track changes to master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ), Effective DateTimestamp is used for identifying latest or point in time information. Refer pmp.auth_policy table description for details.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Aug-2020             Sadanndegowda       Added policy_type, version, schema and validity
-- Aug-2020             Sadanndegowda       Updated Schema name 
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false 
-- Mar-2021		Ram Bhatt	    Reverting is_deleted flag not null changes for 1.1.5  
-- ------------------------------------------------------------------------------------------

-- object: pms.auth_policy_h | type: TABLE --
-- DROP TABLE IF EXISTS pms.auth_policy_h CASCADE;
CREATE TABLE pms.auth_policy_h(
	id character varying(36) NOT NULL,
	eff_dtimes timestamp NOT NULL,
	policy_group_id character varying(36),
	name character varying(128) NOT NULL,
	descr character varying(256) NOT NULL,
	policy_file_id character varying(5120) NOT NULL,
	policy_type character varying(36) NOT NULL,
	version character varying(8) NOT NULL,
	policy_schema character varying(5120),
	valid_from_date timestamp NOT NULL,
	valid_to_date timestamp NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_apolh PRIMARY KEY (id,eff_dtimes),
	CONSTRAINT uk_apolh UNIQUE (eff_dtimes,policy_group_id,name)

);
-- ddl-end --
COMMENT ON TABLE pms.auth_policy_h IS 'Authentication Policy History: This to track changes to master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ), Effective DateTimestamp is used for identifying latest or point in time information. Refer pmp.auth_policy table description for details.   
';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.id IS 'ID: A unique identity ';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.eff_dtimes IS 'Effective Date Timestamp : This to track master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ).  The current record is effective from this date-time. ';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.policy_group_id IS 'Polocy Group ID: Id of the policy group to which this policy belongs.';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.name IS 'Name: Name of the policy';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.descr IS 'Description: Description of the policy';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.policy_file_id IS 'Policy File ID: Policy are defined by Policy / Partner manager are stored in file system or key based storages like CEPH. The policy file details (location / id / key) is stored here.';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.policy_type IS 'Policy Type: Type of the policy for example authentication, Data_Share, Credential_Issuance...etc.';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.version IS 'Policy Version : Version number of the policy, Version to be upgraded based on changes to the policy';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.policy_schema IS 'Policy Schema: Policy schema, schema is populated based on policy type';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.valid_from_date IS 'Policy Valid From Date: Date and time from when the policy is valid';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.valid_to_date IS 'Valid To Date: Date and time till when the policy is valid';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.auth_policy_h.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
