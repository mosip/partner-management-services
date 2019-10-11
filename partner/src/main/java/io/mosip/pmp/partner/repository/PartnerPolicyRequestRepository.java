package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.pmp.partner.entity.PartnerPolicyRequest;

public interface PartnerPolicyRequestRepository extends JpaRepository<PartnerPolicyRequest, String>{
		
	//PartnerPolicyRequest findByPartnerId(String part_id);
}
