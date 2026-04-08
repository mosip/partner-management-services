package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerPolicyBioextractRequest;

@Repository
public interface PartnerPolicyBioextractRequestRepository extends JpaRepository<PartnerPolicyBioextractRequest, String> {
	boolean existsByPartnerPolicyRequestIdAndIsDeletedFalseAndStatusCodeIn(
			String partnerPolicyRequestId, List<String> statusCode);

	boolean existsByPartnerPolicyRequestIdAndIsDeletedFalseAndStatusCode(
			String partnerPolicyRequestId, String statusCode);

	List<PartnerPolicyBioextractRequest> findByPartnerPolicyRequestIdAndIsDeletedFalseAndStatusCode(
			String partnerPolicyRequestId, String statusCode);

	List<PartnerPolicyBioextractRequest> findByPartnerPolicyRequestIdAndIsDeletedFalseOrderByCrDtimesAsc(
			String partnerPolicyRequestId);
}
