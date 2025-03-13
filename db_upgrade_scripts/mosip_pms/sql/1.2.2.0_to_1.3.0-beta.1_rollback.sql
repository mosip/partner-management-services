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

drop table if exists pms.notifications;

ALTER TABLE pms.user_details DROP COLUMN notifications_seen;
ALTER TABLE pms.user_details DROP COLUMN notifications_seen_dtimes;