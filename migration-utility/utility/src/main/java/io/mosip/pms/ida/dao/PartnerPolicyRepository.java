package io.mosip.pms.ida.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

/**
 * @author sanjeev.shrivastava
 *
 */
@Repository
public interface PartnerPolicyRepository extends JpaRepository<PartnerPolicy, String> {
	
	@Query(value = "select * from partner_policy pp where pp.part_id=?1 AND pp.policy_id=?2 AND (pp.is_deleted is null or pp.is_deleted = false) AND pp.is_active=true",nativeQuery = true)
	public List<PartnerPolicy> findByPartnerIdAndPolicyIdAndIsActiveTrue(String part_id, String policy_id);
	
	@Query(value = "select * from partner_policy pp where pp.part_id=?1 AND (pp.is_deleted is null or pp.is_deleted = false) AND pp.is_active=true",nativeQuery = true)
	public List<PartnerPolicy> findByPartnerIdAndIsActiveTrue(String partner_Id); 
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?1 AND ppr.policy_id=?2 AND ppr.policy_api_key=?3", nativeQuery = true )
	public PartnerPolicy findByPartnerIdAndPolicyIdAndApikey(String partnerId,String policyId,String apiKey);
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?1 AND ppr.policy_api_key=?2", nativeQuery = true )
	public PartnerPolicy findByPartnerIdAndApikey(String partnerId,String apiKey);
	
	@Query(value = "select * from partner_policy ppr where ppr.policy_api_key=?", nativeQuery = true )
	public PartnerPolicy findByApiKey(String policy_api_key);
	
	@Query(value = "select * from partner_policy ppr where ppr.part_id=?", nativeQuery = true )
	public PartnerPolicy findByPartnerId(String partId);
	
	@Query(value = "select * from partner_policy pp where pp.policy_api_key=? AND (pp.is_deleted is null or pp.is_deleted = false) AND pp.is_active=true",nativeQuery = true)
	public PartnerPolicy findByPolicyApiKey(String policyApiKey);
	
	@Query(value = "select * from partner_policy pp where pp.valid_to_datetime <?1 AND pp.valid_to_datetime >?2  AND (pp.is_deleted is null or pp.is_deleted = false) AND pp.is_active=true", nativeQuery = true)
	public List<PartnerPolicy> findAPIKeysLessThanGivenDate(LocalDateTime validToDate, LocalDateTime fromDate);
	
	@Query(value = "select * from partner_policy pp where pp.policy_id=?1 AND (pp.is_deleted is null or pp.is_deleted = false) AND pp.is_active=true", nativeQuery = true)
	public List<PartnerPolicy> findByPolicyIdAndIsActiveTrue(String policy_id);

	@Query(value = "select * from partner_policy pp where pp.part_id=?1 AND pp.policy_id=?2 AND pp.label=?3",nativeQuery = true)
	public PartnerPolicy findByPartnerIdPolicyIdAndLabel(String partnerId, String policyId, String label);
}
