--- This is required by Spring Batch framework

create table pms.batch_step_execution_context (
    step_execution_id bigint not null primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint step_exec_ctx_fk foreign key (step_execution_id)
    references pms.batch_step_execution(step_execution_id)
);
