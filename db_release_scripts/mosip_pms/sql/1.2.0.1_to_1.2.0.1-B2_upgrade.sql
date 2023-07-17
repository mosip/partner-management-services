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
	 ('Print_Partner','Print Partner',true,true,'superadmin',now(),NULL,NULL,false,NULL) ON CONFLICT (code) DO NOTHING; 

ALTER TABLE pms.misp_license ADD COLUMN policy_id character varying(36);
	 
\ir ../ddl/pms-oidc_client.sql

INSERT INTO pms.partner
(id, policy_group_id, "name", address, contact_no, email_id, certificate_alias, user_id, partner_type_code, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', 'mpolicygroup-default-digitalcard', 'IITB', 'mpartner-default-digitalcard', '9232121212', 'digitalcard@mosip.io', '94d4ae61-31f0-42ca-97ae-8f4953f41fb6', 'mpartner-default-digitalcard', 'Credential_Partner', 'approved', true, 'superadmin', '2020-12-16 12:30:13.973', '110006', '2022-06-01 08:01:35.025', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_h
(id, eff_dtimes, policy_group_id, "name", address, contact_no, email_id, certificate_alias, user_id, partner_type_code, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', '2020-12-16 12:30:14.306', 'mpolicygroup-deafult-digitalcard', 'mpartner-default-digitalcard', 'mpartner-default-digitalcard', '9232121212', 'digitalcard@mosip.io', NULL, 'mpartner-default-resident', 'Credential_Partner', 'Activated', true, 'superadmin', '2020-12-16 12:30:14.306', 'superadmin', '2020-12-16 12:30:14.306', NULL, NULL) ON CONFLICT (id, eff_dtimes) DO NOTHING;

INSERT INTO pms.policy_group
(id, "name", descr, user_id, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicygroup-deafult-digitalcard', 'mpolicygroup-deafult-digitalcard', 'mpolicygroup-deafult-digitalcard', 'superadmin', true, 'superadmin', '2020-12-16 12:30:14.100', 'superadmin', '2020-12-16 12:30:14.100', NULL, NULL) ON CONFLICT (id) DO NOTHING;


INSERT INTO pms.auth_policy
(id, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-digitalcard', 'mpolicygroup-deafult-digitalcard', 'mpolicy-default-digitalcard', 'To Share Data', '{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"250","transactionsAllowed":"1000","encryptionType":"none","shareDomain":"datashare.datashare","source":"Print"},"shareableAttributes":[]}', 'Datashare', '1.0', 'https://schemas.mosip.io/v1/auth-policy', '2022-04-04 12:48:58.193', '2022-10-01 12:49:05.712', true, '110068', '2022-04-04 12:48:58.193', '110068', '2022-04-04 12:49:05.712', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.auth_policy
(id, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-PDFCard', 'mpolicygroup-deafult-digitalcard', 'string', 'string', '{"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"DD/MM/YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"PCN","source":[{"attribute":"VID","filter":[{"type":"PERPETUAL"}]}],"encrypted":false,"format":"RETRIEVE"},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"bestTwoFingers","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Finger"}]}],"encrypted":false,"format":"bestTwoFingers"},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":false,"format":"extraction"}]}', 'DataShare', 'string', 'https://schemas.mosip.io/v1/auth-policy', NOW(), NOW() + INTERVAL '5 year', true, 'admin', NOW(), 'service-account-mosip-creser-client', NULL, false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.auth_policy_h
(id, eff_dtimes, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-digitalcard', '2020-11-14 05:59:00.000', 'mpolicygroup-deafult-digitalcard', 'mpolicy-default-digitalcard', 'mpolicy-default-digitalcard', '{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"none","shareDomain":"datashare.datashare","source":"Print"},"shareableAttributes":[]}', 'DataShare', '1', 'https://schemas.mosip.io/v1/auth-policy', '2020-12-16 12:30:14.343', '2025-05-02 09:37:00.000', true, 'admin', '2020-12-16 12:30:14.343', 'admin', '2020-12-16 12:30:14.343', NULL, NULL) ON CONFLICT (id, eff_dtimes) DO NOTHING;


INSERT INTO pms.auth_policy_h
(id, eff_dtimes, policy_group_id, name, descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-PDFCard', '2020-11-13 05:58:00.000', 'mpolicygroup-deafult-digitalcard', 'mpolicy-default-PDFCard', 'mpolicy-default-PDFCard', '{"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}', 'DataShare', '1', 'https://schemas.mosip.io/v1/auth-policy', '2020-12-16 12:30:14.343', '2025-05-01 09:37:00.000', true, 'admin', '2020-12-16 12:30:14.343', 'admin', '2020-12-16 12:30:14.343', NULL, NULL) ON CONFLICT (id, eff_dtimes) DO NOTHING;

INSERT INTO pms.partner_policy
(policy_api_key, part_id, policy_id, valid_from_datetime, valid_to_datetime, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy_part_digitalcard_api', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-04-04 13:21:20.172', '2022-07-03 13:21:20.172', true, 'service-account-mosip-regproc-client', '2022-04-04 13:21:20.172', NULL, NULL, false, NULL) ON CONFLICT (policy_api_key) DO NOTHING;

INSERT INTO pms.partner_policy
(policy_api_key, part_id, policy_id, valid_from_datetime, valid_to_datetime, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy_part_PDFCard_api', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-02-21 07:02:26.223', '2025-12-01 05:31:00.000', true, 'admin', '2022-02-21 07:02:26.223', 'admin', '2022-02-21 07:02:26.223', false, NULL) ON CONFLICT (policy_api_key) DO NOTHING;


INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146110', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'photo', 'mock', '1.1', 'face', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL) ON CONFLICT (id) DO NOTHING;
INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146111', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'iris', 'mock', '1.1', 'iris', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL) ON CONFLICT (id) DO NOTHING;
INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146112', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'fingerprint', 'mock', '1.1', 'finger', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy_credential_type
(part_id, policy_id, credential_type, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'PDFCard', true, 'service-account-mosip-regproc-client', '2022-04-04 13:29:10.383', NULL, NULL, false, NULL) ON CONFLICT (part_id,policy_id,credential_type) DO NOTHING;

INSERT INTO pms.partner_policy_request
(id, part_id, policy_id, request_datetimes, request_detail, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner_policy_PDFCard_req', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', '2022-02-21 07:02:26.292', 'mpolicy-default-PDFCard', 'approved', 'admin', '2022-02-21 07:02:26.292', 'admin', '2022-02-21 07:02:26.292', NULL, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy_request
(id, part_id, policy_id, request_datetimes, request_detail, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner_policy_digitalcard_req', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-02-21 07:02:26.292', 'mpolicy-default-digitalcard', 'approved', 'admin', '2022-02-21 07:02:26.292', 'admin', '2022-02-21 07:02:26.292', NULL, NULL) ON CONFLICT (id) DO NOTHING;
