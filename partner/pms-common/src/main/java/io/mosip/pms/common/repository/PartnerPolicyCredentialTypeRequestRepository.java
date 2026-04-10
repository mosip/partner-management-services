package io.mosip.pms.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerPolicyCredentialTypeRequest;

@Repository
public interface PartnerPolicyCredentialTypeRequestRepository extends JpaRepository<PartnerPolicyCredentialTypeRequest, String> {
	boolean existsByPartnerPolicyRequestId(String partnerPolicyRequestId);

	List<PartnerPolicyCredentialTypeRequest> findByPartnerPolicyRequestIdAndStatusCode(
			String partnerPolicyRequestId, String statusCode);

	Optional<PartnerPolicyCredentialTypeRequest> findFirstByPartnerPolicyRequestIdOrderByCrDtimesAsc(String partnerPolicyRequestId);

}

