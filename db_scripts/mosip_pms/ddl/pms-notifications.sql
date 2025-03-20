-- This table stores notifications for root, intermediate, and partner certificate expiry, as well as SBI expiry and API key expiry.
CREATE TABLE pms.notifications
(
    id character varying(128) NOT NULL,
    partner_id character varying(128) NOT NULL,
    notification_type character varying(36) NOT NULL,
    notification_status character varying(36) NOT NULL,
	notification_details_json character varying(4000) NOT NULL,
    email_id character varying(256) NOT NULL,
    email_lang_code character varying(36) NOT NULL,
    email_sent boolean DEFAULT FALSE,
    email_sent_dtimes timestamp,
    cr_by character varying(128) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(128),
    upd_dtimes timestamp,
    CONSTRAINT notifications_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE pms.notifications IS 'This table stores notifications along with their details.';
COMMENT ON COLUMN pms.notifications.id IS 'ID: A unique identifier for the notification.';
COMMENT ON COLUMN pms.notifications.partner_id IS 'Partner ID: A unique identifier for the partner.';
COMMENT ON COLUMN pms.notifications.notification_type IS 'Notification Type: The type of notification generated. Examples include PARTNER_CERT_EXPIRY, ROOT_CERT_EXPIRY, and SBI_EXPIRY.';
COMMENT ON COLUMN pms.notifications.notification_status IS 'Notification Status: The current status of the notification. Possible values include ACTIVE and DISMISSED.';
COMMENT ON COLUMN pms.notifications.notification_details_json IS 'Notification Details (JSON): Detailed information about the notification in JSON format.';
COMMENT ON COLUMN pms.notifications.email_id IS 'Email ID: The email address of the partner to whom the notification is sent.';
COMMENT ON COLUMN pms.notifications.email_lang_code IS 'Email Language Code: The language code used for the email.';
COMMENT ON COLUMN pms.notifications.email_sent IS 'Email Sent: Indicates whether the email has been sent (TRUE) or not (FALSE).';
COMMENT ON COLUMN pms.notifications.email_sent_dtimes IS 'Email Sent Timestamp: The date and time when the email was sent.';
COMMENT ON COLUMN pms.notifications.cr_dtimes IS 'Created Timestamp: The date and time when the record was created.';
COMMENT ON COLUMN pms.notifications.cr_by IS 'Created By: The ID or name of the user who created the record.';
COMMENT ON COLUMN pms.notifications.upd_by IS 'Updated By: The ID or name of the user who last updated the record.';
COMMENT ON COLUMN pms.notifications.upd_dtimes IS 'Updated Timestamp: The date and time when any field in the record was last updated.';