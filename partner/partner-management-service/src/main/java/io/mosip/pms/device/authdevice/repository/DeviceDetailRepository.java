package io.mosip.pms.device.authdevice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.authdevice.entity.DeviceDetail;

@Repository
public interface DeviceDetailRepository extends JpaRepository<DeviceDetail, String>{
	
	@Query(value ="select * FROM device_detail  where  make=?1 AND model=?2 AND dprovider_id=?3 AND dstype_code=?4 AND dtype_code=?5 AND (is_deleted is null OR is_deleted = false) AND is_active = true",nativeQuery = true)
	DeviceDetail findByDeviceDetail( String make, String model, String deviceProviderId, String deviceSubTypeCode,
			String deviceTypeCode);
	@Query("FROM DeviceDetail d where d.id = ?1 AND (d.isDeleted is null or d.isDeleted = false)")
	DeviceDetail findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);
	
	@Query("FROM DeviceDetail d where d.id = ?1 AND (d.isDeleted is null or d.isDeleted = false) AND d.isActive=true ")
	DeviceDetail findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(String id);
	
	@Query(value ="select * from device_detail where  make=?1 AND model=?2 AND dprovider_id=?3 AND dstype_code=?4 AND dtype_code=?5 AND approval_status != 'rejected' AND NOT (approval_status = 'approved' AND is_active = false)",nativeQuery = true)
	DeviceDetail findUniqueDeviceDetail( String make, String model, String deviceProviderId, String deviceSubTypeCode,
			String deviceTypeCode);
	
	@Query(value= "select * from device_detail d where d.id IN :ids AND (d.is_deleted is null or d.is_deleted = false) AND d.is_active=true",nativeQuery = true)
	List<DeviceDetail> findByIds(@Param("ids") List<String> ids);

	@Query("FROM DeviceDetail d where d.id=?1 AND d.deviceProviderId=?2 AND (d.isDeleted is null or d.isDeleted = false)")
	Optional<DeviceDetail> findByIdAndDeviceProviderId(String id, String deviceProviderId);

	@Query(value= " select * from device_detail dd join device_detail_sbi dds on dd.id = dds.device_detail_id where dds.sbi_id = ?1 AND dd.approval_status = 'approved' AND dd.is_active = true",nativeQuery = true)
	List<DeviceDetail> findApprovedDevicesBySbiId(String sbiId);

	@Query(value= " select * from device_detail dd join device_detail_sbi dds on dd.id = dds.device_detail_id where dds.sbi_id = ?1 AND dd.approval_status = 'pending_approval'",nativeQuery = true)
	List<DeviceDetail> findPendingApprovalDevicesBySbiId(String sbiId);
}
