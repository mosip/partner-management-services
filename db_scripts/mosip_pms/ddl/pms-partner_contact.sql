-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name 	: pms.partner_contact
-- Purpose    	: Partner Contact: Registered external partners use will have mutiple contact and these contacts are maintained in this table.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted flag not null changes for 1.1.5   
-- ------------------------------------------------------------------------------------------

-- object: pms.partner_contact | type: TABLE --
-- DROP TABLE IF EXISTS pms.partner_contact CASCADE;
CREATE TABLE pms.partner_contact(
	id character varying(36) NOT NULL,
	partner_id character varying(36) NOT NULL,
	contact_no character varying(16),
	email_id character varying(254),
	address character varying(2000),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT partcnt PRIMARY KEY (id),
	CONSTRAINT uk_partcnt_idcnt UNIQUE (partner_id,contact_no),
	CONSTRAINT uk_partcnt_ideml UNIQUE (partner_id,email_id)

);
-- ddl-end --
COMMENT ON TABLE pms.partner_contact IS 'Partner Contact: Registered external partners use will have mutiple contact and these contacts are maintained in this table.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.id IS 'Contact ID : Unique ID generated / assigned for partner';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.partner_id IS 'Partner ID: partner id which is referenced from pms.partner table';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.contact_no IS 'Contact Number: Contact number of the partner organization or the contact person';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.email_id IS 'Email ID: Email ID of the MISP organization''s contact person';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.address IS 'Address: Address of the partner organization';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.partner_contact.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
