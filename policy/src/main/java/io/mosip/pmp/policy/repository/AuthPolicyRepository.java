package io.mosip.pmp.policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.pmp.policy.entity.AuthPolicy;


/**
 * @author Nagarjuna Kuchi
 *
 */
public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

	AuthPolicy findByName(String name);
}
