package io.mosip.pms.device.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;

@Repository
public interface RegistrationDeviceSubTypeRepository extends JpaRepository<RegistrationDeviceSubType, String>{
	
	@Query(value ="select * FROM reg_device_sub_type  where code=?1  AND dtyp_code=?2 AND (is_deleted is null OR is_deleted = false) AND is_active = true",nativeQuery = true)
	RegistrationDeviceSubType findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String deviceSubTypeCode,
			String deviceTypeCode);

}
