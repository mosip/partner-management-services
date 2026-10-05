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

-- -------------------------------------------------------------------------------------------------
-- MOSIP-42571: auth_policy.csv / auth_policy_h.csv had invalid JSON in mpolicy-default-mobile
-- (an extra comma after "encrypted":false for dateOfBirth). Correct it only where the bad value is present,
-- so any policy edited by the user is left untouched.
-- -------------------------------------------------------------------------------------------------

UPDATE pms.auth_policy
SET policy_file_id='{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"}]}],"encrypted":false,"format":"extraction"}],"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"}}'
WHERE id='mpolicy-default-mobile'
AND policy_file_id LIKE '%"encrypted":false,,"format"%';

UPDATE pms.auth_policy_h
SET policy_file_id='{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":false},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":false,"format":"YYYY"},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":false},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":false},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":false},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":false},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":false},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":false},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":false},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":false},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":false},{"attributeName":"UIN","source":[{"attribute":"UIN"}],"encrypted":false},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":false},{"attributeName":"biometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics","filter":[{"type":"Face"}]}],"encrypted":false,"format":"extraction"}],"dataSharePolicies":{"typeOfShare":"direct","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"}}'
WHERE id='mpolicy-default-mobile'
AND eff_dtimes='2021-01-16 05:55:00.000'
AND policy_file_id LIKE '%"encrypted":false,,"format"%';
