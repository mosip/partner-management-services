-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name 	: pms.device_detail
-- Purpose    	: Device Detail SBI : Mapping of device details and SBI
--           
-- Create By   	: Ram Bhatt
-- Created Date	: Dec-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------
-- object: pms.device_detail | type: TABLE --
-- DROP TABLE IF EXISTS pms.device_detail_sbi CASCADE;
CREATE TABLE pms.device_detail_sbi(
	dprovider_id character varying(36) NOT NULL,
	partner_org_name character varying(128),
	device_detail_id character varying(36) NOT NULL,
	sbi_id character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp

);
-- ddl-end --

