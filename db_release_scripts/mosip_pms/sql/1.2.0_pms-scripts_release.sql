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

INSERT INTO pms.policy_group (id,name,descr,user_id,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpolicygroup-default-cert','mpolicygroup-default-cert','mpolicygroup-default-cert','superadmin',true,'superadmin',now(),'superadmin',now(),false,NULL);

INSERT INTO pms.partner (id,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,lang_code) 
VALUES('mpartner-default-cert','mpolicygroup-default-cert','mpartner-default-cert','mpartner-default-cert','9232121212','info@mosip.io',NULL,'mpartner-default-cert','Internal_Partner','approved',true,'superadmin',now(),'superadmin',now(),false,NULL,NULL);

INSERT INTO pms.partner_h (id,eff_dtimes,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,lang_code) 
VALUES('mpartner-default-cert',now(),'mpolicygroup-default-cert','mpartner-default-cert','mpartner-default-cert','9232121212','info@mosip.io',NULL,'mpartner-default-cert','Internal_Partner','approved',true,'superadmin',now(),'superadmin',now(),false,NULL,NULL);


INSERT INTO pms.auth_policy (id,policy_group_id,name,descr,policy_file_id,policy_type,"version",policy_schema,valid_from_date,valid_to_date,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpolicy-default-cert','mpolicygroup-default-cert','mpolicy-default-cert','mpolicy-default-cert','{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":true},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":true},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":true},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":true},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":true},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":true},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":true},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":true},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":true},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":true},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":true},{"attributeName":"individualBiometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics"}],"encrypted":true,"format":"extraction"}],"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"none","shareDomain":"datashare.datashare","source":"ID Repository"}}','DataShare','1','https://schemas.mosip.io/v1/auth-policy',now(),now()+interval '12 years',true,'admin',now(),'admin',now(),false,NULL);

