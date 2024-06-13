-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.1.4
-- Purpose    		: Revoking Database Alter deployement done for release in PMS DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

DROP TABLE IF EXISTS pms.partner_policy_credential_type;