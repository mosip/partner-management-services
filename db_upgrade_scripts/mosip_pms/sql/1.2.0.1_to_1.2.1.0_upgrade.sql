\c mosip_pms

ALTER TABLE pms.partner ADD COLUMN logo_url character varying(256);
ALTER TABLE pms.partner ADD COLUMN addl_info character varying;

ALTER TABLE pms.partner_h ADD COLUMN logo_url character varying(256);
ALTER TABLE pms.partner_h ADD COLUMN addl_info character varying;
