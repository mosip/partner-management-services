--- This is required by Spring Batch framework

create sequence pms.batch_step_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence pms.batch_job_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence pms.batch_job_seq maxvalue 9223372036854775807 no cycle;