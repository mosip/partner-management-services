\c mosip_pms

REASSIGN OWNED BY sysadmin TO postgres;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA pms FROM pmsuser;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA pms FROM sysadmin;

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON ALL TABLES IN SCHEMA pms TO pmsuser;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA pms TO postgres;

-- object: pms.device_detail | type: TABLE --
-- DROP TABLE IF EXISTS pms.device_detail CASCADE;
CREATE TABLE pms.device_detail(
	id character varying(36) NOT NULL,
	dprovider_id character varying(36) NOT NULL,
	dtype_code character varying(36) NOT NULL,
	dstype_code character varying(36) NOT NULL,
	make character varying(36) NOT NULL,
	model character varying(36) NOT NULL,
	partner_org_name character varying(128),
	approval_status character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_devdtl_id PRIMARY KEY (id),
	CONSTRAINT uk_devdtl_id UNIQUE (dprovider_id,dtype_code,dstype_code,make,model)

);
-- ddl-end --
COMMENT ON TABLE pms.device_detail IS 'Device Detail : Details of the device like device provider id, make , model, device type, device sub type, approval status are stored here.';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.id IS 'ID: Unigue service ID, Service ID is geerated by the MOSIP system';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.dprovider_id IS 'Device Provider ID : Device provider ID, Referenced from master.device_provider.id';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.dtype_code IS 'Device Type Code: Code of the device type, Referenced from master.reg_device_type.code';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.dstype_code IS ' Device Sub Type Code: Code of the device sub type, Referenced from master.reg_device_sub_type.code';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.make IS 'Make: Make of the device';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.model IS ' Model: Model of the device';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.partner_org_name IS 'Partner Organisation Name';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.approval_status IS 'Approval Status';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.device_detail.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.device_detail TO pmsuser;

-- object: pms.device_detail | type: TABLE --
-- DROP TABLE IF EXISTS pms.device_detail_sbi CASCADE;
CREATE TABLE pms.device_detail_sbi(
	dprovider_id character varying(36) NOT NULL,
	partner_org_name character varying(128),
	device_detail_id character varying(36) NOT NULL,
	sbi_id character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp

);
-- ddl-end --

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.device_detail_sbi TO pmsuser;

-- object: pms.ftp_chip_detail | type: TABLE --
-- DROP TABLE IF EXISTS pms.ftp_chip_detail CASCADE;
CREATE TABLE pms.ftp_chip_detail(
	id character varying(36) NOT NULL,
	foundational_trust_provider_id character varying(36) NOT NULL,
	make character varying(36),
	model character varying(36),
	certificate_alias character varying(36),
	partner_org_name character varying(128),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_fcdtl_id PRIMARY KEY (id),
	CONSTRAINT uk_fcdtl_id UNIQUE (foundational_trust_provider_id,make,model)

);
-- ddl-end --
COMMENT ON TABLE pms.ftp_chip_detail IS 'Foundational Trust Provider Chip Details : To store all foundational trust provider chip details like make, model and certificate.';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.id IS 'Foundational Trust Provider ID: Unique ID of the trust provider, Trust provider id is generated by MOSIP system.';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.foundational_trust_provider_id IS 'Foundational Trust Provider ID: This is the partner id who provide chip and required certificates for L1 devices. This is soft referenced from Partner Management Service database.';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.make IS 'Chip Make: Make of the chip provided by the foundational trust provider';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.model IS 'Model : Model of the chip which is provided by the foundational trust provider';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.certificate_alias IS 'Certificate Alias : Its certificate alias which is stored in some key store and provided by MOSIP to a trust provider';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.partner_org_name IS 'Partner Organisation Name';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.ftp_chip_detail.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.ftp_chip_detail TO pmsuser;

-- object: pms.reg_device_sub_type | type: TABLE --
-- DROP TABLE IF EXISTS pms.reg_device_sub_type CASCADE;
CREATE TABLE pms.reg_device_sub_type(
	code character varying(36) NOT NULL,
	dtyp_code character varying(36) NOT NULL,
	name character varying(64) NOT NULL,
	descr character varying(512),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_rdstyp_code PRIMARY KEY (code,dtyp_code)

);
-- ddl-end --
COMMENT ON TABLE pms.reg_device_sub_type IS 'Device Type : Sub types of devices that are supported by the MOSIP system,  like  Slab, Single, Touchless...etc';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.code IS 'Device Sub Type Code: Sub types of devices used for registration processes, authentication...etc for ex., SLB, SINGLE, FULLFACE... etc';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.dtyp_code IS 'Device Type Code : Code of the device type where this sub type belongs to. refers to master.reg_device_type.code';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.name IS 'Device Name: Name of the device sub type';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.descr IS 'Device description: Device sub type description';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_sub_type.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.reg_device_sub_type TO pmsuser;

