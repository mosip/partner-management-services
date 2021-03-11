package io.mosip.pmp.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.pmp.common.entity.PartnerPolicyRequest;


public interface PartnerPolicyRequestRepository extends JpaRepository<PartnerPolicyRequest, String>{
		
	@Query(value = "select * from partner_policy_request ppr where ppr.part_id=?", nativeQuery = true )
	List<PartnerPolicyRequest> findByPartnerId(String partId);
}
