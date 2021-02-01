package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerPolicy;

/**
 * @author sanjeev.shrivastava
 *
 */
@Repository
public interface PartnerPolicyRepository extends JpaRepository<PartnerPolicy, String> {
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?1 AND ppr.policy_id=?2", nativeQuery = true )
	public PartnerPolicy findByPartnerIdAndPolicyId(String part_id, String policy_id);
	
	@Query(value = "select * from partner_policy pp where pp.part_id=?1 AND (d.is_deleted is null or d.is_deleted = false) AND d.is_active=true",nativeQuery = true)
	public List<PartnerPolicy> findByPartnerIdAndIsActiveTrue(String partner_Id); 
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?1 AND ppr.policy_id=?2 AND ppr.policy_api_key=?3", nativeQuery = true )
	public PartnerPolicy findByPartnerIdAndPolicyIdAndApikey(String partnerId,String policyId,String apiKey);
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?1 AND ppr.policy_api_key=?2", nativeQuery = true )
	public PartnerPolicy findByPartnerIdAndApikey(String partnerId,String apiKey);
	
	@Query(value = "select * from partner_policy ppr where ppr.policy_api_key=?", nativeQuery = true )
	public PartnerPolicy findByApiKey(String policy_api_key);
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?", nativeQuery = true )
	public PartnerPolicy findByPartnerId(String partId);
}
