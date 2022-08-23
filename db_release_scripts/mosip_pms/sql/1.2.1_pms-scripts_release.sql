-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.1
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Nagarjuna K
-- Created Date		: Aug-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

ALTER TABLE pms.partner ADD COLUMN logo_url character varying(256);

ALTER TABLE pms.partner_h ADD COLUMN logo_url character varying(256);

ALTER TABLE pms.partner ADD COLUMN addl_info character varying;

ALTER TABLE pms.partner_h ADD COLUMN addl_info character varying;

INSERT INTO pms.partner_type (code,partner_description,is_policy_required,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) VALUES
	 ('Print_Partner','Print Partner',true,true,'superadmin',now(),NULL,NULL,false,NULL);
