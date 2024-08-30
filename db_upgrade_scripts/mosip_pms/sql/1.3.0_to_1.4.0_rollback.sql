-- -------------------------------------------------------------------------------------------------
-- Database Name    : mosip_pms
-- Release Version 	: 1.4.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Swetha K
-- Created Date		: Aug-2024
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

-- Remove the fk_mapping_device_detail_id foreign key constraint from pms.device_detail_sbi.
ALTER TABLE pms.device_detail_sbi
DROP CONSTRAINT fk_mapping_device_detail_id;

-- Remove the fk_mapping_sbi_id foreign key constraint from pms.device_detail_sbi.
ALTER TABLE pms.device_detail_sbi
DROP CONSTRAINT fk_mapping_sbi_id;