package io.mosip.pmp.policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import io.mosip.pmp.policy.entity.AuthPolicyH;
import io.mosip.pmp.policy.entity.AuthPolicyHPK;

/**
 * @author Nagarjuna Kuchi
 *
 */
public interface AuthPolicyHRepository extends JpaRepository<AuthPolicyH, AuthPolicyHPK> {

}
