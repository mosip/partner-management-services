package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.PartnerPolicyRequestSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("PartnerPolicyMappingRequestRepository")
public interface PartnerPolicyMappingRequestRepository extends BaseRepository<PartnerPolicyRequestSummaryEntity, String> {

    @Query(value = "SELECT new PartnerPolicyRequestSummaryEntity(" +
            "ppr.id, ppr.partnerId, p.name, p.partnerTypeCode, p.policyGroup.name, ppr.policyId, ap.name, " +
            "ppr.statusCode, ppr.createdDateTime, ppr.requestDetail, ppr.updatedDateTime) " +
            "FROM PartnerPolicyRequestV2 ppr " +
            "LEFT JOIN ppr.policy ap " +
            "LEFT JOIN ppr.partner p " +
            "WHERE (:partnerId IS NULL OR lower(ppr.partnerId) LIKE %:partnerId%) " +
            "AND (:partnerTypeCode IS NULL OR lower(p.partnerTypeCode) LIKE %:partnerTypeCode%) " +
            "AND (:organizationName IS NULL OR lower(p.name) LIKE %:organizationName%) " +
            "AND (:policyName IS NULL OR lower(ap.name) LIKE %:policyName%) " +
            "AND (:policyGroupName IS NULL OR lower(p.policyGroup.name) LIKE %:policyGroupName%) " +
            "AND (:requestDetail IS NULL OR lower(ppr.requestDetail) LIKE %:requestDetail%) " +
            "AND (:statusCode IS NULL OR ppr.statusCode = :statusCode) "
    )
    Page<PartnerPolicyRequestSummaryEntity> getSummaryOfAllPartnerPolicyRequests(
            @Param("partnerId") String partnerId,
            @Param("partnerTypeCode") String partnerTypeCode,
            @Param("organizationName") String organizationName,
            @Param("policyName") String policyName,
            @Param("statusCode") String statusCode,
            @Param("requestDetail") String requestDetail,
            @Param("policyGroupName") String policyGroupName,
            Pageable pageable
    );
}