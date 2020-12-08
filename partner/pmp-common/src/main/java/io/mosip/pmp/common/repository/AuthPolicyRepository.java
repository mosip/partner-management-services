package io.mosip.pmp.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.pmp.common.entity.AuthPolicy;


/**
 * @author Nagarjuna Kuchi
 *
 */
public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

	AuthPolicy findByName(String name);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?", nativeQuery = true)
	List<AuthPolicy> findByPolicyGroupId(String policyId);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.name = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupAndName(String policyGroupId, String name);
	
	@Query(value = "select * from auth_policy ppr where ppr.id=?", nativeQuery = true )
	public AuthPolicy findByPolicyId(String policyGroupId);	
}
