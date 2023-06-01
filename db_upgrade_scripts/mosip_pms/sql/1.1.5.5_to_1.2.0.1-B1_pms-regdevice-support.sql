\COPY regdevice.device_detail TO '../dml/reg-device_detail.csv' WITH (FORMAT CSV, HEADER);

\COPY regdevice.secure_biometric_interface TO '../dml/reg-secure_biometric_interface.csv' WITH (FORMAT CSV, HEADER);

\COPY regdevice.secure_biometric_interface_h TO '../dml/reg-secure_biometric_interface_h.csv' WITH (FORMAT CSV, HEADER);

\COPY regdevice.device_detail_sbi TO '../dml/reg-device_detail_sbi.csv' WITH (FORMAT CSV, HEADER);

\COPY regdevice.ftp_chip_detail TO '../dml/reg-ftp_chip_detail.csv' WITH (FORMAT CSV, HEADER);