package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.ApiKeyRequestsSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ApiKeyRequestSummaryRepository")
public interface ApiKeyRequestSummaryRepository extends BaseRepository<ApiKeyRequestsSummaryEntity, String> {

    @Query(value = "SELECT new io.mosip.pms.common.entity.ApiKeyRequestsSummaryEntity(" +
            "pp.apiKeyId, pp.partnerId, pp.label, p.name, pp.policyId, ap.name, ap.descr, pg.id, pg.name, pg.desc, " +
            "CASE " +
            "WHEN pp.isActive = false THEN 'deactivated' " +
            "WHEN pp.isActive = true THEN 'activated' " +
            "END, " +
            "pp.createdDateTime) " +
            "FROM PartnerPolicyV2 pp " +
            "LEFT JOIN pp.partner p " +
            "LEFT JOIN pp.policy ap " +
            "LEFT JOIN ap.policyGroup pg " +
            "WHERE (:partnerId IS NULL OR lower(pp.partnerId) LIKE %:partnerId%) " +
            "AND (p.partnerTypeCode = 'Auth_Partner') " +
            "AND (:apiKeyLabel IS NULL OR lower(pp.label) LIKE %:apiKeyLabel%) " +
            "AND (:orgName IS NULL OR lower(p.name) LIKE %:orgName%) " +
            "AND (:policyName IS NULL OR lower(ap.name) LIKE %:policyName%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:status IS NULL OR " +
            "(:status = 'deactivated' AND pp.isActive = false) " +
            "OR (:status = 'activated' AND pp.isActive = true))" +
            "AND (:isPartnerAdmin = true OR (p.id IN :partnerIdList)) "
    )
    Page<ApiKeyRequestsSummaryEntity> getSummaryOfAllApiKeyRequests(
            @Param("partnerId") String partnerId,
            @Param("apiKeyLabel") String apiKeyLabel,
            @Param("orgName") String orgName,
            @Param("policyName") String policyName,
            @Param("policyGroupName") String policyGroupName,
            @Param("status") String status,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );

}