package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.AuthPolicy;

@Repository
public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.name = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupAndName(String policyGroupId, String name);
}
