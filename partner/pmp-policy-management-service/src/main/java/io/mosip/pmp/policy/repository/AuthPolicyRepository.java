package io.mosip.pmp.policy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.pmp.policy.entity.AuthPolicy;


/**
 * @author Nagarjuna Kuchi
 *
 */
public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

	AuthPolicy findByName(String name);
	
	@Query(value = "select * from pmp.auth_policy ap where ap.policy_group_id=?", nativeQuery = true)
	List<AuthPolicy> findByPolicyId(String policyId);
}
