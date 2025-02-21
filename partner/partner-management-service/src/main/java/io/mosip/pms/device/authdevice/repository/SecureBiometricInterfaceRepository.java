package io.mosip.pms.device.authdevice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;

@Repository
public interface SecureBiometricInterfaceRepository extends JpaRepository<SecureBiometricInterface, String>{
	@Query(value ="select * from  secure_biometric_interface d where d.id = ?1 AND (d.is_deleted is null or d.is_deleted = false)",nativeQuery = true)
	SecureBiometricInterface findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);
	
	@Query(value ="select * from  secure_biometric_interface d where d.id = ?1 AND (d.is_deleted is null or d.is_deleted = false) AND d.is_active=true",nativeQuery = true)
	SecureBiometricInterface findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(String id);
	
	@Query(value = "select * from  secure_biometric_interface d where d.provider_id = ?1 AND d.sw_version=?2 AND (d.is_deleted is null or d.is_deleted = false)", nativeQuery = true)
	List<SecureBiometricInterface> findByProviderIdAndSwVersion(String providerId, String swversion);
}
