package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegDeviceDetail;

@Repository
public interface RegDeviceDetailRepository extends JpaRepository<RegDeviceDetail, String>{
	
	@Query(value ="select * from device_detail where  make=?1 AND model=?2 AND dprovider_id=?3 AND dstype_code=?4 AND dtype_code=?5 AND (is_deleted is null OR is_deleted = false) AND is_active = true",nativeQuery = true)
	RegDeviceDetail findByDeviceDetail( String make, String model, String deviceProviderId, String deviceSubTypeCode,
			String deviceTypeCode);
	@Query(value ="select * from device_detail d where d.id = ?1 AND (d.isDeleted is null or d.isDeleted = false)",nativeQuery = true)
	RegDeviceDetail findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);
	
	@Query(value= "select * from device_detail d where d.id = ?1 AND (d.isDeleted is null or d.isDeleted = false) AND d.isActive=true",nativeQuery = true)
	RegDeviceDetail findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(String id);

}
