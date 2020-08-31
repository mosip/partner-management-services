package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegFoundationalTrustProvider;

@Repository
public interface RegFoundationalTrustProviderRepository extends JpaRepository<RegFoundationalTrustProvider, String> {

	@Query(value="select * from foundational_trust_provider where id=?1  and isActive=true and (isDeleted is null or isDeleted = false)",nativeQuery = true)
	RegFoundationalTrustProvider findByIdAndIsActiveTrue(String id);

}
