\c mosip_pms

-- Rollback script for the license_key_name column in misp_license table
ALTER TABLE pms.misp_license DROP COLUMN license_key_name;

-- Remove NOT NULL constraint from email_id
ALTER TABLE pms.partner
    ALTER COLUMN email_id DROP NOT NULL;

-- Create the otp_transaction table
CREATE TABLE pms.otp_transaction (
    id character varying(36) NOT NULL,
    ref_id character varying(64) NOT NULL,
    otp_hash character varying(512) NOT NULL,
    generated_dtimes timestamp,
    expiry_dtimes timestamp,
    validation_retry_count smallint,
    status_code character varying(36),
    is_active boolean NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,
    is_deleted boolean DEFAULT FALSE,
    del_dtimes timestamp,
    CONSTRAINT pk_otpt_id PRIMARY KEY (id)
);

COMMENT ON TABLE pms.otp_transaction IS 'OTP Transaction: All OTP related data and validation details are maintained here for partner management service.';
COMMENT ON COLUMN pms.otp_transaction.id IS 'ID: Unique transaction id for each OTP transaction request';
COMMENT ON COLUMN pms.otp_transaction.ref_id IS 'Reference ID: Reference information received from OTP requester used while validating the OTP.';
COMMENT ON COLUMN pms.otp_transaction.otp_hash IS 'OTP Hash: Hash of id, ref_id and otp based on configuration setup and sent to requester module.';
COMMENT ON COLUMN pms.otp_transaction.generated_dtimes IS 'Generated Date Time: Timestamp when the OTP was generated';
COMMENT ON COLUMN pms.otp_transaction.expiry_dtimes IS 'Expiry Date Time: Timestamp when the OTP expires';
COMMENT ON COLUMN pms.otp_transaction.validation_retry_count IS 'Validation Retry Count: Number of OTP validation retries.';
COMMENT ON COLUMN pms.otp_transaction.status_code IS 'Status Code: Status of the OTP (active or expired).';
COMMENT ON COLUMN pms.otp_transaction.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
COMMENT ON COLUMN pms.otp_transaction.cr_by IS 'Created By : ID or name of the user who created the record';
COMMENT ON COLUMN pms.otp_transaction.cr_dtimes IS 'Created DateTimestamp : When the record was inserted';
COMMENT ON COLUMN pms.otp_transaction.upd_by IS 'Updated By : ID or name of the user who updated the record';
COMMENT ON COLUMN pms.otp_transaction.upd_dtimes IS 'Updated DateTimestamp : When the record was last updated';
COMMENT ON COLUMN pms.otp_transaction.is_deleted IS 'IS_Deleted : Soft delete flag';
COMMENT ON COLUMN pms.otp_transaction.del_dtimes IS 'Deleted DateTimestamp : When the record was soft deleted';    