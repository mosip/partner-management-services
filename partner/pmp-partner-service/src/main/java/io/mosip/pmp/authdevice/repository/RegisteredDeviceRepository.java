package io.mosip.pmp.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.authdevice.entity.RegisteredDevice;
@Repository
public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, String> {

	RegisteredDevice findByDeviceDetailIdAndSerialNoAndIsActiveIsTrue(String deviceDetailId, String serialNo);

	RegisteredDevice findByCodeAndIsActiveIsTrue(String deviceCode);

}
