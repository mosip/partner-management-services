package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.MISPLicenseEntity;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to provide misp license data base related operations.
 */
@Repository
public interface MispLicenseKeyRepository extends BaseRepository<MISPLicenseEntity, String> {

	@Query(value = "select * from misp_license ml where ml.license_key=?", nativeQuery = true)
	MISPLicenseEntity findByLicensekey(String licenseKey);
	
	@Query(value = "select * from misp_license ml where ml.misp_id=?", nativeQuery = true)
	List<MISPLicenseEntity> findByMispId(String mispId);

}
