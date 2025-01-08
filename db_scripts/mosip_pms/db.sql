CREATE DATABASE :mosipdbname
	ENCODING = 'UTF8' 
	TABLESPACE = pg_default 
	OWNER = postgres;

COMMENT ON DATABASE :mosipdbname IS 'PMS related entities and its data is stored in this database';

\c :mosipdbname

DROP SCHEMA IF EXISTS pms CASCADE;
CREATE SCHEMA pms;
ALTER SCHEMA pms OWNER TO postgres;
ALTER DATABASE :mosipdbname SET search_path TO pms,pg_catalog,public;