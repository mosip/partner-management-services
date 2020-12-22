-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pmp
-- Table Name : 
-- Purpose    : All the FKs are created separately, not part of create table scripts to ease the deployment process
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Aug-2020             Sadanandegowda DM   Update for the partner managment restructure
-- Dec-2020             Sadanandegowda DM   Update for the partner_policy_credential_type
-- ------------------------------------------------------------------------------------------

-- object: fk_apol_polg | type: CONSTRAINT --
-- ALTER TABLE pms.auth_policy DROP CONSTRAINT IF EXISTS fk_apol_polg CASCADE;
ALTER TABLE pms.auth_policy ADD CONSTRAINT fk_apol_polg FOREIGN KEY (policy_group_id)
REFERENCES pms.policy_group (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --


-- object: fk_part_code | type: CONSTRAINT --
-- ALTER TABLE pms.partner DROP CONSTRAINT IF EXISTS fk_part_code CASCADE;
ALTER TABLE pms.partner ADD CONSTRAINT fk_part_code FOREIGN KEY (partner_type_code)
REFERENCES pms.partner_type (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_partcnt_id | type: CONSTRAINT --
-- ALTER TABLE pms.partner_contact DROP CONSTRAINT IF EXISTS fk_partcnt_id CASCADE;
ALTER TABLE pms.partner_contact ADD CONSTRAINT fk_partcnt_id FOREIGN KEY (partner_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_ppol_part | type: CONSTRAINT --
-- ALTER TABLE pms.partner_policy DROP CONSTRAINT IF EXISTS fk_ppol_part CASCADE;
ALTER TABLE pms.partner_policy ADD CONSTRAINT fk_ppol_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_ppol_apol | type: CONSTRAINT --
-- ALTER TABLE pms.partner_policy DROP CONSTRAINT IF EXISTS fk_ppol_apol CASCADE;
ALTER TABLE pms.partner_policy ADD CONSTRAINT fk_ppol_apol FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_papr_part | type: CONSTRAINT --
-- ALTER TABLE pms.partner_policy_request DROP CONSTRAINT IF EXISTS fk_papr_part CASCADE;
ALTER TABLE pms.partner_policy_request ADD CONSTRAINT fk_papr_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_ppbe_part | type: CONSTRAINT --
-- ALTER TABLE pms.partner_policy_bioextract DROP CONSTRAINT IF EXISTS fk_ppbe_part CASCADE;
ALTER TABLE pms.partner_policy_bioextract ADD CONSTRAINT fk_ppbe_part FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_ppbe_pol | type: CONSTRAINT --
-- ALTER TABLE pms.partner_policy_bioextract DROP CONSTRAINT IF EXISTS fk_ppbe_pol CASCADE;
ALTER TABLE pms.partner_policy_bioextract ADD CONSTRAINT fk_ppbe_pol FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_part_id | type: CONSTRAINT --
-- ALTER TABLE pms.partner_policy_credential_type DROP CONSTRAINT IF EXISTS fk_part_id CASCADE;
ALTER TABLE pms.partner_policy_credential_type ADD CONSTRAINT fk_part_id FOREIGN KEY (part_id)
REFERENCES pms.partner (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_pol_id | type: CONSTRAINT --
-- ALTER TABLE pms.partner_policy_credential_type DROP CONSTRAINT IF EXISTS fk_pol_id CASCADE;
ALTER TABLE pms.partner_policy_credential_type ADD CONSTRAINT fk_pol_id FOREIGN KEY (policy_id)
REFERENCES pms.auth_policy (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

