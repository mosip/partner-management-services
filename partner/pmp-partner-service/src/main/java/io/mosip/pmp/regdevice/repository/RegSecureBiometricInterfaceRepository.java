package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegSecureBiometricInterface;

@Repository
public interface RegSecureBiometricInterfaceRepository extends JpaRepository<RegSecureBiometricInterface, String>{
	
	@Query(value="select * from secure_biometric_interface d where d.id = ?1 AND (d.isDeleted is null or d.isDeleted = false)",nativeQuery = true)
	RegSecureBiometricInterface findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);

}
