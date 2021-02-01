package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerPolicyCredentialType;

@Repository
public interface PartnerPolicyCredentialTypeRepository extends JpaRepository<PartnerPolicyCredentialType, String>{
	
	@Query(value = "select * from partner_policy_credential_type pc where pc.part_id =?1 and pc.credential_type =?2 ",nativeQuery = true)
	PartnerPolicyCredentialType findByPartnerIdAndCrdentialType(String partnerId,String credentialType);
}
