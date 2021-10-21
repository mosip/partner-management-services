-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : pms.partner
-- Purpose    : Partner: Registered external partners use the authentication services provided by MOSIP. The auth services are channeled through MISPs. This table stores the master list of partners who can self register themselves and use auth services.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted flag not null changes for 1.1.5.2  
-- ------------------------------------------------------------------------------------------

-- object: pms.partner | type: TABLE --
-- DROP TABLE IF EXISTS pms.partner CASCADE;
CREATE TABLE pms.partner(
	id character varying(36) NOT NULL,
	policy_group_id character varying(36),
	name character varying(128) NOT NULL,
	address character varying(2000),
	contact_no character varying(16),
	email_id character varying(254),
	certificate_alias character varying(128),
	user_id character varying(256) NOT NULL,
	partner_type_code character varying(36) NOT NULL,
	approval_status character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_part PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE pms.partner IS 'Partner: Registered external partners use the authentication services provided by MOSIP. The auth services are channeled through MISPs. This table stores the master list of partners who can self register themselves and use auth services.';
-- ddl-end --
COMMENT ON COLUMN pms.partner.id IS 'Partner ID : Unique ID generated / assigned for partner';
-- ddl-end --
COMMENT ON COLUMN pms.partner.policy_group_id IS 'Policy Group ID: Policy group to which the partner registers for to avail the auth services.';
-- ddl-end --
COMMENT ON COLUMN pms.partner.name IS 'Name: Name of the Partner.';
-- ddl-end --
COMMENT ON COLUMN pms.partner.address IS 'Address: Address of the partner organization';
-- ddl-end --
COMMENT ON COLUMN pms.partner.contact_no IS 'Contact Number: Contact number of the partner organization or the contact person';
-- ddl-end --
COMMENT ON COLUMN pms.partner.email_id IS 'Email ID: Email ID of the MISP organization''s contact person';
-- ddl-end --
COMMENT ON COLUMN pms.partner.certificate_alias IS 'Certificate Alias: Certificate alias provided by the partner to MOSIP to use its authentication request data.';
-- ddl-end --
COMMENT ON COLUMN pms.partner.user_id IS 'Partner Admin: When a partner registers themselves to avail auth services, a user id is created for them to login to partner management portal to perform few operational activities. Currently only one user is created per partner.';
-- ddl-end --
COMMENT ON COLUMN pms.partner.partner_type_code IS 'Partner Type Code: Partner type code for different type of partners... Referenced from pmp.partner_type table';
-- ddl-end --
COMMENT ON COLUMN pms.partner.approval_status IS 'Approval Status: Status of the partner. Status gives the partner status is pending, approved or rejected by partner admin';
-- ddl-end --
COMMENT ON COLUMN pms.partner.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.partner.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.partner.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.partner.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.partner.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.partner.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.partner.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
