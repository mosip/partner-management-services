-- -------------------------------------------------------------------------------------------------
-- Database Name    : mosip_pms
-- Release Version 	: 1.2.2.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Swetha K
-- Created Date		: Aug-2024
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------
\c mosip_pms

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

-- Dropping unique constraint from pms.device_detail table if it exists
ALTER TABLE pms.device_detail
    DROP CONSTRAINT IF EXISTS uk_devdtl_id;

-- Creating unique index for make, model, and approval status if it does not exist
CREATE UNIQUE INDEX IF NOT EXISTS uk_devdtl_make_model_approval_status
    ON pms.device_detail (dprovider_id, dtype_code, dstype_code, make, model)
    WHERE approval_status != 'rejected' 
    AND NOT (approval_status = 'approved' AND is_active = false);

-- Dropping unique constraint from pms.ftp_chip_detail table if it exists
ALTER TABLE pms.ftp_chip_detail
    DROP CONSTRAINT IF EXISTS uk_fcdtl_id;

-- Creating unique index for make, model, and approval status if it does not exist
CREATE UNIQUE INDEX IF NOT EXISTS uk_fcdtl_make_model_approval_status
    ON pms.ftp_chip_detail (foundational_trust_provider_id, make, model)
    WHERE approval_status != 'rejected' 
    AND NOT (approval_status = 'approved' AND is_active = false);

-- Updating policy_file_id in pms.auth_policy for specific IDs
UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-qrcode';

UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-reprint';