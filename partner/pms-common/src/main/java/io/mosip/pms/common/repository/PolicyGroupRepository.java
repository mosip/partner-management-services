package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PolicyGroup;

/**
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PolicyGroupRepository extends JpaRepository<PolicyGroup, String> {
	
	public PolicyGroup findByName(String policyName);
}