INSERT INTO pms.partner_policy_request (id,part_id,policy_id,request_datetimes,request_detail,status_code,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpartner_policy_cert_req','mpartner-default-cert','mpolicy-default-cert',now(),'mpolicy-default-cert','approved','admin',now(),'admin',now(),NULL,NULL);

INSERT INTO pms.partner_policy (policy_api_key,part_id,policy_id,valid_from_datetime,valid_to_datetime,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,"label") 
VALUES('mpolicy_part_cert_api','mpartner-default-cert','mpolicy-default-cert',now(),now()+interval '12 years',true,'admin',now(),'admin',now(),false,NULL,'mpolicy_part_cert_api');


CREATE extension dblink;

SELECT dblink_connect('auth_device_conn', 'dbname=mosip_authdevice port=':dbport' host=':dbhost' user=':dbuser' password=':dbpassword'');

INSERT INTO device_detail
SELECT *
FROM   dblink('auth_device_conn','SELECT id, dprovider_id, dtype_code, dstype_code, make, model, partner_org_name, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes FROM device_detail') AS (id varchar, dprovider_id varchar, dtype_code varchar, dstype_code varchar, make varchar, model varchar, partner_org_name varchar, approval_status varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp);

INSERT INTO secure_biometric_interface
SELECT *
FROM   dblink('auth_device_conn','SELECT id, sw_binary_hash, sw_version, sw_cr_dtimes, sw_expiry_dtimes, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes, provider_id, partner_org_name FROM secure_biometric_interface') AS (id varchar, sw_binary_hash bytea, sw_version varchar, sw_cr_dtimes timestamp, sw_expiry_dtimes timestamp, approval_status varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp, provider_id varchar, partner_org_name varchar);

INSERT INTO secure_biometric_interface_h
SELECT *
FROM   dblink('auth_device_conn','SELECT id, sw_binary_hash, sw_version, sw_cr_dtimes, sw_expiry_dtimes, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes, eff_dtimes, provider_id, partner_org_name FROM secure_biometric_interface_h') AS(id varchar, sw_binary_hash bytea, sw_version varchar, sw_cr_dtimes timestamp, sw_expiry_dtimes timestamp, approval_status varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp, eff_dtimes timestamp, provider_id varchar, partner_org_name varchar);

INSERT INTO device_detail_sbi
SELECT *
FROM   dblink('auh_device_conn','select dd.dprovider_id,dd.partner_org_name,dd.id,id,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes from secure_biometric_interface inner join device_detail dd where  device_detail_id = dd.id') AS(dprovider_id varchar, partner_org_name varchar, device_detail_id varchar, sbi_id varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp);

INSERT INTO ftp_chip_detail
SELECT id,
       foundational_trust_provider_id,
       make,
       model,
       certificate_alias,
       partner_org_name,
       is_active,
       cr_by,
       cr_dtimes,
       upd_by,
       upd_dtimes,
       is_deleted,
       del_dtimes
FROM   dblink('auth_device_conn', 'select id, foundational_trust_provider_id, make, model, certificate_alias, partner_org_name, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes from ftp_chip_detail') AS(id varchar, foundational_trust_provider_id varchar, make varchar, model varchar, certificate_alias varchar, partner_org_name varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp);

SELECT dblink_connect('reg_device_conn', 'dbname=mosip_regdevice port=':dbport' host=':dbhost' user=':dbuser' password=':dbpassword'');

INSERT INTO device_detail
SELECT *
FROM   dblink('reg_device_conn','SELECT id, dprovider_id, dtype_code, dstype_code, make, model, partner_org_name, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes FROM device_detail') AS (id varchar, dprovider_id varchar, dtype_code varchar, dstype_code varchar, make varchar, model varchar, partner_org_name varchar, approval_status varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp);

INSERT INTO secure_biometric_interface
SELECT *
FROM   dblink('reg_device_conn','SELECT id, sw_binary_hash, sw_version, sw_cr_dtimes, sw_expiry_dtimes, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes, provider_id, partner_org_name FROM secure_biometric_interface') AS (id varchar, sw_binary_hash bytea, sw_version varchar, sw_cr_dtimes timestamp, sw_expiry_dtimes timestamp, approval_status varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp, provider_id varchar, partner_org_name varchar);

INSERT INTO secure_biometric_interface_h
SELECT *
FROM   dblink('reg_device_conn','SELECT id, sw_binary_hash, sw_version, sw_cr_dtimes, sw_expiry_dtimes, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes, eff_dtimes, provider_id, partner_org_name FROM secure_biometric_interface_h') AS(id varchar, sw_binary_hash bytea, sw_version varchar, sw_cr_dtimes timestamp, sw_expiry_dtimes timestamp, approval_status varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp, eff_dtimes timestamp, provider_id varchar, partner_org_name varchar);

INSERT INTO device_detail_sbi
SELECT *
FROM   dblink('reg_device_conn','select dd.dprovider_id,dd.partner_org_name,dd.id,id,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes from secure_biometric_interface inner join device_detail dd where  device_detail_id = dd.id') AS(dprovider_id varchar, partner_org_name varchar, device_detail_id varchar, sbi_id varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp);

INSERT INTO ftp_chip_detail
SELECT id,
       foundational_trust_provider_id,
       make,
       model,
       certificate_alias,
       partner_org_name,
       is_active,
       cr_by,
       cr_dtimes,
       upd_by,
       upd_dtimes,
       is_deleted,
       del_dtimes
FROM   dblink('reg_device_conn', 'select id, foundational_trust_provider_id, make, model, certificate_alias, partner_org_name, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes from ftp_chip_detail') AS(id varchar, foundational_trust_provider_id varchar, make varchar, model varchar, certificate_alias varchar, partner_org_name varchar, is_active bool, cr_by varchar, cr_dtimes timestamp, upd_by varchar, upd_dtimes timestamp, is_deleted bool, del_dtimes timestamp);