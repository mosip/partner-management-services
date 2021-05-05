-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.1.5.1
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------
-- Mar-2021		Ram Bhatt	    Reverting is_deleted flag not null changes for 1.1.5.1  
------------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

----------------------------------------------------------------------------------------------------

--ALTER TABLE pms.misp ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.misp_license ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.auth_policy_h ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.policy_group ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner_policy ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.auth_policy ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner_policy_request ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner_type ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner_h ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner_contact ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.otp_transaction ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner_policy_credential_type ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE pms.partner_policy_bioextract ALTER COLUMN is_deleted SET NOT NULL;


--ALTER TABLE pms.misp ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.misp_license ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.auth_policy_h ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.policy_group ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner_policy ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.auth_policy ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner_policy_request ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner_type ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner_h ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner_contact ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.otp_transaction ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner_policy_credential_type ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE pms.partner_policy_bioextract ALTER COLUMN is_deleted SET DEFAULT FALSE;

-----------------------------------------------------------------------------------------------
