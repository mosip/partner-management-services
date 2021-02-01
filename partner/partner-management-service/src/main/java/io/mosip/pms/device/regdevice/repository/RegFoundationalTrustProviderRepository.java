package io.mosip.pms.device.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.regdevice.entity.RegFoundationalTrustProvider;

@Repository
public interface RegFoundationalTrustProviderRepository extends JpaRepository<RegFoundationalTrustProvider, String> {

	@Query(value="select * from foundational_trust_provider where id=?1  and is_active=true and (is_deleted is null or is_deleted = false)",nativeQuery = true)
	RegFoundationalTrustProvider findByIdAndIsActiveTrue(String id);

}
