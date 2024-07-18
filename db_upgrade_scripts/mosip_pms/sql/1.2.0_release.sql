-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.0
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- 15-07-2022           Nagarjuna K          Added dml insert scripts 
-- 15-07-2022           Nagarjuna K          Merged 1.2.0.1 changes to 1.2.0 release patch
-- -------------------------------------------------------------------------------------------------

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

INSERT INTO pms.partner
(id, policy_group_id, "name", address, contact_no, email_id, certificate_alias, user_id, partner_type_code, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', 'mpolicygroup-default-digitalcard', 'IITB', 'mpartner-default-digitalcard', '9232121212', 'digitalcard@mosip.io', '94d4ae61-31f0-42ca-97ae-8f4953f41fb6', 'mpartner-default-digitalcard', 'Credential_Partner', 'approved', true, 'superadmin', '2020-12-16 12:30:13.973', '110006', '2022-06-01 08:01:35.025', false, NULL);

INSERT INTO pms.partner_h
(id, eff_dtimes, policy_group_id, "name", address, contact_no, email_id, certificate_alias, user_id, partner_type_code, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', '2020-12-16 12:30:14.306', 'mpolicygroup-default-digitalcard', 'IITB', 'mpartner-default-digitalcard', '9232121212', 'digitalcard@mosip.io', NULL, 'mpartner-default-resident', 'Credential_Partner', 'Activated', true, 'superadmin', '2020-12-16 12:30:14.306', 'superadmin', '2020-12-16 12:30:14.306', NULL, NULL);

INSERT INTO pms.policy_group
(id, "name", descr, user_id, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicygroup-default-digitalcard', 'mpolicygroup-default-digitalcard', 'mpolicygroup-default-digitalcard', 'superadmin', true, 'superadmin', '2020-12-16 12:30:14.100', 'superadmin', '2020-12-16 12:30:14.100', NULL, NULL);


INSERT INTO pms.auth_policy
(id, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-digitalcard', 'mpolicygroup-default-digitalcard', 'mpolicy-default-digitalcard', 'To Share Data', '{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"250","transactionsAllowed":"1000","encryptionType":"none","shareDomain":"datashare-service","source":"Print"},"shareableAttributes":[]}', 'Datashare', '1.0', 'https://schemas.mosip.io/v1/auth-policy', '2022-04-04 12:48:58.193', '2022-10-01 12:49:05.712', true, '110068', '2022-04-04 12:48:58.193', '110068', '2022-04-04 12:49:05.712', false, NULL);

INSERT INTO pms.auth_policy
(id, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-PDFCard', 'mpolicygroup-default-digitalcard', 'string', 'string', '{"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"encrypted":false,"format":"RETRIEVE","attributeName":"VID","source":[{"filter":[{"type":"PERPETUAL"}],"attribute":"VID"}]},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":false,"format":"extraction"}]}', 'DataShare', 'string', 'https://schemas.mosip.io/v1/auth-policy', '2020-12-16 12:30:14.183', '2025-04-28 09:37:00.000', true, 'admin', '2020-12-16 12:30:14.183', 'service-account-mosip-creser-client', '2021-02-09 06:50:22.065', false, NULL);

INSERT INTO pms.auth_policy_h
(id, eff_dtimes, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-digitalcard', '2020-11-14 05:59:00.000', 'mpolicygroup-default-digitalcard', 'mpolicy-default-digitalcard', 'mpolicy-default-digitalcard', '{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"none","shareDomain":"datashare-service","source":"Print"},"shareableAttributes":[]}', 'DataShare', '1', 'https://schemas.mosip.io/v1/auth-policy', '2020-12-16 12:30:14.343', '2025-05-02 09:37:00.000', true, 'admin', '2020-12-16 12:30:14.343', 'admin', '2020-12-16 12:30:14.343', NULL, NULL);


INSERT INTO pms.auth_policy_h
(id, eff_dtimes, policy_group_id, name, descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-PDFCard', '2020-11-13 05:58:00.000', 'mpolicygroup-default-digitalcard', 'mpolicy-default-PDFCard', 'mpolicy-default-PDFCard','{"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"encrypted":false,"format":"RETRIEVE","attributeName":"VID","source":[{"filter":[{"type":"PERPETUAL"}],"attribute":"VID"}]},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":false,"format":"extraction"}]}', 'DataShare', '1', 'https://schemas.mosip.io/v1/auth-policy', '2020-12-16 12:30:14.343', '2025-05-01 09:37:00.000', true, 'admin', '2020-12-16 12:30:14.343', 'admin', '2020-12-16 12:30:14.343', NULL, NULL);

INSERT INTO pms.partner_policy
(policy_api_key, part_id, policy_id, valid_from_datetime, valid_to_datetime, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes,label)
VALUES('mpolicy_part_digitalcard_api', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-04-04 13:21:20.172', '2022-07-03 13:21:20.172', true, 'service-account-mosip-regproc-client', '2022-04-04 13:21:20.172', 'admin', '2022-02-21 07:02:26.223', false, NULL,'mpolicy_part_PDFCard_api');

INSERT INTO pms.partner_policy
(policy_api_key, part_id, policy_id, valid_from_datetime, valid_to_datetime, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes,label)
VALUES('mpolicy_part_PDFCard_api', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-02-21 07:02:26.223', '2025-12-01 05:31:00.000', true, 'admin', '2022-02-21 07:02:26.223', 'admin', '2022-02-21 07:02:26.223', false, NULL,'mpolicy_part_PDFCard_api');


INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146210', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'photo', 'mock', '1.1', 'face', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL);
INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146211', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'iris', 'mock', '1.1', 'iris', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL);
INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146212', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'fingerprint', 'mock', '1.1', 'finger', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL);

INSERT INTO pms.partner_policy_credential_type
(part_id, policy_id, credential_type, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'PDFCard', true, 'service-account-mosip-regproc-client', '2022-04-04 13:29:10.383', NULL, NULL, false, NULL);

INSERT INTO pms.partner_policy_request
(id, part_id, policy_id, request_datetimes, request_detail, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner_policy_PDFCard_req', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', '2022-02-21 07:02:26.292', 'mpolicy-default-PDFCard', 'approved', 'admin', '2022-02-21 07:02:26.292', 'admin', '2022-02-21 07:02:26.292', NULL, NULL);

INSERT INTO pms.partner_policy_request
(id, part_id, policy_id, request_datetimes, request_detail, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner_policy_digitalcard_req', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-02-21 07:02:26.292', 'mpolicy-default-digitalcard', 'approved', 'admin', '2022-02-21 07:02:26.292', 'admin', '2022-02-21 07:02:26.292', NULL, NULL);

UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-qrcode';

UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-reprint';
