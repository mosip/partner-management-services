-- -------------------------------------------------------------------------------------------------
-- Database Name         : mosip_pms
-- Release Version 	 : 1.2.0
-- Purpose    		 : Revoking Database Alter deployement done for release in PMS DB.       
-- Create By   		 : Yash Mohan
-- Created Date		 : Apr-2023
-- 
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

ALTER TABLE pms.device_detail DROP CONSTRAINT fk_devdtl_id;

ALTER TABLE pms.reg_device_sub_type DROP CONSTRAINT fk_rdstyp_dtype_code;

ALTER TABLE pms.partner_policy DROP COLUMN label;

ALTER TABLE pms.partner DROP COLUMN lang_code;

ALTER TABLE pms.partner_h DROP COLUMN lang_code;

ALTER TABLE pms.ftp_chip_detail DROP COLUMN approval_status;
