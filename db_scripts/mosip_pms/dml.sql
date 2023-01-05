\c mosip_pms 

TRUNCATE TABLE pms.partner_type cascade ;
\COPY pms.partner_type (code,partner_description,is_active,cr_by,cr_dtimes,is_policy_required) FROM './dml/pms-partner_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.policy_group cascade ;
\COPY pms.policy_group (id,name,descr,user_id,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-policy_group.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.auth_policy cascade ;
\COPY pms.auth_policy (id,policy_group_id,name,descr,policy_file_id,policy_type,version,policy_schema,valid_from_date,valid_to_date,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-auth_policy.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.auth_policy_h cascade ;
\COPY pms.auth_policy_h (id,eff_dtimes,policy_group_id,name,descr,policy_file_id,policy_type,version,policy_schema,valid_from_date,valid_to_date,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-auth_policy_h.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.partner cascade ;
\COPY pms.partner (id,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-partner.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.partner_h cascade ;
\COPY pms.partner_h (id,eff_dtimes,policy_group_id,name,address,contact_no,email_id,certificate_alias,user_id,partner_type_code,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-partner_h.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.partner_policy_request cascade ;
\COPY pms.partner_policy_request (id,part_id,policy_id,request_datetimes,request_detail,status_code,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-partner_policy_request.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.partner_policy_bioextract cascade ;
\COPY pms.partner_policy_bioextract (id,part_id,policy_id,attribute_name,extractor_provider,extractor_provider_version,biometric_modality,biometric_sub_types,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-partner_policy_bioextract.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.partner_policy cascade ;
\COPY pms.partner_policy (policy_api_key,label,part_id,policy_id,valid_from_datetime,valid_to_datetime,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-partner_policy.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.partner_policy_credential_type cascade ;
\COPY pms.partner_policy_credential_type (part_id,policy_id,credential_type,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) FROM './dml/pms-partner_policy_credential_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.reg_device_type cascade ;
\COPY pms.reg_device_type (code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/pms-reg_device_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.reg_device_sub_type cascade ;
\COPY pms.reg_device_sub_type (code,dtyp_code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/pms-reg_device_sub_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.device_detail cascade ;
\COPY pms.device_detail (id,dprovider_id,dtype_code,dstype_code,make,model,partner_org_name,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-device_detail.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.secure_biometric_interface cascade ;
\COPY pms.secure_biometric_interface (id,sw_binary_hash,sw_version,sw_cr_dtimes,sw_expiry_dtimes,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM './dml/pms-secure_biometric_interface.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pms.secure_biometric_interface_h cascade ;
\COPY pms.secure_biometric_interface_h (id,sw_binary_hash,sw_version,sw_cr_dtimes,sw_expiry_dtimes,approval_status,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,eff_dtimes) FROM './dml/pms-secure_biometric_interface_h.csv' delimiter ',' HEADER  csv;
