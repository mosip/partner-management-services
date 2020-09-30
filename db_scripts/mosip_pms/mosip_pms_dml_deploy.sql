\c mosip_pms sysadmin

\set CSVDataPath '\'/home/dbadmin/mosip_pms/'

-------------- Level 1 data load scripts ------------------------

----- TRUNCATE pms.key_policy_def TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE pms.partner_type cascade ;

\COPY pms.partner_type (code,partner_description,is_active,cr_by,cr_dtimes,is_policy_required) FROM './dml/pms-partner_type.csv' delimiter ',' HEADER  csv;

---------------------------------------------------------------------------------------------------------------------------------------------------------------------


















