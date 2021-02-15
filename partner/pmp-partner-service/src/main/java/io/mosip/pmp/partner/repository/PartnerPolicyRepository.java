package io.mosip.pmp.partner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.PartnerPolicy;

/**
 * @author sanjeev.shrivastava
 *
 */
@Repository
public interface PartnerPolicyRepository extends JpaRepository<PartnerPolicy, String> {
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?1 AND ppr.policy_id=?2", nativeQuery = true )
	public List<PartnerPolicy> findByPartnerIdAndPolicyId(String part_id, String policy_id);
	
	@Query(value = "select * from partner_policy pp where pp.part_id=?1 AND (pp.is_deleted is null or pp.is_deleted = false) AND pp.is_active=true",nativeQuery = true)
	public List<PartnerPolicy> findByPartnerIdAndIsActiveTrue(String partner_Id); 
	
	@Query(value = "select * from partner_policy pp where pp.policy_api_key=? AND (pp.is_deleted is null or pp.is_deleted = false) AND pp.is_active=true",nativeQuery = true)
	public PartnerPolicy findByApiKey(String policy_api_key);
}
