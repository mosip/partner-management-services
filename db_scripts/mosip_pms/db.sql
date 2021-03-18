DROP DATABASE IF EXISTS mosip_pms;
CREATE DATABASE mosip_pms 
	ENCODING = 'UTF8' 
--	LC_COLLATE = 'en_US.UTF-8' 
--	LC_CTYPE = 'en_US.UTF-8' 
	TABLESPACE = pg_default 
	OWNER = sysadmin;

-- ddl-end --
COMMENT ON DATABASE mosip_pms IS 'PMS related entities and its data is stored in this database';
-- ddl-end --

\c mosip_pms sysadmin

-- object: pms | type: SCHEMA --
DROP SCHEMA IF EXISTS pms CASCADE;
CREATE SCHEMA pms;
-- ddl-end --
ALTER SCHEMA pms OWNER TO sysadmin;
-- ddl-end --


ALTER DATABASE mosip_pms SET search_path TO pms,pg_catalog,public;
-- ddl-end --

-- REVOKE CONNECT ON DATABASE mosip_pms FROM PUBLIC;
-- REVOKE ALL ON SCHEMA pms FROM PUBLIC;
-- REVOKE ALL ON ALL TABLES IN SCHEMA pms FROM PUBLIC ;
