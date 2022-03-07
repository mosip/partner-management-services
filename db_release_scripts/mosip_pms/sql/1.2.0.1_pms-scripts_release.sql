-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.0.1
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Mar-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

ALTER TABLE pms.ftp_chip_detail ADD COLUMN approval_status character varying(36);
UPDATE pms.ftp_chip_detail SET approval_status='certpending' where certificate_alias is null;
UPDATE pms.ftp_chip_detail SET approval_status='pending_approval' where certificate_alias is not null and is_active =false;
UPDATE pms.ftp_chip_detail SET approval_status='approved' where certificate_alias is not null and is_active = true;
ALTER TABLE pms.ftp_chip_detail ALTER COLUMN approval_status SET NOT NULL;