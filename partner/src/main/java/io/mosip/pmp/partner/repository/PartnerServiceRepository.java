package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.Partner;

/**
 * Repository class for create partner id.
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PartnerServiceRepository extends JpaRepository<Partner, String> {

	//public List<Partner> findByName(String name);
	
	public Partner findByName(String name);
	
	//@Query(value = "select * from partner ppr where ppr.policy_group_id=?", nativeQuery = true )
	//public Partner findByPolicyId(String policyGroupId);
}
