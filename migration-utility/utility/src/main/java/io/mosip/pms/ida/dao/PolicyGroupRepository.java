package io.mosip.pms.ida.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PolicyGroupRepository extends JpaRepository<PolicyGroup, String> {
	
	public PolicyGroup findByName(String policyName);
}
