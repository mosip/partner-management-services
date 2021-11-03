package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.AuthPolicy;

@Repository
public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.name = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupAndName(String policyGroupId, String name);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.id = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupAndId(String policyGroupId, String policyId);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?", nativeQuery = true)
	List<AuthPolicy> findByPolicyGroupId(String policyId);
	
	AuthPolicy findByName(String name);

}
