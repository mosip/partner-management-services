package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.PartnerPolicy;

/**
 * @author sanjeev.shrivastava
 *
 */
@Repository
public interface PartnerPolicyRepository extends JpaRepository<PartnerPolicy, String> {
	
	//@Query(value = "select * from partner_policy ppr where ppr.part_id=?", nativeQuery = true )
	//public List<PartnerPolicy> findByPartnerId(String part_id);
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?", nativeQuery = true )
	public PartnerPolicy findByPartnerId(String part_id);
 
}
