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

create sequence pms.batch_step_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence pms.batch_job_execution_seq maxvalue 9223372036854775807 no cycle;
create sequence pms.batch_job_seq maxvalue 9223372036854775807 no cycle;

grant usage, select on all sequences in schema pms to pmsuser;

-- This table stores notifications for root, intermediate, and partner certificate expiry, as well as SBI expiry and API key expiry.
CREATE TABLE pms.notifications
(
    id character varying(128) NOT NULL,
    partner_id character varying(128) NOT NULL,
    notification_type character varying(36) NOT NULL,
    notification_status character varying(36) NOT NULL,
	notification_details_json character varying(4000) NOT NULL,
    email_id character varying(3000) NOT NULL,
    email_lang_code character varying(36) NOT NULL,
    email_sent boolean DEFAULT FALSE,
    email_sent_dtimes timestamp,
    cr_by character varying(128) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(128),
    upd_dtimes timestamp,
    CONSTRAINT notifications_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE pms.notifications IS 'This table stores notifications along with their details.';
COMMENT ON COLUMN pms.notifications.id IS 'ID: A unique identifier for the notification.';
COMMENT ON COLUMN pms.notifications.partner_id IS 'Partner ID: A unique identifier for the partner.';
COMMENT ON COLUMN pms.notifications.notification_type IS 'Notification Type: The type of notification generated. Examples include PARTNER_CERT_EXPIRY, ROOT_CERT_EXPIRY, and SBI_EXPIRY.';
COMMENT ON COLUMN pms.notifications.notification_status IS 'Notification Status: The current status of the notification. Possible values include ACTIVE and DISMISSED.';
COMMENT ON COLUMN pms.notifications.notification_details_json IS 'Notification Details (JSON): Detailed information about the notification in JSON format.';
COMMENT ON COLUMN pms.notifications.email_id IS 'Email ID: The email address of the partner to whom the notification is sent.';
COMMENT ON COLUMN pms.notifications.email_lang_code IS 'Email Language Code: The language code used for the email.';
COMMENT ON COLUMN pms.notifications.email_sent IS 'Email Sent: Indicates whether the email has been sent (TRUE) or not (FALSE).';
COMMENT ON COLUMN pms.notifications.email_sent_dtimes IS 'Email Sent Timestamp: The date and time when the email was sent.';
COMMENT ON COLUMN pms.notifications.cr_dtimes IS 'Created Timestamp: The date and time when the record was created.';
COMMENT ON COLUMN pms.notifications.cr_by IS 'Created By: The ID or name of the user who created the record.';
COMMENT ON COLUMN pms.notifications.upd_by IS 'Updated By: The ID or name of the user who last updated the record.';
COMMENT ON COLUMN pms.notifications.upd_dtimes IS 'Updated Timestamp: The date and time when any field in the record was last updated.';

-- add new columns in user_details table
ALTER TABLE pms.user_details Add COLUMN notifications_seen_dtimes timestamp;
COMMENT ON COLUMN pms.user_details.notifications_seen_dtimes IS 'Notifications Seen Timestamp: The date and time when the notifications was seen.';

UPDATE pms.auth_policy_h SET policy_file_id = '{""shareableAttributes"":[{""attributeName"":""biometrics"",""group"":""CBEFF"",""source"":[{""attribute"":""registration-client\/NEW\/individualBiometrics"",""filter"":[{""type"":""Iris""}]},{""attribute"":""CNIE\/verification\/biometrics"",""filter"":[{""type"":""Finger""}]}],""encrypted"":true,""format"":""extraction""}],""dataSharePolicies"":{""typeOfShare"":""Data Share"",""validForInMinutes"":""30"",""transactionsAllowed"":""2"",""encryptionType"":""Partner Based"",""shareDomain"":""datashare.datashare"",""source"":""Packet Manager""}}' where id ='mpolicy-default-abis';

UPDATE pms.auth_policy_h SET policy_file_id ='{""dataSharePolicies"":{""typeOfShare"":""Data Share"",""validForInMinutes"":""30"",""transactionsAllowed"":""2"",""encryptionType"":""Partner Based"",""shareDomain"":""datashare.datashare"",""source"":""ID Repository""},""shareableAttributes"":[{""attributeName"":""fullName"",""source"":[{""attribute"":""fullName"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""dateOfBirth"",""source"":[{""attribute"":""dateOfBirth""}],""encrypted"":false,""format"":""YYYY""},{""attributeName"":""gender"",""source"":[{""attribute"":""gender"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""phone"",""source"":[{""attribute"":""phone""}],""encrypted"":false},{""attributeName"":""email"",""source"":[{""attribute"":""email""}],""encrypted"":false},{""attributeName"":""addressLine1"",""source"":[{""attribute"":""addressLine1"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""addressLine2"",""source"":[{""attribute"":""addressLine2"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""addressLine3"",""source"":[{""attribute"":""addressLine3"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""region"",""source"":[{""attribute"":""region"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""province"",""source"":[{""attribute"":""province"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""city"",""source"":[{""attribute"":""city"",""filter"":[{""language"":""eng""}]}],""encrypted"":false},{""attributeName"":""UIN"",""source"":[{""attribute"":""UIN""}],""encrypted"":false},{""attributeName"":""postalCode"",""source"":[{""attribute"":""postalCode""}],""encrypted"":false},{""attributeName"":""biometrics"",""group"":""CBEFF"",""source"":[{""attribute"":""individualBiometrics"",""filter"":[{""type"":""Face""}]}],""encrypted"":true,""format"":""extraction""}]}' where id ='mpolicy-default-reprint';

UPDATE pms.auth_policy
SET policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-qrcode';

UPDATE pms.auth_policy
SET  policy_file_id='{"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"},"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city","filter":[{"language":"eng"}]}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"},{"type":"Finger","subType":["Left Thumb","Right Thumb"]}]}],"encrypted":true,"format":"extraction"}]}'
WHERE id='mpolicy-default-euin';

-- Add new column for email hash
ALTER TABLE pms.partner
ADD COLUMN email_id_hash VARCHAR(3000);

-- Update column sizes in pms.partner
ALTER TABLE pms.partner
    ALTER COLUMN contact_no TYPE character varying(1000),
    ALTER COLUMN email_id TYPE character varying(3000),
    ALTER COLUMN address TYPE character varying(10000);

-- Update column sizes in pms.partner_h
ALTER TABLE pms.partner_h
    ALTER COLUMN contact_no TYPE character varying(1000),
    ALTER COLUMN email_id TYPE character varying(3000),
    ALTER COLUMN address TYPE character varying(10000);

-- Update column sizes in pms.partner_contact
ALTER TABLE pms.partner_contact
    ALTER COLUMN contact_no TYPE character varying(1000),
    ALTER COLUMN email_id TYPE character varying(3000),
    ALTER COLUMN address TYPE character varying(10000)
    ADD COLUMN email_id_hash character varying(3000);
