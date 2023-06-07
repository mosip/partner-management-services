\COPY regdevice.device_detail TO 'dml/reg-device_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY (select sbi.id,sbi.sw_binary_hash,sbi.sw_version,sbi.sw_cr_dtimes,sbi.sw_expiry_dtimes,sbi.approval_status,sbi.is_active,sbi.cr_by,sbi.cr_dtimes,sbi.upd_by,sbi.upd_dtimes,sbi.is_deleted,sbi.del_dtimes,dd.dprovider_id,dd.partner_org_name from regdevice.secure_biometric_interface sbi inner join regdevice.device_detail dd on  sbi.device_detail_id = dd.id) TO 'dml/reg-secure_biometric_interface.csv' WITH (FORMAT CSV, HEADER);

\COPY (select sbi.id,sbi.sw_binary_hash,sbi.sw_version,sbi.sw_cr_dtimes,sbi.sw_expiry_dtimes,sbi.approval_status,sbi.is_active,sbi.cr_by,sbi.cr_dtimes,sbi.upd_by,sbi.upd_dtimes,sbi.is_deleted,sbi.del_dtimes,sbi.eff_dtimes,dd.dprovider_id,dd.partner_org_name from regdevice.secure_biometric_interface_h sbi inner join regdevice.device_detail dd on  sbi.device_detail_id = dd.id) TO 'dml/reg-secure_biometric_interface_h.csv' WITH (FORMAT CSV, HEADER);

\COPY regdevice.ftp_chip_detail TO 'dml/reg-ftp_chip_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY (select dd.dprovider_id,dd.partner_org_name,dd.id,sbi.id, sbi.is_active,sbi.cr_by,sbi.cr_dtimes,sbi.upd_by,sbi.upd_dtimes,sbi.is_deleted,sbi.del_dtimes from regdevice.secure_biometric_interface sbi inner join regdevice.device_detail dd on device_detail_id = dd.id) TO 'dml/reg-device_detail_sbi.csv' WITH (FORMAT CSV, HEADER);

