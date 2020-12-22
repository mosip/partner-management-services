package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegRegisteredDevice;

@Repository
public interface RegRegisteredDeviceRepository extends JpaRepository<RegRegisteredDevice, String> {

	RegRegisteredDevice findByDeviceDetailIdAndSerialNoAndIsActiveIsTrue(String deviceDetailId, String serialNo);
	
	RegRegisteredDevice findByDeviceDetailIdAndSerialNo(String deviceDetailId, String serialNo);

	RegRegisteredDevice findByCodeAndIsActiveIsTrue(String deviceCode);
	
	/**
	 * Find by code and purpose and is active is true.
	 *
	 * @param deviceCode the device code
	 * @param purpose    the purpose
	 * @return the registered device
	 */
	RegRegisteredDevice findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(String deviceCode, String purpose);

}
