--- This is required by Spring Batch framework

create table pms.batch_job_execution_context (
    job_execution_id bigint not null primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint job_exec_ctx_fk foreign key (job_execution_id)
    references batch_job_execution(job_execution_id)
);
