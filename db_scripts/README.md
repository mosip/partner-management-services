# PMS Database

## Introduction
This folder containers various SQL scripts to create database and tables in postgres.  These scripts are automatically run with [DB init](https://github.com/mosip/mosip-infra/blob/1.2.0-rc2/deployment/v3/external/postgres/cluster/init_db.sh) of sandbox deployment.

Default data that's populated in the tables is present under [`dml`](mosip_prereg/dml) folder

## Database
The module uses `mosip_pms` database with following tables:

* [partner_type](mosip_pms/ddl/pms-partner_type.sql)
* [policy_group](mosip_pms/ddl/pms-policy_group.sql)
* [auth_policy](mosip_pms/ddl/pms-auth_policy.sql)
* [auth_policy_h](mosip_pms/ddl/pms-auth_policy_h.sql)
* [partner](mosip_pms/ddl/pms-partner.sql)
* [partner_h](mosip_pms/ddl/pms-partner_h.sql)
* [partner_contact](mosip_pms/ddl/pms-partner_contact.sql)
* [partner_policy_request](mosip_pms/ddl/pms-partner_policy_request.sql)
* [partner_policy_credential_type](mosip_pms/ddl/pms-partner_policy_credential_type.sql)
* [partner_policy_bioextract](mosip_pms/ddl/pms-partner_policy_bioextract.sql)
* [partner_policy](mosip_pms/ddl/pms-partner_policy.sql)
* [misp_license](mosip_pms/ddl/pms-misp_license.sql)
* [reg_device_type](mosip_pms/ddl/pms-reg_device_type.sql)
* [reg_device_sub_type](mosip_pms/ddl/pms-reg_device_sub_type.sql)
* [device_detail](mosip_pms/ddl/pms-device_detail.sql)
* [secure_biometric_interface](mosip_pms/ddl/pms-secure_biometric_interface.sql)
* [secure_biometric_interface_h](mosip_pms/ddl/pms-secure_biometric_interface_h.sql)
* [device_detail_sbi](mosip_pms/ddl/pms-device_detail_sbi.sql)
* [ftp_chip_detail](mosip_pms/ddl/pms-ftp_chip_detail.sql)
