package io.mosip.pmp.partnermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;


/**
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PartnerPolicyRepository extends JpaRepository<PartnerPolicy, String> {

	@Query(value = "select * from partner_policy ppr where ppr.part_id=?", nativeQuery = true )
	public PartnerPolicy findByPartnerId(String partId);
	
	@Query(value = "select * from partner_policy ppr where ppr.policy_api_key=?", nativeQuery = true )
	public PartnerPolicy findByApiKey(String policy_api_key); 
}
