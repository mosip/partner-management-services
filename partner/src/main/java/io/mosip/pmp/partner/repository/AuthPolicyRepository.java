package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.pmp.partner.entity.AuthPolicy;

public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

}
