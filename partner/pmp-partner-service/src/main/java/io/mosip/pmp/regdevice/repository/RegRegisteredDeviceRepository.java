package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegRegisteredDevice;

@Repository
public interface RegRegisteredDeviceRepository extends JpaRepository<RegRegisteredDevice, String> {

	RegRegisteredDevice findByDeviceDetailIdAndSerialNoAndIsActiveIsTrue(String deviceDetailId, String serialNo);

	RegRegisteredDevice findByCodeAndIsActiveIsTrue(String deviceCode);

}
