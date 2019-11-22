-- object: sysadmin | type: ROLE --
--DROP ROLE IF EXISTS sysadmin;
CREATE ROLE sysadmin WITH 
	SUPERUSER
	CREATEDB
	CREATEROLE
	INHERIT
	LOGIN
	REPLICATION
	PASSWORD 'postgres';
-- ddl-end --

-- object: dbadmin | type: ROLE --
--DROP ROLE IF EXISTS dbadmin;
CREATE ROLE dbadmin WITH 
	CREATEDB
	CREATEROLE
	INHERIT
	LOGIN
	REPLICATION
	PASSWORD 'postgres';
-- ddl-end --

-- object: appadmin | type: ROLE --
--DROP ROLE IF EXISTS appadmin;
CREATE ROLE appadmin WITH 
	INHERIT
	LOGIN
	PASSWORD 'postgres';
-- ddl-end --