-- object: pms.reg_device_type | type: TABLE --
-- DROP TABLE IF EXISTS pms.reg_device_type CASCADE;
CREATE TABLE pms.reg_device_type(
	code character varying(36) NOT NULL,
	name character varying(64) NOT NULL,
	descr character varying(512),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_rdtyp_code PRIMARY KEY (code)

);
-- ddl-end --
COMMENT ON TABLE pms.reg_device_type IS 'Device Type : Types of devices that are supported by the MOSIP system,  like  scanning, finger, face, iris etc';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.code IS 'Device Type Code: Types of devices used for registration processes, authentication...etc for ex., FNR, FACE, IRIS... etc';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.name IS 'Device Name: Name of the device type';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.descr IS 'Device description: Device sub type description';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.reg_device_type.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.reg_device_type TO pmsuser;

-- object: pms.secure_biometric_interface | type: TABLE --
-- DROP TABLE IF EXISTS pms.secure_biometric_interface CASCADE;
CREATE TABLE pms.secure_biometric_interface(
	id character varying(36) NOT NULL,
	sw_binary_hash bytea NOT NULL,
	sw_version character varying(64) NOT NULL,
	sw_cr_dtimes timestamp,
	sw_expiry_dtimes timestamp,
	approval_status character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	provider_id character varying(36),
	partner_org_name character varying(128),
	CONSTRAINT pk_sbi_id PRIMARY KEY (id)
);
-- ddl-end --
COMMENT ON TABLE pms.secure_biometric_interface IS 'Secure Biometric Interface : Secure Biometric Interface to have all the details about the device types, provider and software details';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.id IS 'ID: Unigue service ID, Service ID is geerated by the MOSIP system';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.sw_binary_hash IS 'Software Binary Hash : Its is a software binary stored in MOSIP system for the devices';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.sw_version IS 'Software Version : Version of the stored software';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.sw_cr_dtimes IS 'Software Created Date Time: Date and Time on which this software is created';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.sw_expiry_dtimes IS 'Software Expiry Date Time: Expiry date and time of the device software';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.approval_status IS 'Approval Status:';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.secure_biometric_interface TO pmsuser;

