\c mosip_pms

alter table pms.batch_job_execution drop constraint job_inst_exec_fk;
alter table pms.batch_job_execution_params drop constraint job_exec_params_fk;
alter table pms.batch_step_execution drop constraint job_exec_step_fk;
alter table pms.batch_step_execution_context drop constraint step_exec_ctx_fk;
alter table pms.batch_job_execution_context drop constraint job_exec_ctx_fk;

drop table if exists pms.batch_job_execution_context;
drop table if exists pms.batch_step_execution_context;
drop table if exists pms.batch_step_execution;
drop table if exists pms.batch_job_execution_params;
drop table if exists pms.batch_job_execution;
drop table if exists pms.batch_job_instance;

drop sequence if exists pms.batch_step_execution_seq;
drop sequence if exists pms.batch_job_execution_seq;
drop sequence if exists pms.batch_job_seq;

drop table if exists pms.notifications;

ALTER TABLE pms.user_details DROP COLUMN notifications_seen;
ALTER TABLE pms.user_details DROP COLUMN notifications_seen_dtimes;

UPDATE pms.auth_policy_h SET policy_file_id = '{""shareableAttributes"":[{""attributeName"":""biometrics"",""group"":""CBEFF"",""source"":[{""attribute"":""registration-client\/NEW\/individualBiometrics"",""filter"":[{""type"":""Iris""}]},{""attribute"":""CNIE\/verification\/biometrics"",""filter"":[{""type"":""Finger""}]},{""attribute"":""CNIE\/verification\/biometrics"",""filter"":[{""type"":""Face""}]}],""encrypted"":true,""format"":""extraction""}],""dataSharePolicies"":{""typeOfShare"":""Data Share"",""validForInMinutes"":""30"",""transactionsAllowed"":""2"",""encryptionType"":""Partner Based"",""shareDomain"":""datashare.datashare"",""source"":""Packet Manager""}}' where id ='mpolicy-default-abis';

UPDATE pms.auth_policy_h SET policy_file_id ='{""dataSharePolicies"":{""typeOfShare"":""Data Share"",""validForInMinutes"":""30"",""transactionsAllowed"":""2"",""encryptionType"":""Partner Based"",""shareDomain"":""datashare.datashare"",""source"":""ID Repository""},""shareableAttributes"":[{""attributeName"":""fullName"",""source"":[{""attribute"":""fullName"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""dateOfBirth"",""source"":[{""attribute"":""dateOfBirth""}],""encrypted"":false,""format"":""YYYY""},{""attributeName"":""gender"",""source"":[{""attribute"":""gender"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""phone"",""source"":[{""attribute"":""phone""}],""encrypted"":false},{""attributeName"":""email"",""source"":[{""attribute"":""email""}],""encrypted"":false},{""attributeName"":""addressLine1"",""source"":[{""attribute"":""addressLine1"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""addressLine2"",""source"":[{""attribute"":""addressLine2"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""addressLine3"",""source"":[{""attribute"":""addressLine3"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""region"",""source"":[{""attribute"":""region"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""province"",""source"":[{""attribute"":""province"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""city"",""source"":[{""attribute"":""city"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""UIN"",""source"":[{""attribute"":""UIN""}],""encrypted"":false},{""attributeName"":""postalCode"",""source"":[{""attribute"":""postalCode""}],""encrypted"":false},{""attributeName"":""biometrics"",""group"":""CBEFF"",""source"":[{""attribute"":""individualBiometrics"",""filter"":[{""type"":""Face""},{""type"":""Finger"",""subType"":[""Left Thumb"",""Right Thumb""]}]}],""encrypted"":true,""format"":""extraction""}]}' where id ='mpolicy-default-reprint';
