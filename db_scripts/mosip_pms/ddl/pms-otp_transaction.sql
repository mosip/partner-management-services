-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name 	: pms.otp_transaction
-- Purpose    	: OTP Transaction: All OTP related data and validation details are maintained here fro partner managment service.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted flag not null changes for 1.1.5  
-- ------------------------------------------------------------------------------------------

-- object: pms.otp_transaction | type: TABLE --
-- DROP TABLE IF EXISTS pms.otp_transaction CASCADE;
CREATE TABLE pms.otp_transaction(
	id character varying(36) NOT NULL,
	ref_id character varying(64) NOT NULL,
	otp_hash character varying(512) NOT NULL,
	generated_dtimes timestamp,
	expiry_dtimes timestamp,
	validation_retry_count smallint,
	status_code character varying(36),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_otpt_id PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE pms.otp_transaction IS 'OTP Transaction: All OTP related data and validation details are maintained here fro partner managment service.';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.id IS 'ID: Unique transaction id for each otp transaction request';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.ref_id IS 'Reference ID: Reference ID is a reference information received from OTP requester which can be used while validating the OTP. AM: please give examples of ref_id';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.otp_hash IS 'OTP Hash: Hash of id, ref_id and otp which is generated based on the configuration setup and sent to the requester application / module.';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.generated_dtimes IS 'Generated Date Time: Date and Time when the OTP was generated';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.expiry_dtimes IS 'Expiry Date Time: Date Time when the OTP will be expired';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.validation_retry_count IS 'Validation Retry Count: Validation retry counts of this OTP request. If the validation retry crosses the threshold limit, then the OTP will be de-activated.';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.status_code IS 'Status Code: Status of the OTP whether it is active or expired. AM: please enumerate the status types. They are only a few, not infinite';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN pms.otp_transaction.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
