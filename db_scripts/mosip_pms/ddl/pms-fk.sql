ALTER TABLE pms.auth_policy ADD CONSTRAINT fk_apol_polg FOREIGN KEY (policy_group_id)
REFERENCES pms.policy_group (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner ADD CONSTRAINT fk_part_code FOREIGN KEY (partner_type_code)
REFERENCES pms.partner_type (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_contact ADD CONSTRAINT fk_partcnt_id FOREIGN KEY (partner_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy ADD CONSTRAINT fk_ppol_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy ADD CONSTRAINT fk_ppol_apol FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_request ADD CONSTRAINT fk_papr_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_bioextract ADD CONSTRAINT fk_ppbe_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_bioextract ADD CONSTRAINT fk_ppbe_pol FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_credential_type ADD CONSTRAINT fk_part_id FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.partner_policy_credential_type ADD CONSTRAINT fk_pol_id FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.device_detail ADD CONSTRAINT fk_devdtl_id FOREIGN KEY (dtype_code,dstype_code)
REFERENCES pms.reg_device_sub_type (dtyp_code,code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE pms.reg_device_sub_type ADD CONSTRAINT fk_rdstyp_dtype_code FOREIGN KEY (dtyp_code)
REFERENCES pms.reg_device_type (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

