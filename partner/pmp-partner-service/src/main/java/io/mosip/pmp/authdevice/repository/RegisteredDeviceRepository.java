package io.mosip.pmp.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.authdevice.entity.RegisteredDevice;
@Repository
public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, String> {
	
	@Query("FROM RegisteredDevice d where d.deviceDetailId = ?1 AND serialNo=?2 AND (d.isDeleted is null or d.isDeleted = false) AND d.isActive=true ")
	RegisteredDevice findByDeviceDetailIdAndSerialNoAndIsActiveIsTrue(String deviceDetailId, String serialNo);
	
	@Query("FROM RegisteredDevice d where d.deviceDetailId = ?1 AND serialNo=?2 AND (d.isDeleted is null or d.isDeleted = false)")
	RegisteredDevice findByDeviceDetailIdAndSerialNo(String deviceDetailId, String serialNo);
	
	@Query("FROM RegisteredDevice d where d.code =?1 AND (d.isDeleted is null or d.isDeleted = false) AND d.isActive=true ")
	RegisteredDevice findByCodeAndIsActiveIsTrue(String deviceCode);

}
