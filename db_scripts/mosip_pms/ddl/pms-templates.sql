-- -------------------------------------------------------------------------------------------------
-- Database Name    : mosip_pms
-- Release Version 	: 1.3.0-beta.1
-- Purpose    		: Database Alter scripts for the release for PMS DB.       
-- Create By   		: Swetha K
-- Created Date		: Feb-2025
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------
\c mosip_pms

-- This table has templates of email notifications.
CREATE TABLE pms.templates(
    id character varying(36) NOT NULL,
    lang_code character varying(36) NOT NULL,
    template_name character varying(64) NOT NULL,
    template character varying NOT NULL,
    cr_dtimes timestamp NOT NULL,
    cr_by character varying(256) NOT NULL,
	upd_dtimes timestamp NOT NULL,
    upd_by character varying(256) NOT NULL,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
    CONSTRAINT pk_templates PRIMARY KEY (id),
	CONSTRAINT uk_templates UNIQUE (lang_code, template_name)
);
COMMENT ON COLUMN pms.templates.id IS 'ID: Unique Id generated for an template.';
COMMENT ON COLUMN pms.templates.lang_code IS 'Lang Code: Language of the template stored.';
COMMENT ON COLUMN pms.templates.template_name IS 'Template Name: Name of the template saved.';
COMMENT ON COLUMN pms.templates.template IS 'Template: Stores the actual template data.';
COMMENT ON COLUMN pms.templates.cr_by IS 'Created By : ID or name of the user who create / insert record.';
COMMENT ON COLUMN pms.templates.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN pms.templates.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN pms.templates.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN pms.templates.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN pms.templates.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';