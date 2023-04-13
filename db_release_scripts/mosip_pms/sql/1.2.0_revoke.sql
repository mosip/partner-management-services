-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.0
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

ALTER TABLE pms.device_detail DROP CONSTRAINT fk_devdtl_id;

ALTER TABLE pms.reg_device_sub_type DROP CONSTRAINT fk_rdstyp_dtype_code;

ALTER TABLE pms.partner DROP COLUMN lang_code;

ALTER TABLE pms.partner_h DROP COLUMN lang_code;