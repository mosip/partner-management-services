\c mosip_pms

-- Rollback: Revert typeOfShare from "Data Share" back to "direct"

UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_faceQR';


UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_QR';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_faceQR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-eUIN_with_QR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"Data Share"', '"typeOfShare":"direct"')
WHERE id='mpolicy-default-PDFCard'
AND eff_dtimes='2023-11-14 05:59:00.000';

DROP TABLE IF EXISTS pms.partner_policy_bioextract_request CASCADE;

DROP TABLE IF EXISTS pms.bioextractor_configuration;

DROP TABLE IF EXISTS pms.partner_policy_credential_type_request CASCADE;