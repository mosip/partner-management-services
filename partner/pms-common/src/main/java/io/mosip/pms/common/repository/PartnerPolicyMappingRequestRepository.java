package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.PartnerPolicyRequestSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerPolicyMappingRequestRepository extends BaseRepository<PartnerPolicyRequestSummaryEntity, String> {

    @Query(value = "SELECT new PartnerPolicyRequestSummaryEntity(" +
            "ppr.id, ppr.partnerId, " +
            "CASE " +
            "WHEN p.approvalStatus = 'approved' AND p.isActive = true THEN 'activated' " +
            "WHEN p.approvalStatus = 'approved' AND p.isActive = false THEN 'deactivated' " +
            "WHEN p.approvalStatus = 'InProgress' THEN 'InProgress' " +
            "WHEN p.approvalStatus = 'rejected' THEN 'rejected' " +
            "END AS status, " +
            "p.name, p.partnerTypeCode, p.policyGroupId, pg.name, ppr.policyId, ap.name, " +
            "ppr.statusCode, ppr.createdDateTime, ppr.requestDetail, ppr.updatedDateTime, ap.descr, pg.desc) " +
            "FROM PartnerPolicyRequestV2 ppr " +
            "LEFT JOIN ppr.policy ap " +
            "LEFT JOIN ppr.partner p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:partnerId IS NULL OR lower(ppr.partnerId) LIKE %:partnerId%) " +
            "AND (:partnerTypeCode IS NULL OR lower(p.partnerTypeCode) LIKE %:partnerTypeCode%) " +
            "AND (:organizationName IS NULL OR lower(p.name) LIKE %:organizationName%) " +
            "AND (:policyId IS NULL OR lower(ap.id) LIKE %:policyId%) " +
            "AND (:policyName IS NULL OR lower(ap.name) LIKE %:policyName%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:partnerComment IS NULL OR lower(ppr.requestDetail) LIKE %:partnerComment%) " +
            "AND (:statusCode IS NULL OR ppr.statusCode = :statusCode) " +
            "AND (:isPartnerAdmin = true OR (p.id IN :partnerIdList)) "
    )
    Page<PartnerPolicyRequestSummaryEntity> getSummaryOfAllPartnerPolicyRequests(
            @Param("partnerId") String partnerId,
            @Param("partnerTypeCode") String partnerTypeCode,
            @Param("organizationName") String organizationName,
            @Param("policyId") String policyId,
            @Param("policyName") String policyName,
            @Param("statusCode") String statusCode,
            @Param("partnerComment") String partnerComment,
            @Param("policyGroupName") String policyGroupName,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );
}