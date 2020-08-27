package io.mosip.pmp.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import io.mosip.pmp.authdevice.entity.FoundationalTrustProvider;
@Component
public interface FoundationalTrustProviderRepository extends JpaRepository<FoundationalTrustProvider, String> {

	@Query("FROM FoundationalTrustProvider where id=?1  and isActive=true and (isDeleted is null or isDeleted = false)")
	FoundationalTrustProvider findByIdAndIsActiveTrue(String id);

}
