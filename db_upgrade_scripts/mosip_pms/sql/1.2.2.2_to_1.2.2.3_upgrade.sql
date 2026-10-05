\c mosip_pms

-- -------------------------------------------------------------------------------------------------
-- MOSIP-42249: default policy expiry dates were hardcoded and have started to expire.
-- Extend validity of the default policies and default partner policies to 200 years.
-- Same change that db_scripts/mosip_pms/dml.sql applies on a fresh install (added in release 1.2.2.3).
-- -------------------------------------------------------------------------------------------------

UPDATE pms.auth_policy
SET valid_to_date = valid_from_date + interval '200 years'
WHERE policy_group_id LIKE 'mpolicygroup-default%' AND id LIKE 'mpolicy-default%';

UPDATE pms.auth_policy_h
SET valid_to_date = valid_from_date + interval '200 years'
WHERE policy_group_id LIKE 'mpolicygroup-default%' AND id LIKE 'mpolicy-default%';

UPDATE pms.partner_policy
SET valid_to_datetime = valid_from_datetime + interval '200 years'
WHERE part_id LIKE 'mpartner-default%' AND policy_id LIKE 'mpolicy-default%';
