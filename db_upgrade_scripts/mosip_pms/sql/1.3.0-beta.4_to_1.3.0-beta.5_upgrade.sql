\c mosip_pms

-- Updated type of share from direct to data share for the below policies
UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_faceQR';


UPDATE pms.auth_policy
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_QR';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_faceQR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-eUIN_with_QR'
AND eff_dtimes='2020-11-13 05:58:00.000';


UPDATE pms.auth_policy_h
SET policy_file_id = REPLACE(policy_file_id, '"typeOfShare":"direct"', '"typeOfShare":"Data Share"')
WHERE id='mpolicy-default-PDFCard'
AND eff_dtimes='2023-11-14 05:59:00.000';

CREATE TABLE IF NOT EXISTS pms.bioextractor_configuration(
                                                             id character varying(36) NOT NULL,
    config_name character varying(128) NOT NULL,
    bioextractor_provider_name character varying(128) NOT NULL,
    bioextractor_provider_version character varying(36),
    bio_modality character varying(64) NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    CONSTRAINT pk_bioextractor_configuration PRIMARY KEY (id),
    CONSTRAINT uq_bioextractor_configuration_config_name UNIQUE (config_name)
    );
