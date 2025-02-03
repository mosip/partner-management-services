-- -------------------------------------------------------------------------------------------------
-- Database Name    : mosip_pms
-- Release Version 	: release-1.2.2.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Swetha K
-- Created Date		: Aug-2024
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------
\c mosip_pms

-- user_details
DROP TABLE IF EXISTS pms.user_details;

-- Dropping unique index from pms.device_detail table if it exists
DROP INDEX IF EXISTS pms.uk_devdtl_make_model_approval_status;

-- Adding unique constraint for dprovider_id, dtype_code, dstype_code, make, and model
-- Only if the constraint does not already exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_constraint 
        WHERE conname = 'uk_devdtl_id' 
        AND conrelid = 'pms.device_detail'::regclass
    ) THEN
        ALTER TABLE pms.device_detail
        ADD CONSTRAINT uk_devdtl_id 
        UNIQUE (dprovider_id, dtype_code, dstype_code, make, model);
    END IF;
END $$;

-- Dropping unique index from pms.ftp_chip_detail table if it exists
DROP INDEX IF EXISTS pms.uk_fcdtl_make_model_approval_status;

-- Adding unique constraint for foundational_trust_provider_id, make, and model
-- Only if the constraint does not already exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM pg_constraint 
        WHERE conname = 'uk_fcdtl_id' 
        AND conrelid = 'pms.ftp_chip_detail'::regclass
    ) THEN
        ALTER TABLE pms.ftp_chip_detail
        ADD CONSTRAINT uk_fcdtl_id 
        UNIQUE (foundational_trust_provider_id, make, model);
    END IF;
END $$;

-- Updating policy_file_id in pms.auth_policy for specific IDs
UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-qrcode';

UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-reprint';