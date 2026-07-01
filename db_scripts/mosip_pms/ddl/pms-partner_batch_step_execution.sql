--- This is required by Spring Batch framework

create table pms.batch_step_execution (
    step_execution_id bigint not null primary key,
    version bigint not null,
    step_name varchar(100) not null,
    job_execution_id bigint not null,
    create_time timestamp not null,
    start_time timestamp default null,
    end_time timestamp default null,
    status varchar(10),
    commit_count bigint,
    read_count bigint,
    filter_count bigint,
    write_count bigint,
    read_skip_count bigint,
    write_skip_count bigint,
    process_skip_count bigint,
    rollback_count bigint,
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    constraint job_exec_step_fk foreign key (job_execution_id)
    references pms.batch_job_execution(job_execution_id)
);
