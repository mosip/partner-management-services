COPY authdevice.device_detail TO '../dml/auth-device_detail.csv' WITH (FORMAT CSV, HEADER);

COPY authdevice.secure_biometric_interface TO '../dml/auth-secure_biometric_interface.csv' WITH (FORMAT CSV, HEADER);

COPY authdevice.secure_biometric_interface_h TO '../dml/auth-secure_biometric_interface_h.csv' WITH (FORMAT CSV, HEADER);

COPY authdevice.device_detail_sbi TO '../dml/auth-device_detail_sbi.csv' WITH (FORMAT CSV, HEADER);

COPY authdevice.ftp_chip_detail TO '../dml/auth-ftp_chip_detail.csv' WITH (FORMAT CSV, HEADER);