-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.1.4
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin


\ir ../ddl/pms-partner_policy_credential_type.sql


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

----- TRUNCATE pms.partner_policy_credential_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE pms.partner_policy_credential_type cascade ;

\COPY pms.partner_policy_credential_type (part_id,policy_id,credential_type,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) FROM './dml/pms-partner_policy_credential_type.csv' delimiter ',' HEADER  csv;


ALTER TABLE pms.misp_license DROP CONSTRAINT IF EXISTS fk_mispl_misp CASCADE;