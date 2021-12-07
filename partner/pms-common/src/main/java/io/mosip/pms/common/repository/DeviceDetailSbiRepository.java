package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.DeviceDetailSBI;

@Repository
public interface DeviceDetailSbiRepository extends JpaRepository<DeviceDetailSBI, String>{

	@Query(value = "select * from device_detail_sbi dds where dds.device_detail_id=?1 and dds.sbi_id = ?2",nativeQuery = true)
	DeviceDetailSBI findByDeviceDetailAndSbi(String deviceDetailId, String sbiId);
}
