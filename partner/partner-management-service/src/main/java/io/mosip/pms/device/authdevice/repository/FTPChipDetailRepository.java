package io.mosip.pms.device.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.authdevice.entity.FTPChipDetail;

import java.util.List;

@Repository
public interface FTPChipDetailRepository extends JpaRepository<FTPChipDetail, String>{
	
	@Query(value = "select * from ftp_chip_detail where foundational_trust_provider_id =?1 AND make =?2 AND model =?3 AND approval_status != 'rejected' AND NOT (approval_status = 'approved' AND is_active = false)",nativeQuery = true)
	FTPChipDetail findByUniqueKey(String ftpId, String make, String model);

	@Query(value = "select * from ftp_chip_detail fcd where fcd.foundational_trust_provider_id =?1 AND (fcd.is_deleted is null or fcd.is_deleted = false)",nativeQuery = true)
	List<FTPChipDetail> findByProviderId(String providerId);
}
