package io.mosip.pms.device.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.authdevice.entity.FTPChipDetail;

@Repository
public interface FTPChipDetailRepository extends JpaRepository<FTPChipDetail, String>{
	
	@Query(value = "select * from ftp_chip_detail fcd where fcd.foundational_trust_provider_id =?1 and make =?2 and model =?3",nativeQuery = true)
	FTPChipDetail findByUniqueKey(String ftpId, String make, String model);	
}
