-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.1
-- Purpose    		: Revoking Database Alter deployement done for release in PMS DB.       
-- Create By   		: Nagarjuna K
-- Created Date		: Aug-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

ALTER TABLE pms.partner DROP COLUMN logo_url;

ALTER TABLE pms.partner_h DROP COLUMN logo_url;

ALTER TABLE pms.partner DROP COLUMN addl_info;

ALTER TABLE pms.partner_h DROP COLUMN addl_info;

UPDATE pms.partner_type SET is_policy_required=false,upd_dtimes =now(), upd_by ='superadmin' WHERE code ='MISP_Partner'

ALTER TABLE pms.misp_license DROP COLUMN policy_id;

DROP TABLE IF EXISTS pms.oidc_client; 

