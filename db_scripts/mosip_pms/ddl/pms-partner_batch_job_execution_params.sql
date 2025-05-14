--- This is required by Spring Batch framework

create table pms.batch_job_execution_params (
    job_execution_id bigint not null,
    parameter_name varchar(100) not null,
    parameter_type varchar(100) not null,
    parameter_value varchar(2500),
    identifying char(1) not null,
    constraint job_exec_params_fk foreign key (job_execution_id)
    references pms.batch_job_execution(job_execution_id)
);