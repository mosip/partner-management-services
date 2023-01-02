
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
    lang_code character varying(36),
	logo_url character varying(256),
	addl_info character varying,
    CONSTRAINT pk_part PRIMARY KEY (id)
);

COMMENT ON TABLE pms.partner IS 'Partner: Registered external partners use the authentication services provided by MOSIP. The auth services are channeled through MISPs. This table stores the master list of partners who can self register themselves and use auth services.';
COMMENT ON COLUMN pms.partner.id IS 'Partner ID : Unique ID generated / assigned for partner';
COMMENT ON COLUMN pms.partner.policy_group_id IS 'Policy Group ID: Policy group to which the partner registers for to avail the auth services.';
COMMENT ON COLUMN pms.partner.name IS 'Name: Name of the Partner.';
COMMENT ON COLUMN pms.partner.address IS 'Address: Address of the partner organization';
COMMENT ON COLUMN pms.partner.contact_no IS 'Contact Number: Contact number of the partner organization or the contact person';
COMMENT ON COLUMN pms.partner.email_id IS 'Email ID: Email ID of the MISP organization''s contact person';
COMMENT ON COLUMN pms.partner.certificate_alias IS 'Certificate Alias: Certificate alias provided by the partner to MOSIP to use its authentication request data.';
COMMENT ON COLUMN pms.partner.user_id IS 'Partner Admin: When a partner registers themselves to avail auth services, a user id is created for them to login to partner management portal to perform few operational activities. Currently only one user is created per partner.';
COMMENT ON COLUMN pms.partner.partner_type_code IS 'Partner Type Code: Partner type code for different type of partners... Referenced from pmp.partner_type table';
COMMENT ON COLUMN pms.partner.approval_status IS 'Approval Status: Status of the partner. Status gives the partner status is pending, approved or rejected by partner admin';
COMMENT ON COLUMN pms.partner.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
COMMENT ON COLUMN pms.partner.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN pms.partner.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.partner.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.partner.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN pms.partner.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN pms.partner.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
