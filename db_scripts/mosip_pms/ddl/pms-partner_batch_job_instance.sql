--- This is required by Spring Batch framework

create table pms.batch_job_instance (
    job_instance_id bigint not null primary key,
    version bigint,
    job_name varchar(100) not null,
    job_key varchar(32) not null,
    constraint job_inst_un unique (job_name, job_key)
);
