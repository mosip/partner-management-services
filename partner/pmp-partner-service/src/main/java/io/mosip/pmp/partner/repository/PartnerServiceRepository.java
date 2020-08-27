package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.Partner;

/**
 * Repository class for create partner id.
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PartnerServiceRepository extends JpaRepository<Partner, String> {
	
	@Query(value = "select * from partner ppr where ppr.name=?", nativeQuery = true)
	public Partner findByName(String name);
	@Query(value = "select * from partner ppr where ppr.id=?", nativeQuery = true)
	public Partner findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String deviceProviderId);

}
