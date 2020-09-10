package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegSecureBiometricInterface;

@Repository
public interface RegSecureBiometricInterfaceRepository extends JpaRepository<RegSecureBiometricInterface, String>{
	
	@Query(value="select * from secure_biometric_interface d where d.id = ?1 AND (d.is_deleted is null or d.is_deleted = false)",nativeQuery = true)
	RegSecureBiometricInterface findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);

}
