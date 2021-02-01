package io.mosip.pms.device.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import io.mosip.pms.device.authdevice.entity.FoundationalTrustProvider;

@Component
public interface FoundationalTrustProviderRepository extends JpaRepository<FoundationalTrustProvider, String> {

	@Query(value="select * from foundational_trust_provider where id=?1  and is_active=true and (is_deleted is null or is_deleted = false)",nativeQuery = true)
	FoundationalTrustProvider findByIdAndIsActiveTrue(String id);

}
