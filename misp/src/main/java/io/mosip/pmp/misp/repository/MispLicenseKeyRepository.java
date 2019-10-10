package io.mosip.pmp.misp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pmp.misp.entity.MISPLicenseEntity;

@Repository
public interface MispLicenseKeyRepository extends BaseRepository<MISPLicenseEntity, String> {

	@Query(value = "select * from pmp.misp_license ml where ml.license_key=?", nativeQuery = true)
	List<MISPLicenseEntity> findByLicensekey(String licenseKey);
	
	@Query(value = "select * from pmp.misp_license ml where ml.misp_id=?", nativeQuery = true)
	List<MISPLicenseEntity> findByMispId(String mispId);

}
