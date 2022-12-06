-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Release Version 	: 1.2.1
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Nagarjuna K
-- Created Date		: Aug-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_pms sysadmin

ALTER TABLE pms.partner ADD COLUMN logo_url character varying(256);

ALTER TABLE pms.partner_h ADD COLUMN logo_url character varying(256);

ALTER TABLE pms.partner ADD COLUMN addl_info character varying;

ALTER TABLE pms.partner_h ADD COLUMN addl_info character varying;

INSERT INTO pms.partner_type (code,partner_description,is_policy_required,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) VALUES
	 ('Print_Partner','Print Partner',true,true,'superadmin',now(),NULL,NULL,false,NULL);

UPDATE pms.partner_type SET is_policy_required=true, upd_dtimes =now(), upd_by ='superadmin' WHERE code='MISP_Partner';

ALTER TABLE pms.misp_license ADD COLUMN policy_id character varchar(36);


\COPY pms.policy_group (id,name,descr,user_id,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM '../dml/pms-policy_group.csv' delimiter ',' HEADER  csv;

\COPY pms.auth_policy (id,policy_group_id,name,descr,policy_file_id,policy_type,version,policy_schema,valid_from_date,valid_to_date,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM '../dml/pms-auth_policy.csv' delimiter ',' HEADER  csv;

\COPY pms.auth_policy_h (id,eff_dtimes,policy_group_id,name,descr,policy_file_id,policy_type,version,policy_schema,valid_from_date,valid_to_date,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes) FROM '../dml/pms-auth_policy_h.csv' delimiter ',' HEADER  csv;


UPDATE pms.misp_license SET policy_id = (select id from pms.auth_policy ap where ap.name = 'mpolicy-default-misp')
where misp_id IN(SELECT p.id  FROM pms.partner p INNER JOIN pms.misp_license ml ON ml.misp_id = p.id where p.partner_type_code ='MISP_Partner')
	 

