package io.mosip.pmp.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.authdevice.entity.SecureBiometricInterface;
@Repository
public interface SecureBiometricInterfaceRepository extends JpaRepository<SecureBiometricInterface, String>{
	@Query(value ="select * from  secure_biometric_interface d where d.id = ?1 AND (d.is_deleted is null or d.is_deleted = false)",nativeQuery = true)
	SecureBiometricInterface findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);

}
