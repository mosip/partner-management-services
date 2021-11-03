package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.pms.common.entity.AuthPolicyH;
import io.mosip.pms.common.entity.AuthPolicyHPK;

/**
 * @author Nagarjuna Kuchi
 *
 */
public interface AuthPolicyHRepository extends JpaRepository<AuthPolicyH, AuthPolicyHPK> {

}
