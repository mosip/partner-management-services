package io.mosip.pms.device.regdevice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.regdevice.entity.RegSecureBiometricInterface;

@Repository
public interface RegSecureBiometricInterfaceRepository extends JpaRepository<RegSecureBiometricInterface, String>{
	
	@Query(value="select * from secure_biometric_interface d where d.id = ?1 AND (d.is_deleted is null or d.is_deleted = false)",nativeQuery = true)
	RegSecureBiometricInterface findByIdAndIsDeletedFalseOrIsDeletedIsNull(String id);
	
	/**
	 * Find by id and is active is true.
	 *
	 * @param id the id
	 * @return the device service
	 */
	List<RegSecureBiometricInterface> findBySwVersionAndIsActiveIsTrue(String id);
	
	@Query(value = "select * from secure_biometric_interface sbi where sbi.sw_binary_hash =?1 and sbi.sw_version =?2 and sbi.sw_cr_dtimes =?3 and sbi.sw_expiry_dtimes =?4 ",nativeQuery = true)
	RegSecureBiometricInterface findByHash(byte[] swBinaryHash, String swVersion,LocalDateTime swCreateDateTime,LocalDateTime swExpiryDateTime);

}
