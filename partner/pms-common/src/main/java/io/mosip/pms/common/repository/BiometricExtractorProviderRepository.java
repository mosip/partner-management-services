package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.BiometricExtractorProvider;

@Repository
public interface BiometricExtractorProviderRepository extends JpaRepository<BiometricExtractorProvider, String>{

	@Query(value = "select * from partner_policy_bioextract ppb where ppb.part_id=?1 and ppb.policy_id = ?2",nativeQuery = true)
	List<BiometricExtractorProvider> findByPartnerAndPolicyId(String partnerId, String policyId);
	
	@Query(value = "select * from partner_policy_bioextract ppb where ppb.part_id=?1 and ppb.policy_id = ?2 and ppb.attribute_name =?3",nativeQuery = true)
	BiometricExtractorProvider findByPartnerAndPolicyIdAndAttributeName(String partnerId, String policyId, String attributeName);

} 
