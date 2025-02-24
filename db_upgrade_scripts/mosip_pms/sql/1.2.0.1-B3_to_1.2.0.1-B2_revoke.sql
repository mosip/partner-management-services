-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.1
-- Purpose    		: Revoking Database Alter deployement done for release in PMS DB.       
-- Create By   		: Balaji A
-- Created Date		: Mar-2023
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------



\c mosip_pms sysadmin

DELETE FROM pms.partner_type WHERE code = 'Binding_Partner';