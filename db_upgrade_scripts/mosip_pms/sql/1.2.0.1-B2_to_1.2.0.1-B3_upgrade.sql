-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.1
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Balaji A
-- Created Date		: Mar-2023
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------




\c mosip_pms sysadmin

INSERT INTO pms.partner_type (code,partner_description,is_policy_required,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) VALUES
	 ('Binding_Partner','Binding Partner',true,true,'superadmin',now(),NULL,NULL,false,NULL) ON CONFLICT (code) DO NOTHING;