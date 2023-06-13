\c mosip_pms

REASSIGN OWNED BY sysadmin TO postgres;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA pms FROM pmsuser;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA pms FROM sysadmin;

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON ALL TABLES IN SCHEMA pms TO pmsuser;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA pms TO postgres;

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

INSERT INTO pms.policy_group (id,name,descr,user_id,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpolicygroup-default-cert','mpolicygroup-default-cert','mpolicygroup-default-cert','superadmin',true,'superadmin',now(),'superadmin',now(),false,NULL);

INSERT INTO pms.partner (id,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,lang_code) 
VALUES('mpartner-default-cert','mpolicygroup-default-cert','mpartner-default-cert','mpartner-default-cert','9232121212','info@mosip.io',NULL,'mpartner-default-cert','Credential_Partner','approved',true,'superadmin',now(),'superadmin',now(),false,NULL,NULL);

INSERT INTO pms.partner_h (id,eff_dtimes,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,lang_code) 
VALUES('mpartner-default-cert',now(),'mpolicygroup-default-cert','mpartner-default-cert','mpartner-default-cert','9232121212','info@mosip.io',NULL,'mpartner-default-cert','Credential_Partner','approved',true,'superadmin',now(),'superadmin',now(),false,NULL,NULL);


INSERT INTO pms.auth_policy (id,policy_group_id,name,descr,policy_file_id,policy_type,"version",policy_schema,valid_from_date,valid_to_date,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpolicy-default-cert','mpolicygroup-default-cert','mpolicy-default-cert','mpolicy-default-cert','{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":true},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":true},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":true},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":true},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":true},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":true},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":true},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":true},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":true},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":true},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":true},{"attributeName":"individualBiometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics"}],"encrypted":true,"format":"extraction"}],"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"none","shareDomain":"datashare.datashare","source":"ID Repository"}}','DataShare','1','https://schemas.mosip.io/v1/auth-policy',now(),now()+interval '12 years',true,'admin',now(),'admin',now(),false,NULL);

INSERT INTO pms.partner_policy_request (id,part_id,policy_id,request_datetimes,request_detail,status_code,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpartner_policy_cert_req','mpartner-default-cert','mpolicy-default-cert',now(),'mpolicy-default-cert','approved','admin',now(),'admin',now(),NULL,NULL);

INSERT INTO pms.partner_policy (policy_api_key,part_id,policy_id,valid_from_datetime,valid_to_datetime,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,"label") 
VALUES('mpolicy_part_cert_api','mpartner-default-cert','mpolicy-default-cert',now(),now()+interval '12 years',true,'admin',now(),'admin',now(),false,NULL,'mpolicy_part_cert_api');



TRUNCATE TABLE pms.reg_device_type cascade ;
\COPY pms.reg_device_type  FROM 'dml/pms-reg_device_type.csv' WITH (FORMAT CSV, HEADER);

TRUNCATE TABLE pms.reg_device_sub_type cascade ;
\COPY pms.reg_device_sub_type FROM 'dml/pms-reg_device_sub_type.csv' WITH (FORMAT CSV, HEADER);

TRUNCATE TABLE pms.partner_policy_credential_type cascade ;
\COPY pms.partner_policy_credential_type FROM 'dml/pms-partner_policy_credential_type.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.device_detail FROM 'dml/auth-device_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.secure_biometric_interface FROM 'dml/auth-secure_biometric_interface.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.secure_biometric_interface_h FROM 'dml/auth-secure_biometric_interface_h.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.device_detail_sbi FROM 'dml/auth-device_detail_sbi.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.ftp_chip_detail FROM 'dml/auth-ftp_chip_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.device_detail FROM 'dml/reg-device_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.secure_biometric_interface FROM 'dml/reg-secure_biometric_interface.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.secure_biometric_interface_h FROM 'dml/reg-secure_biometric_interface_h.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.device_detail_sbi FROM 'dml/reg-device_detail_sbi.csv' WITH (FORMAT CSV, HEADER);

\COPY pms.ftp_chip_detail FROM 'dml/reg-ftp_chip_detail.csv' WITH (FORMAT CSV, HEADER);

ALTER TABLE pms.ftp_chip_detail ADD COLUMN approval_status character varying(36);
UPDATE pms.ftp_chip_detail SET approval_status='pending_cert_upload' where certificate_alias is null;
UPDATE pms.ftp_chip_detail SET approval_status='pending_approval' where certificate_alias is not null and is_active =false;
UPDATE pms.ftp_chip_detail SET approval_status='approved' where certificate_alias is not null and is_active = true;
ALTER TABLE pms.ftp_chip_detail ALTER COLUMN approval_status SET NOT NULL;

