-- object: pmsuser | type: ROLE --
-- DROP ROLE IF EXISTS pmsuser;
CREATE ROLE pmsuser WITH 
	INHERIT
	LOGIN
	PASSWORD :dbuserpwd;
-- ddl-end --
