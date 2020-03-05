package io.mosip.pmp.partnermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.pmp.partnermanagement.entity.AuthPolicy;

public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{
	
	@Query(value = "select * from auth_policy ppr where ppr.policy_group_id=?", nativeQuery = true )
	public AuthPolicy findByPolicyId(String policyGroupId);
}
