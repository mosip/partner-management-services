\COPY authdevice.device_detail TO 'dml/auth-device_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY (select sbi.id,sbi.sw_binary_hash,sbi.sw_version,sbi.sw_cr_dtimes,sbi.sw_expiry_dtimes,sbi.approval_status,sbi.is_active,sbi.cr_by,sbi.cr_dtimes,sbi.upd_by,sbi.upd_dtimes,sbi.is_deleted,sbi.del_dtimes,dd.dprovider_id,dd.partner_org_name from authdevice.secure_biometric_interface sbi inner join authdevice.device_detail dd on  device_detail_id = dd.id) TO 'dml/auth-secure_biometric_interface.csv' WITH (FORMAT CSV, HEADER);

\COPY (select sbi.id,sbi.sw_binary_hash,sbi.sw_version,sbi.sw_cr_dtimes,sbi.sw_expiry_dtimes,sbi.approval_status,sbi.is_active,sbi.cr_by,sbi.cr_dtimes,sbi.upd_by,sbi.upd_dtimes,sbi.is_deleted,sbi.del_dtimes,sbi.eff_dtimes,dd.dprovider_id,dd.partner_org_name from authdevice.secure_biometric_interface_h sbi inner join authdevice.device_detail dd on  device_detail_id = dd.id) TO 'dml/auth-secure_biometric_interface_h.csv' WITH (FORMAT CSV, HEADER);

\COPY authdevice.ftp_chip_detail TO 'dml/auth-ftp_chip_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY (select dd.dprovider_id,dd.partner_org_name,dd.id,sbi.id,sbi.is_active,sbi.cr_by,sbi.cr_dtimes,sbi.upd_by,sbi.upd_dtimes,sbi.is_deleted,sbi.del_dtimes from authdevice.secure_biometric_interface sbi inner join authdevice.device_detail dd on  device_detail_id = dd.id) TO 'dml/auth-device_detail_sbi.csv' WITH (FORMAT CSV, HEADER);
