-- This Source Code Form is subject to the terms of the Mozilla Public
-- License, v. 2.0. If a copy of the MPL was not distributed with this
-- file, You can obtain one at https://mozilla.org/MPL/2.0/.
-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_pms
-- Table Name : oidc_client
-- Purpose    : oicd client: Table to store all registered OIDC client details.
--           
-- Create By   	: Nagarjuna K
-- Created Date	: Sep-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------------------

-- object: oidc_client.oidc_client | type: TABLE --
-- DROP TABLE IF EXISTS oidc_client.oidc_client CASCADE;
CREATE TABLE pms.oidc_client(
	id character varying(100) NOT NULL,
	name character varying(256) NOT NULL,
	rp_id character varying(100) NOT NULL,
	policy_id character varying(50) NOT NULL,
	logo_uri character varying(2048) NOT NULL,
	redirect_uris character varying NOT NULL,
	claims character varying NOT NULL,
	acr_values character varying NOT NULL,
	public_key character varying NOT NULL,
	grant_types character varying(256) NOT NULL,
	auth_methods character varying(256) NOT NULL,
	status character varying(20) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_oidc_client PRIMARY KEY (id),
	CONSTRAINT uq_oidc_client_public_key UNIQUE (public_key)
);
-- ddl-end --
COMMENT ON TABLE oidc_client.id IS 'Client ID: Unique id assigned to registered OIDC client.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.name IS 'Client Name: Registered name of OIDC client.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.logo_uri IS 'Client Logo URL: Client logo to be displayed on IDP UI.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.redirect_uris IS 'Recirect URLS: Comma separated list of client redirect URLs.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.rp_id IS 'Relaying Party Id: Id of the partner id who has created this OIDC client.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.policy_id IS 'Policy Id: Id of the policy ';
-- ddl-end --
COMMENT ON COLUMN oidc_client.status IS 'Client status: Allowed values - ACTIVE / INACTIVE.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.public_key IS 'Public key: JWK data.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.grant_types IS 'Grant Types: Allowed grant types for the client.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.auth_methods IS 'Client Auth methods: Allowed token endpoint authentication methods.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.claims IS 'Requested Claims: claims json as per policy defined for relaying party.';
-- ddl-end --
COMMENT ON COLUMN oidc_client.acr_values IS 'Allowed Authentication context References(acr) json';
-- ddl-end --

