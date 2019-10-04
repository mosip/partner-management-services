package io.mosip.pmp.partnermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partnermanagement.entity.PolicyGroup;

/**
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PolicyGroupRepository extends JpaRepository<PolicyGroup, String> {

 
}
