package io.mosip.pmp.partnermanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.pmp.partnermanagement.entity.PartnerPolicyRequest;


public interface PartnerPolicyRequestRepository extends JpaRepository<PartnerPolicyRequest, String>{
		
	//@Query(value = "select * from partner_policy_request ppr where ppr.part_id=?", nativeQuery = true )
	//List<PartnerPolicyRequest> findByPartnerId(String part_id);
}
