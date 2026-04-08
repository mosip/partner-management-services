CREATE TABLE pms.partner_policy_credential_type_request (
    id character varying(36) NOT NULL,

    partner_policy_request_id character varying(36) NOT NULL,

    part_id character varying(36) NOT NULL,
    policy_id character varying(36) NOT NULL,

    credential_type character varying(128) NOT NULL,

    status_code character varying(20) NOT NULL DEFAULT 'InProgress',

    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,

    is_deleted boolean DEFAULT FALSE,
    del_dtimes timestamp,

    CONSTRAINT pk_ppctr_id PRIMARY KEY (id),

    CONSTRAINT fk_ppctr_request
        FOREIGN KEY (partner_policy_request_id)
        REFERENCES pms.partner_policy_request (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,

    CONSTRAINT fk_ppctr_part
        FOREIGN KEY (part_id)
        REFERENCES pms.partner (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,

    CONSTRAINT fk_ppctr_policy
        FOREIGN KEY (policy_id)
        REFERENCES pms.auth_policy (id)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,

    CONSTRAINT chk_ppctr_status
        CHECK (status_code IN ('InProgress', 'approved', 'rejected'))
);

-- Only one InProgress per (partner, credential_type)
CREATE UNIQUE INDEX uniq_partner_cred_inprogress
ON pms.partner_policy_credential_type_request (part_id, credential_type)
WHERE status_code = 'InProgress'
  AND is_deleted = FALSE;

-- Only one approved per (partner, credential_type)
CREATE UNIQUE INDEX uniq_partner_cred_approved
ON pms.partner_policy_credential_type_request (part_id, credential_type)
WHERE status_code = 'approved'
  AND is_deleted = FALSE;

