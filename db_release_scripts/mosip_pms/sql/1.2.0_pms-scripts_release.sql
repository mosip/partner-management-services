-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin


\ir ../ddl/pms-device_detail.sql
\ir ../ddl/pms-ftp_chip_detail.sql
\ir ../ddl/pms-reg_device_sub_type.sql
\ir ../ddl/pms-reg_device_type.sql
\ir ../ddl/pms-secure_biometric_interface.sql
\ir ../ddl/pms-secure_biometric_interface_h.sql

-- object: fk_devdtl_id | type: CONSTRAINT --
-- ALTER TABLE pms.device_detail DROP CONSTRAINT IF EXISTS fk_devdtl_id CASCADE;
ALTER TABLE pms.device_detail ADD CONSTRAINT fk_devdtl_id FOREIGN KEY (dtype_code,dstype_code)
REFERENCES pms.reg_device_sub_type (dtyp_code,code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_rdstyp_dtype_code | type: CONSTRAINT --
-- ALTER TABLE pms.reg_device_sub_type DROP CONSTRAINT IF EXISTS fk_rdstyp_dtype_code CASCADE;
ALTER TABLE pms.reg_device_sub_type ADD CONSTRAINT fk_rdstyp_dtype_code FOREIGN KEY (dtyp_code)
REFERENCES pms.reg_device_type (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --
