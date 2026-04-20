package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerPolicyBioextractRequest;

@Repository
public interface PartnerPolicyBioextractRequestRepository extends JpaRepository<PartnerPolicyBioextractRequest, String> {
	boolean existsByPartnerPolicyRequestId(String partnerPolicyRequestId);

	List<PartnerPolicyBioextractRequest> findByPartnerPolicyRequestId(String partnerPolicyRequestId);

	List<PartnerPolicyBioextractRequest> findByPartnerPolicyRequestIdAndStatusCode(
			String partnerPolicyRequestId, String statusCode);

	List<PartnerPolicyBioextractRequest> findByPartnerPolicyRequestIdOrderByCrDtimesAsc(
			String partnerPolicyRequestId);
}
