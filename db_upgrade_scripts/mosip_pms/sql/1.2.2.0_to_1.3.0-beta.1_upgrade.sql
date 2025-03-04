\c mosip_pms

--- These tables are required by Spring Batch framework

create table pms.batch_job_instance (
    job_instance_id bigint not null primary key,
    version bigint,
    job_name varchar(100) not null,
    job_key varchar(32) not null,
    constraint job_inst_un unique (job_name, job_key)
);

create table pms.batch_job_execution (
    job_execution_id bigint not null primary key,
    version bigint,
    job_instance_id bigint not null,
    create_time timestamp not null,
    start_time timestamp default null,
    end_time timestamp default null,
    status varchar(10),
    exit_code varchar(2500),
    exit_message varchar(2500),
    last_updated timestamp,
    constraint job_inst_exec_fk foreign key (job_instance_id)
    references pms.batch_job_instance(job_instance_id)
);

create table pms.batch_job_execution_params (
    job_execution_id bigint not null,
    parameter_name varchar(100) not null,
    parameter_type varchar(100) not null,
    parameter_value varchar(2500),
    identifying char(1) not null,
    constraint job_exec_params_fk foreign key (job_execution_id)
    references pms.batch_job_execution(job_execution_id)
);

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

create table pms.batch_step_execution_context (
    step_execution_id bigint not null primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint step_exec_ctx_fk foreign key (step_execution_id)
    references pms.batch_step_execution(step_execution_id)
);

create table pms.batch_job_execution_context (
    job_execution_id bigint not null primary key,
    short_context varchar(2500) not null,
    serialized_context text,
    constraint job_exec_ctx_fk foreign key (job_execution_id)
    references batch_job_execution(job_execution_id)
);
