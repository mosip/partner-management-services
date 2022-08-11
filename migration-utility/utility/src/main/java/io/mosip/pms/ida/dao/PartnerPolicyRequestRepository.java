package io.mosip.pms.ida.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PartnerPolicyRequestRepository extends JpaRepository<PartnerPolicyRequest, String>{
		
	@Query(value = "select * from partner_policy_request ppr where ppr.part_id=?", nativeQuery = true )
	List<PartnerPolicyRequest> findByPartnerId(String part_id);

	@Query(value = "select * from partner_policy_request ppr where ppr.part_id=?1 and ppr.policy_id=?2", nativeQuery = true )
	List<PartnerPolicyRequest> findByPartnerIdAndPolicyId(String partnerId, String policyId);
	
	@Query(value = "select * from partner_policy_request ppr where ppr.part_id=?1 and ppr.id=?2", nativeQuery = true )
	PartnerPolicyRequest findByPartnerIdAndReqId(String partnerId, String apikeyReqId);
	
	@Query(value = "select * from partner_policy_request ppr where ppr.id=?1", nativeQuery = true )
	PartnerPolicyRequest findByReqId(String apikeyReqId);
	
	@Query(value = "select * from partner_policy_request ppr where ppr.part_id=?1 and ppr.policy_id=?2 and ppr.status_code=?3", nativeQuery = true )
	List<PartnerPolicyRequest> findByPartnerIdAndPolicyIdAndStatusCode(String partnerId, String policyId, String status);
}