-- object: pms.secure_biometric_interface_h | type: TABLE --
-- DROP TABLE IF EXISTS pms.secure_biometric_interface_h CASCADE;
CREATE TABLE pms.secure_biometric_interface_h(
	id character varying(36) NOT NULL,
	sw_binary_hash bytea NOT NULL,
	sw_version character varying(64) NOT NULL,
	sw_cr_dtimes timestamp,
	sw_expiry_dtimes timestamp,
	approval_status character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	eff_dtimes timestamp NOT NULL,
	provider_id character varying(36),
	partner_org_name character varying(128),
	CONSTRAINT pk_mdsh_id PRIMARY KEY (id,eff_dtimes)
);
-- ddl-end --
COMMENT ON TABLE pms.secure_biometric_interface_h IS 'MOSIP Secure Biometric Interface History : History of changes of any MOSIP secure biometric interface will be stored in history table to track any chnages for future validations.';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.id IS 'ID: Unigue service ID, Service ID is geerated by the MOSIP system';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.sw_binary_hash IS 'Software Binary Hash : Its is a software binary stored in MOSIP system for the devices';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.sw_version IS 'Software Version : Version of the stored software';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.sw_cr_dtimes IS 'Software Created Date Time: Date and Time on which this software is created';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.sw_expiry_dtimes IS 'Software Expiry Date Time: Expiry date and time of the device software';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.approval_status IS 'Approval Status';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
COMMENT ON COLUMN pms.secure_biometric_interface_h.eff_dtimes IS 'Effective Date Timestamp : This to track master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ).  The current record is effective from this date-time.';
-- ddl-end --

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON pms.secure_biometric_interface_h TO pmsuser;

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
VALUES('mpolicygroup-default-cert','mpolicygroup-default-cert','mpolicygroup-default-cert','superadmin',true,'superadmin',now(),'superadmin',now(),false,NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner (id,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,lang_code) 
VALUES('mpartner-default-cert','mpolicygroup-default-cert','mpartner-default-cert','mpartner-default-cert','9232121212','info@mosip.io',NULL,'mpartner-default-cert','Credential_Partner','approved',true,'superadmin',now(),'superadmin',now(),false,NULL,NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_h (id,eff_dtimes,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,lang_code) 
VALUES('mpartner-default-cert',now(),'mpolicygroup-default-cert','mpartner-default-cert','mpartner-default-cert','9232121212','info@mosip.io',NULL,'mpartner-default-cert','Credential_Partner','approved',true,'superadmin',now(),'superadmin',now(),false,NULL,NULL) ON CONFLICT (id, eff_dtimes) DO NOTHING;


INSERT INTO pms.auth_policy (id,policy_group_id,name,descr,policy_file_id,policy_type,"version",policy_schema,valid_from_date,valid_to_date,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpolicy-default-cert','mpolicygroup-default-cert','mpolicy-default-cert','mpolicy-default-cert','{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":true},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":true},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":true},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":true},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":true},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":true},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":true},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":true},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":true},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":true},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":true},{"attributeName":"individualBiometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics"}],"encrypted":true,"format":"extraction"}],"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"none","shareDomain":"datashare.datashare","source":"ID Repository"}}','DataShare','1','https://schemas.mosip.io/v1/auth-policy',now(),now()+interval '12 years',true,'admin',now(),'admin',now(),false,NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy_request (id,part_id,policy_id,request_datetimes,request_detail,status_code,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) 
VALUES('mpartner_policy_cert_req','mpartner-default-cert','mpolicy-default-cert',now(),'mpolicy-default-cert','approved','admin',now(),'admin',now(),NULL,NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy (policy_api_key,part_id,policy_id,valid_from_datetime,valid_to_datetime,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes,"label") 
VALUES('mpolicy_part_cert_api','mpartner-default-cert','mpolicy-default-cert',now(),now()+interval '12 years',true,'admin',now(),'admin',now(),false,NULL,'mpolicy_part_cert_api') ON CONFLICT (policy_api_key) DO NOTHING;



TRUNCATE TABLE pms.reg_device_type cascade ;

INSERT INTO pms.reg_device_type(code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Finger', 'Finger', 'Fingerprint Biometric', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code) DO NOTHING;
	
INSERT INTO pms.reg_device_type(code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Face', 'Face', 'Face Biometric', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code) DO NOTHING;
	
INSERT INTO pms.reg_device_type(code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Iris', 'Iris', 'Iris Biometric', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code) DO NOTHING;

TRUNCATE TABLE pms.reg_device_sub_type cascade ;

INSERT INTO pms.reg_device_sub_type(code, dtyp_code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Slap', 'Finger', 'Slap', 'Fingerprint Slap', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code, dtyp_code) DO NOTHING;

INSERT INTO pms.reg_device_sub_type(code, dtyp_code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Single', 'Finger', 'Single', 'Fingerprint Single Finger', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code, dtyp_code) DO NOTHING;

INSERT INTO pms.reg_device_sub_type(code, dtyp_code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Touchless', 'Finger', 'Touchless', 'Touch Fingerprint', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code, dtyp_code) DO NOTHING;

INSERT INTO pms.reg_device_sub_type(code, dtyp_code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Single', 'Iris', 'Single', 'Single Iris', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code, dtyp_code) DO NOTHING;

INSERT INTO pms.reg_device_sub_type(code, dtyp_code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Double', 'Iris', 'Double', 'Double Iris', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code, dtyp_code) DO NOTHING;

INSERT INTO pms.reg_device_sub_type(code, dtyp_code, name, descr, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('Full face', 'Face', 'Full face', 'Full face', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (code, dtyp_code) DO NOTHING;	


TRUNCATE TABLE pms.partner_policy_credential_type cascade ;

INSERT INTO pms.partner_policy_credential_type(part_id, policy_id, credential_type, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('mpartner-default-print', 'mpolicy-default-qrcode', 'qrcode', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (part_id, policy_id, credential_type) DO NOTHING;
	
INSERT INTO pms.partner_policy_credential_type(part_id, policy_id, credential_type, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('mpartner-default-print', 'mpolicy-default-euin', 'euin', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (part_id, policy_id, credential_type) DO NOTHING;
	
INSERT INTO pms.partner_policy_credential_type(part_id, policy_id, credential_type, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('mpartner-default-print', 'mpolicy-default-reprint', 'reprint', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (part_id, policy_id, credential_type) DO NOTHING;
	
INSERT INTO pms.partner_policy_credential_type(part_id, policy_id, credential_type, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
	VALUES ('mpartner-default-auth', 'mpolicy-default-auth', 'auth', true, 'superadmin', '2022-12-16 12:30:14.100', NULL, NULL, false, NULL) ON CONFLICT (part_id, policy_id, credential_type) DO NOTHING;

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

COMMENT ON COLUMN pms.ftp_chip_detail.approval_status IS 'approval_status : Status of the record. Status will be pending_cert_upload,pending_approval,approved or rejected';


INSERT INTO pms.partner
(id, policy_group_id, "name", address, contact_no, email_id, certificate_alias, user_id, partner_type_code, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', 'mpolicygroup-default-digitalcard', 'IITB', 'mpartner-default-digitalcard', '9232121212', 'digitalcard@mosip.io', '94d4ae61-31f0-42ca-97ae-8f4953f41fb6', 'mpartner-default-digitalcard', 'Credential_Partner', 'approved', true, 'superadmin', '2020-12-16 12:30:13.973', '110006', '2022-06-01 08:01:35.025', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_h
(id, eff_dtimes, policy_group_id, "name", address, contact_no, email_id, certificate_alias, user_id, partner_type_code, approval_status, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', '2020-12-16 12:30:14.306', 'mpolicygroup-default-digitalcard', 'IITB', 'mpartner-default-digitalcard', '9232121212', 'digitalcard@mosip.io', NULL, 'mpartner-default-resident', 'Credential_Partner', 'Activated', true, 'superadmin', '2020-12-16 12:30:14.306', 'superadmin', '2020-12-16 12:30:14.306', NULL, NULL) ON CONFLICT (id, eff_dtimes) DO NOTHING;

INSERT INTO pms.policy_group
(id, "name", descr, user_id, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicygroup-default-digitalcard', 'mpolicygroup-default-digitalcard', 'mpolicygroup-default-digitalcard', 'superadmin', true, 'superadmin', '2020-12-16 12:30:14.100', 'superadmin', '2020-12-16 12:30:14.100', NULL, NULL) ON CONFLICT (id) DO NOTHING;


INSERT INTO pms.auth_policy
(id, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-digitalcard', 'mpolicygroup-default-digitalcard', 'mpolicy-default-digitalcard', 'To Share Data', '{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"250","transactionsAllowed":"1000","encryptionType":"none","shareDomain":"datashare-service","source":"Print"},"shareableAttributes":[]}', 'Datashare', '1.0', 'https://schemas.mosip.io/v1/auth-policy', '2022-04-04 12:48:58.193', '2022-10-01 12:49:05.712', true, '110068', '2022-04-04 12:48:58.193', '110068', '2022-04-04 12:49:05.712', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.auth_policy
(id, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-PDFCard', 'mpolicygroup-default-digitalcard', 'mpolicy-default-PDFCard', 'mpolicy-default-PDFCard', '{"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"DD/MM/YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"PCN","source":[{"attribute":"VID","filter":[{"type":"PERPETUAL"}]}],"encrypted":false,"format":"RETRIEVE"},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"bestTwoFingers","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Finger"}]}],"encrypted":false,"format":"bestTwoFingers"},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":false,"format":"extraction"}]}', 'DataShare', '1', 'https://schemas.mosip.io/v1/auth-policy', '2022-09-20 09:00:54.316627', '2025-04-28 09:37:00', true, 'admin', '2022-09-20 09:00:54.316627', 'admin', '2022-09-20 09:00:54.316627', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.auth_policy_h
(id, eff_dtimes, policy_group_id, "name", descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-digitalcard', '2020-11-14 05:59:00.000', 'mpolicygroup-default-digitalcard', 'mpolicy-default-digitalcard', 'mpolicy-default-digitalcard', '{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"none","shareDomain":"datashare-service","source":"Print"},"shareableAttributes":[]}', 'DataShare', '1', 'https://schemas.mosip.io/v1/auth-policy', '2020-12-16 12:30:14.343', '2025-05-02 09:37:00.000', true, 'admin', '2020-12-16 12:30:14.343', 'admin', '2020-12-16 12:30:14.343', NULL, NULL) ON CONFLICT (id, eff_dtimes) DO NOTHING;


INSERT INTO pms.auth_policy_h
(id, eff_dtimes, policy_group_id, name, descr, policy_file_id, policy_type, "version", policy_schema, valid_from_date, valid_to_date, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpolicy-default-PDFCard', '2020-11-13 05:58:00.000', 'mpolicygroup-default-digitalcard', 'mpolicy-default-PDFCard', 'mpolicy-default-PDFCard','{"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"encrypted":false,"format":"RETRIEVE","attributeName":"VID","source":[{"filter":[{"type":"PERPETUAL"}],"attribute":"VID"}]},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":false,"format":"extraction"}]}', 'DataShare', '1', 'https://schemas.mosip.io/v1/auth-policy', '2020-12-16 12:30:14.343', '2025-05-01 09:37:00.000', true, 'admin', '2020-12-16 12:30:14.343', 'admin', '2020-12-16 12:30:14.343', NULL, NULL) ON CONFLICT (id, eff_dtimes) DO NOTHING;

INSERT INTO pms.partner_policy
(policy_api_key, part_id, policy_id, valid_from_datetime, valid_to_datetime, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes,label)
VALUES('mpolicy_part_digitalcard_api', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-04-04 13:21:20.172', '2022-07-03 13:21:20.172', true, 'service-account-mosip-regproc-client', '2022-04-04 13:21:20.172', 'admin', '2022-02-21 07:02:26.223', false, NULL,'mpolicy_part_PDFCard_api') ON CONFLICT (policy_api_key) DO NOTHING;

INSERT INTO pms.partner_policy
(policy_api_key, part_id, policy_id, valid_from_datetime, valid_to_datetime, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes,label)
VALUES('mpolicy_part_PDFCard_api', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-02-21 07:02:26.223', '2025-12-01 05:31:00.000', true, 'admin', '2022-02-21 07:02:26.223', 'admin', '2022-02-21 07:02:26.223', false, NULL,'mpolicy_part_PDFCard_api') ON CONFLICT (policy_api_key) DO NOTHING;


INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146210', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'photo', 'mock', '1.1', 'face', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146211', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'iris', 'mock', '1.1', 'iris', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy_bioextract
(id, part_id, policy_id, attribute_name, extractor_provider, extractor_provider_version, biometric_modality, biometric_sub_types, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('146212', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'fingerprint', 'mock', '1.1', 'finger', NULL, 'admin', '2022-02-21 07:02:26.256', 'admin', '2022-02-21 07:02:26.256', false, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy_credential_type
(part_id, policy_id, credential_type, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner-default-digitalcard', 'mpolicy-default-PDFCard', 'PDFCard', true, 'service-account-mosip-regproc-client', '2022-04-04 13:29:10.383', NULL, NULL, false, NULL) ON CONFLICT (part_id, policy_id, credential_type) DO NOTHING;

INSERT INTO pms.partner_policy_request
(id, part_id, policy_id, request_datetimes, request_detail, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner_policy_PDFCard_req', 'mpartner-default-digitalcard', 'mpolicy-default-PDFCard', '2022-02-21 07:02:26.292', 'mpolicy-default-PDFCard', 'approved', 'admin', '2022-02-21 07:02:26.292', 'admin', '2022-02-21 07:02:26.292', NULL, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_policy_request
(id, part_id, policy_id, request_datetimes, request_detail, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)
VALUES('mpartner_policy_digitalcard_req', 'mpartner-default-digitalcard', 'mpolicy-default-digitalcard', '2022-02-21 07:02:26.292', 'mpolicy-default-digitalcard', 'approved', 'admin', '2022-02-21 07:02:26.292', 'admin', '2022-02-21 07:02:26.292', NULL, NULL) ON CONFLICT (id) DO NOTHING;

INSERT INTO pms.partner_type (code,partner_description,is_policy_required,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) VALUES
	 ('Internal_Partner','Used internally to share certs',true,true,'superadmin',now(),NULL,NULL,false,NULL) ON CONFLICT (code) DO NOTHING;

INSERT INTO pms.partner_type (code,partner_description,is_policy_required,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) VALUES
	 ('Print_Partner','Print Partner',true,true,'superadmin',now(),NULL,NULL,false,NULL) ON CONFLICT (code) DO NOTHING;

INSERT INTO pms.partner_type (code,partner_description,is_policy_required,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) VALUES
	 ('SDK_Partner','SDK Partner',true,true,'superadmin',now(),NULL,NULL,false,NULL) ON CONFLICT (code) DO NOTHING;

-- ddl-end --