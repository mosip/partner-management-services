package io.mosip.pms.device.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.regdevice.entity.RegDeviceDetail;

@Repository
public interface RegDeviceDetailRepository extends JpaRepository<RegDeviceDetail, String>{
	
	@Query(value ="select * from device_detail where  make=?1 AND model=?2 AND dprovider_id=?3 AND dstype_code=?4 AND dtype_code=?5 AND (is_deleted is null OR is_deleted = false) AND is_active = true",nativeQuery = true)
	RegDeviceDetail findByDeviceDetail( String make, String model, String deviceProviderId, String deviceSubTypeCode,
			String deviceTypeCode);
	@Query(value ="select * from device_detail d where d.id = ?1 AND (d.is_deleted is null or d.is_deleted = false)",nativeQuery = true)
	RegDeviceDetail findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);
	
	@Query(value= "select * from device_detail d where d.id = ?1 AND (d.is_deleted is null or d.is_deleted = false) AND d.is_active=true",nativeQuery = true)
	RegDeviceDetail findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(String id);
	
	@Query(value ="select * from device_detail where  make=?1 AND model=?2 AND dprovider_id=?3 AND dstype_code=?4 AND dtype_code=?5",nativeQuery = true)
	RegDeviceDetail findUniqueDeviceDetail( String make, String model, String deviceProviderId, String deviceSubTypeCode,
			String deviceTypeCode);

}
