package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PolicyGroup;

import java.util.List;

/**
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PolicyGroupRepository extends JpaRepository<PolicyGroup, String> {
	
	public PolicyGroup findByName(String policyName);

	@Query(value = "select pg.name from policy_group pg where pg.id=?", nativeQuery = true )
	public String findPolicyGroupNameById(String policyGroupId);

	@Query(value = "select * from policy_group pg where pg.id=?", nativeQuery = true )
	public PolicyGroup findPolicyGroupById(String policyGroupId);

	@Query(value = "select * from policy_group pg where pg.is_active=true", nativeQuery = true )
	public List<PolicyGroup> findAllActivePolicyGroups();
}
