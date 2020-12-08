package io.mosip.pmp.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.pmp.common.entity.PolicyGroup;

/**
 * @author Nagarjuna Kuchi
 *
 */
public interface PolicyGroupRepository extends JpaRepository<PolicyGroup, String> {

	PolicyGroup findByName(String name);

}
