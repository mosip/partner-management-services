package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegFTPChipDetail;

@Repository
public interface RegFTPChipDetailRepository extends JpaRepository<RegFTPChipDetail, String>{
	
	@Query(value = "select * from ftp_chip_detail fcd where fcd.foundational_trust_provider_id =?1 and make =?2 and model =?3",nativeQuery = true)
	RegFTPChipDetail findByUniqueKey(String ftpId, String make, String model);	
}
