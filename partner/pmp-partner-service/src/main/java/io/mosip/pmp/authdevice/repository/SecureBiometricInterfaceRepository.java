package io.mosip.pmp.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.authdevice.entity.SecureBiometricInterface;
@Repository
public interface SecureBiometricInterfaceRepository extends JpaRepository<SecureBiometricInterface, String>{
	@Query("FROM SecureBiometricInterface d where d.id = ?1 AND (d.isDeleted is null or d.isDeleted = false)")
	SecureBiometricInterface findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);

}
