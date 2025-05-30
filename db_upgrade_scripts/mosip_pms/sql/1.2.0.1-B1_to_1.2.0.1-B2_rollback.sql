\c mosip_pms

-- Drop the newly added columns from pms.partner and pms.partner_h
ALTER TABLE pms.partner DROP COLUMN IF EXISTS logo_url;
ALTER TABLE pms.partner_h DROP COLUMN IF EXISTS logo_url;
ALTER TABLE pms.partner DROP COLUMN IF EXISTS addl_info;
ALTER TABLE pms.partner_h DROP COLUMN IF EXISTS addl_info;

-- Drop the newly added column from pms.misp_license
ALTER TABLE pms.misp_license DROP COLUMN IF EXISTS policy_id;

-- Drop the newly created pms.oidc_client table
DROP TABLE IF EXISTS pms.oidc_client CASCADE;