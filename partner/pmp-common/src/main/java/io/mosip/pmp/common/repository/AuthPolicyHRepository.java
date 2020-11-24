package io.mosip.pmp.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.pmp.common.entity.AuthPolicyH;
import io.mosip.pmp.common.entity.AuthPolicyHPK;

/**
 * @author Nagarjuna Kuchi
 *
 */
public interface AuthPolicyHRepository extends JpaRepository<AuthPolicyH, AuthPolicyHPK> {

}
