-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- 15-07-2022           Nagarjuna K          Added dml insert scripts 
-- 15-07-2022           Nagarjuna K          Merged 1.2.0.1 changes to 1.2.0 release patch -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin


\ir ../ddl/pms-device_detail.sql
\ir ../ddl/pms-ftp_chip_detail.sql
\ir ../ddl/pms-reg_device_sub_type.sql
\ir ../ddl/pms-reg_device_type.sql
\ir ../ddl/pms-secure_biometric_interface.sql
\ir ../ddl/pms-secure_biometric_interface_h.sql


\ir ../ddl/pms-device_detail_sbi.sql

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

ALTER TABLE pms.partner_policy ADD COLUMN label character varying(36);
UPDATE pms.partner_policy SET label=policy_api_key;
ALTER TABLE pms.partner_policy ALTER COLUMN label SET NOT NULL;

ALTER TABLE pms.partner ADD COLUMN lang_code character varying(36);

ALTER TABLE pms.partner_h ADD COLUMN lang_code character varying(36);

TRUNCATE TABLE pms.reg_device_type cascade ;
\COPY pms.reg_device_type (code,name,descr,is_active,cr_by,cr_dtimes) FROM '../dml/pms-reg_device_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.reg_device_sub_type cascade ;
\COPY pms.reg_device_sub_type (code,dtyp_code,name,descr,is_active,cr_by,cr_dtimes) FROM '../dml/pms-reg_device_sub_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.partner_policy_credential_type cascade ;
\COPY pms.partner_policy_credential_type (part_id,policy_id,credential_type,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) FROM '../dml/pms-partner_policy_credential_type.csv' delimiter ',' HEADER  csv;


ALTER TABLE pms.ftp_chip_detail ADD COLUMN approval_status character varying(36);
UPDATE pms.ftp_chip_detail SET approval_status='pending_cert_upload' where certificate_alias is null;
UPDATE pms.ftp_chip_detail SET approval_status='pending_approval' where certificate_alias is not null and is_active =false;
UPDATE pms.ftp_chip_detail SET approval_status='approved' where certificate_alias is not null and is_active = true;
ALTER TABLE pms.ftp_chip_detail ALTER COLUMN approval_status SET NOT NULL;
