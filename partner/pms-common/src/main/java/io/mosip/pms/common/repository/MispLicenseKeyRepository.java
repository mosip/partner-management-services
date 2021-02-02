package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import io.mosip.pms.common.entity.MISPLicenseEntity;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to provide misp license data base related operations.
 */
@Repository
public interface MispLicenseKeyRepository extends JpaRepository<MISPLicenseEntity, String> {

	@Query(value = "select * from misp_license ml where ml.license_key=?", nativeQuery = true)
	MISPLicenseEntity findByLicensekey(String licenseKey);
	
	@Query(value = "select * from misp_license ml where ml.misp_id=?", nativeQuery = true)
	List<MISPLicenseEntity> findByMispId(String mispId);

}
