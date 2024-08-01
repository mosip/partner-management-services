-- This table has consents of users.
CREATE TABLE pms.user_details(
    id character varying(36) NOT NULL,
    user_id character varying(64) NOT null,
    consent_given character varying(36) NOT NULL DEFAULT 'NO',
    consent_given_dtimes timestamp NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(64) NOT NULL,
    upd_by character varying(64),
    upd_dtimes timestamp,
    CONSTRAINT user_details_pk PRIMARY KEY (id),
    CONSTRAINT consent_given CHECK (consent_given IN ('YES', 'NO'))
);
COMMENT ON TABLE pms.user_details IS 'This table has consents of users.';
COMMENT ON COLUMN pms.user_details.user_id IS 'User Id: user id of the user.';
COMMENT ON COLUMN pms.user_details.id IS 'ID: Unique Id generated.';
COMMENT ON COLUMN pms.user_details.consent_given_dtimes IS 'Consent given DateTimestamp : Date and Timestamp when the consent is given.';
COMMENT ON COLUMN pms.user_details.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.user_details.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN pms.user_details.consent_given IS 'Consent Given : Indicates whether consent has been given by the user.';
COMMENT ON COLUMN pms.user_details.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.user_details.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
