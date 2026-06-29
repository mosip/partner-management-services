\c mosip_pms

-- Attribute name changed from individualBiometrics to photo in pms.partner_policy_bioextract for id 146098
UPDATE pms.partner_policy_bioextract
SET attribute_name='photo',
    upd_by='admin',
    upd_dtimes=now()
WHERE id='146098';

-- Table : pms.auth_policy
-- Reason: CRVS attributes needs to be added
-- Note: Before executing check the existing policy_file_id for id='mpolicy-default-auth'
-- Extra attributes added: {"attributeName":"declaredAsDeceased","source":[{"attribute":"declaredAsDeceased"}],"encrypted":true}
-- After execution restart: Regproc, Packet-Manager, PMS
UPDATE pms.auth_policy
SET policy_file_id='{"shareableAttributes":[{"attributeName":"fullName","source":[{"attribute":"fullName"}],"encrypted":true},{"attributeName":"dateOfBirth","source":[{"attribute":"dateOfBirth"}],"encrypted":true},{"attributeName":"gender","source":[{"attribute":"gender"}],"encrypted":true},{"attributeName":"phone","source":[{"attribute":"phone"}],"encrypted":true},{"attributeName":"email","source":[{"attribute":"email"}],"encrypted":true},{"attributeName":"addressLine1","source":[{"attribute":"addressLine1"}],"encrypted":true},{"attributeName":"addressLine2","source":[{"attribute":"addressLine2"}],"encrypted":true},{"attributeName":"addressLine3","source":[{"attribute":"addressLine3"}],"encrypted":true},{"attributeName":"region","source":[{"attribute":"region"}],"encrypted":true},{"attributeName":"province","source":[{"attribute":"province"}],"encrypted":true},{"attributeName":"city","source":[{"attribute":"city"}],"encrypted":true},{"attributeName":"postalCode","source":[{"attribute":"postalCode"}],"encrypted":true},{"attributeName":"zone","source":[{"attribute":"zone"}],"encrypted":true},{"attributeName":"preferredLang","source":[{"attribute":"preferredLang"}],"encrypted":false},{"attributeName":"individualBiometrics","group":"CBEFF","source":[{"attribute":"individualBiometrics"}],"encrypted":true,"format":"extraction"},{"attributeName":"declaredAsDeceased","source":[{"attribute":"declaredAsDeceased"}],"encrypted":true}],"dataSharePolicies":{"typeOfShare":"Data Share","validForInMinutes":"30","transactionsAllowed":"2","encryptionType":"Partner Based","shareDomain":"datashare.datashare","source":"ID Repository"}}',
    upd_by='admin',
    upd_dtimes=now()
WHERE id='mpolicy-default-auth';

