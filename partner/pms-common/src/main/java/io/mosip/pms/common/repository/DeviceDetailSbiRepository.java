package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.DeviceDetailSBI;

import java.util.List;

@Repository
public interface DeviceDetailSbiRepository extends JpaRepository<DeviceDetailSBI, String>{

	@Query(value = "select * from device_detail_sbi dds where dds.device_detail_id=?1 and dds.sbi_id = ?2",nativeQuery = true)
	DeviceDetailSBI findByDeviceDetailAndSbi(String deviceDetailId, String sbiId);

	@Query(value = "select * from device_detail_sbi dds where dds.dprovider_id=?1 and dds.sbi_id = ?2",nativeQuery = true)
	List<DeviceDetailSBI> findByDeviceProviderIdAndSbiId(String dproviderId, String sbiId);

	@Query(value = "select * from device_detail_sbi dds where dds.device_detail_id=?1",nativeQuery = true)
	List<DeviceDetailSBI> findByDeviceDetailId(String deviceDetailId);

	@Query(value = "select * from device_detail_sbi dds where dds.dprovider_id=?1 and dds.sbi_id=?2 and dds.device_detail_id=?3",nativeQuery = true)
	DeviceDetailSBI findByDeviceProviderIdAndSbiIdAndDeviceDetailId(String dproviderId, String sbiId, String deviceDetailId);
}
